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
        db = new DBHelper(this);
        logDb = new LogDBHelper(this);

        Log.d(TAG, "Service created");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        List<AppModel> targets = db.getAllByPackage(pkg);
        if (targets.isEmpty()) return;

        Notification n = sbn.getNotification();
        Bundle extras = n.extras;
        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

        // 카카오톡 알림 처리
        if ("com.kakao.talk".equals(pkg)) {
            // 메시지 라인 추출 (MessagingStyle)
            ArrayList<CharSequence> lines = extras.getCharSequenceArrayList(Notification.EXTRA_TEXT_LINES);
            if (lines != null && !lines.isEmpty()) {
                for (CharSequence line : lines) {
                    sendFilteredSMS(targets, pkg, line.toString(), true);
                }
                return;
            }
        }

        // 일반 알림 처리
        String titleStr = title == null ? "" : title.toString();
        String textStr = text == null ? "" : text.toString();
        String bigStr = big == null ? "" : big.toString();
        String contentAll = (titleStr + "\n" + bigStr + "\n" + textStr);

        sendFilteredSMS(targets, pkg, contentAll, false);
    }

    /**
     * 필터 적용 후 SMS 발송
     * @param kakaoFilter true면 카톡용: sender 필터만 적용
     */
    private void sendFilteredSMS(List<AppModel> targets, String pkg, String content, boolean kakaoFilter) {
        String contentLower = content.toLowerCase();
        int count = 0;

        for (AppModel target : targets) {
            String nameFilter = target.getSenderNameFilter();
            if (nameFilter != null && !nameFilter.trim().isEmpty()) {
                if (kakaoFilter) {
                    // 카카오톡: 보낸사람 필터만, content 대신 sender 이름 포함 여부 체크
                    if (!nameFilter.toLowerCase().contains(contentLower)) {
                        continue;
                    }
                } else {
                    // 일반 알림: 내용 전체 기준 필터
                    if (!contentLower.contains(nameFilter.toLowerCase())) {
                        continue;
                    }
                }
            }

            if (count >= 5) break;

            boolean sent = SMSHelper.sendSMS(this, target.getPhone(), content);
            logDb.insertLog(pkg, target.getPhone(), content, sent ? "SENT" : "FAILED");
            Log.d(TAG, "SMS " + (sent ? "sent" : "fail") + " to " + target.getPhone() + " | " + content);
            count++;
        }
    }


}
