package com.sschoi.notifyninja;

import android.app.Notification;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.telephony.SmsManager;
import android.util.Log;

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
        AppModel target = db.getByPackage(pkg);
        if (target == null) return;

        Notification n = sbn.getNotification();
        CharSequence title = n.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text  = n.extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence big   = n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

        String body = buildMessage(title, text, big);
        Log.d(TAG, "Matched " + pkg + " -> " + target.getPhone() + " | " + body);

        sendSMS(target.getPhone(), body);

//        try {
//            SmsManager sms = (Build.VERSION.SDK_INT >= 31)
//                    ? this.getSystemService(SmsManager.class)
//                    : SmsManager.getDefault();
//            sms.sendTextMessage(target.getPhone(), null, body, null, null);
//        } catch (Exception e) {
//            Log.e(TAG, "SMS send failed: " + e.getMessage(), e);
//        }
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

    private void sendSMS(String phoneNumber, String message) {
        //SmsManager smsManager = SmsManager.getDefault();
        SmsManager sms = (Build.VERSION.SDK_INT >= 31)
                ? getSystemService(SmsManager.class)
                : SmsManager.getDefault();
        for (String part : sms.divideMessage(message)) {
            sms.sendTextMessage(phoneNumber, null, part, null, null);
            Log.d(TAG, "SMS sent to " + phoneNumber + ": " + part);
        }
    }

}
