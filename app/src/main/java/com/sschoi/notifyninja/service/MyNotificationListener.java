package com.sschoi.notifyninja.service;

import android.app.Notification;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsManager;
import android.util.Log;

import com.sschoi.notifyninja.db.DBHelper;
import com.sschoi.notifyninja.db.LogDBHelper;
import com.sschoi.notifyninja.model.AppModel;
import com.sschoi.notifyninja.util.SMSHelper;

import java.util.ArrayList;
import java.util.List;

public class MyNotificationListener extends NotificationListenerService {

    private static final String TAG = "NotiListener";
    private static final String KAKAO_PACKAGE = "com.kakao.talk";
    private DBHelper db;
    private LogDBHelper logDb;  // 로그 전용 DB

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            db = new DBHelper(this);
            logDb = new LogDBHelper(this);
            Log.d(TAG, "Service created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating service", e);
        }
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            Log.d(TAG, "Service destroyed");
        } catch (Exception e) {
            Log.e(TAG, "Error destroying service", e);
        }
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.d(TAG, "Notification listener connected");
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        Log.d(TAG, "Notification listener disconnected");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            if (sbn == null || db == null || logDb == null) {
                Log.w(TAG, "Service not properly initialized");
                return;
            }

            String pkg = sbn.getPackageName();
            if (pkg == null) {
                Log.w(TAG, "Package name is null");
                return;
            }

            List<AppModel> targets = db.getAllByPackage(pkg);
            if (targets.isEmpty()) return;

            Notification n = sbn.getNotification();
            if (n == null) {
                Log.w(TAG, "Notification is null for package: " + pkg);
                return;
            }

            Bundle extras = n.extras;
            if (extras == null) {
                Log.w(TAG, "Notification extras is null for package: " + pkg);
                return;
            }

            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

            // SMS 앱 알림 처리 - 발신자 번호 추출 시도
            if (isSMSApp(pkg)) {
                String senderNumber = extractSenderNumber(title, text, big);
                String titleStr = title == null ? "" : title.toString();
                String textStr = text == null ? "" : text.toString();
                String bigStr = big == null ? "" : big.toString();
                String contentAll = (titleStr + "\n" + bigStr + "\n" + textStr);
                
                sendFilteredSMS(targets, pkg, contentAll, false, senderNumber);
                return;
            }

            // 카카오톡 알림 처리
            if ("com.kakao.talk".equals(pkg)) {
                // 메시지 라인 추출 (MessagingStyle)
                ArrayList<CharSequence> lines = extras.getCharSequenceArrayList(Notification.EXTRA_TEXT_LINES);
                if (lines != null && !lines.isEmpty()) {
                    for (CharSequence line : lines) {
                        sendFilteredSMS(targets, pkg, line.toString(), true, null);
                    }
                    return;
                }
            }

            // 일반 알림 처리
            String titleStr = title == null ? "" : title.toString();
            String textStr = text == null ? "" : text.toString();
            String bigStr = big == null ? "" : big.toString();
            String contentAll = (titleStr + "\n" + bigStr + "\n" + textStr);

            sendFilteredSMS(targets, pkg, contentAll, false, null);
        } catch (Exception e) {
            Log.e(TAG, "Error processing notification", e);
        }
    }

    /**
     * SMS 앱인지 확인
     */
    private boolean isSMSApp(String packageName) {
        // 주요 SMS 앱 패키지명들
        return "com.android.mms".equals(packageName) ||
               "com.samsung.android.messaging".equals(packageName) ||
               "com.google.android.apps.messaging".equals(packageName) ||
               "com.android.messaging".equals(packageName);
    }

    /**
     * 알림에서 발신자 번호 추출 시도
     */
    private String extractSenderNumber(CharSequence title, CharSequence text, CharSequence big) {
        String fullText = (title != null ? title.toString() : "") + 
                         (text != null ? text.toString() : "") + 
                         (big != null ? big.toString() : "");
        
        // 전화번호 패턴 매칭 (010, 011, 016, 017, 018, 019로 시작하는 10-11자리)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(01[0-9]\\d{7,8})");
        java.util.regex.Matcher matcher = pattern.matcher(fullText);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return null;
    }

    /**
     * 필터 적용 후 SMS 발송
     * @param kakaoFilter true면 카톡용: sender 필터만 적용
     * @param senderNumber 추출된 발신자 번호 (SMS용)
     */
    private void sendFilteredSMS(List<AppModel> targets, String pkg, String content, boolean kakaoFilter, String senderNumber) {
        try {
            if (targets == null || targets.isEmpty() || content == null) {
                Log.w(TAG, "Invalid parameters for sendFilteredSMS");
                return;
            }

            String contentLower = content.toLowerCase();
            int count = 0;

            for (AppModel target : targets) {
                if (target == null) continue;

                boolean shouldSend = true;
                String filterReason = "";

                // 이름 필터 체크
                String nameFilter = target.getSenderNameFilter();
                if (nameFilter != null && !nameFilter.trim().isEmpty()) {
                    if (kakaoFilter) {
                        // 카카오톡: 보낸사람 필터만, content 대신 sender 이름 포함 여부 체크
                        if (!nameFilter.toLowerCase().contains(contentLower)) {
                            shouldSend = false;
                            filterReason = "이름 필터 불일치: " + nameFilter;
                        }
                    } else {
                        // 일반 알림: 내용 전체 기준 필터
                        if (!contentLower.contains(nameFilter.toLowerCase())) {
                            shouldSend = false;
                            filterReason = "이름 필터 불일치: " + nameFilter;
                        }
                    }
                }

                // 번호 필터 체크 (SMS에서 발신자 번호가 추출된 경우)
                String numberFilter = target.getSenderNumberFilter();
                if (numberFilter != null && !numberFilter.trim().isEmpty() && senderNumber != null) {
                    if (!senderNumber.contains(numberFilter)) {
                        shouldSend = false;
                        filterReason = "번호 필터 불일치: " + numberFilter + " (발신자: " + senderNumber + ")";
                    }
                }

                // 필터링 조건이 있지만 매칭되지 않은 경우
                if (!shouldSend) {
                    Log.d(TAG, "필터링됨: " + filterReason);
                    continue;
                }

                if (count >= 5) break;

                boolean sent = SMSHelper.sendSMS(this, target.getPhone(), content);
                if (logDb != null) {
                    logDb.insertLog(pkg, target.getPhone(), content, sent ? "SENT" : "FAILED");
                }
                Log.d(TAG, "SMS " + (sent ? "sent" : "fail") + " to " + target.getPhone() + " | " + content);
                count++;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in sendFilteredSMS", e);
        }
    }


}
