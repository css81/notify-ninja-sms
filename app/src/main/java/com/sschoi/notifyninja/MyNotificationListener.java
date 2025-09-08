package com.sschoi.notifyninja;

import android.app.Notification;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.List;

public class MyNotificationListener extends NotificationListenerService {

    private static final String TAG = "NotiListener";
    private DBHelper db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = new DBHelper(this);
        Log.d(TAG, "Service created");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        // 등록된 모든 번호 조회
        List<AppModel> targets = db.getAllByPackage(pkg);
        if (targets.isEmpty()) return;

        Notification n = sbn.getNotification();
        CharSequence title = n.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text  = n.extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence big   = n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

        String body = buildMessage(title, text, big);

        int count = 0;
        for (AppModel target : targets) {
            if (count >= 5) break; // 최대 5명까지만 발송
            SMSHelper.sendSMS(this, target.getPhone(), body);
            Log.d(TAG, "SMS sent to " + target.getPhone() + " | " + body);
            count++;
        }

    }

    private String buildMessage(CharSequence title, CharSequence text, CharSequence big) {
        StringBuilder sb = new StringBuilder();
        if (title != null) sb.append(title).append(": ");
        if (big != null && big.length() > 0) sb.append(big);
        else if (text != null) sb.append(text);
        String s = sb.toString();
        //if (s.length() > 140) s = s.substring(0, 140); // 과도한 길이 방지
        return s.isEmpty() ? "(알림 내용 없음)" : s;
    }



}
