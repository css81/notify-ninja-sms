package com.sschoi.notifyninja.util;

import android.content.Context;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;

public class SMSHelper {

    public static boolean sendSMS(Context context, String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isEmpty() || message == null) return false;

        try {
            SmsManager sms;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 이상 → 기본 SIM ID 기반 SmsManager
                SubscriptionManager subManager = context.getSystemService(SubscriptionManager.class);
                if (subManager == null) return false;

                int subId = SubscriptionManager.getDefaultSmsSubscriptionId();
                sms = SmsManager.getSmsManagerForSubscriptionId(subId);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                // Android 5.1 이상 → SubscriptionManager 지원
                int subId = SubscriptionManager.getDefaultSmsSubscriptionId();
                sms = SmsManager.getSmsManagerForSubscriptionId(subId);
            } else {
                // 구버전 fallback
                sms = SmsManager.getDefault();
            }

            if (sms == null) return false;

            for (String part : sms.divideMessage(message)) {
                sms.sendTextMessage(phoneNumber, null, part, null, null);
            }

            return true; // 성공
        } catch (Exception e) {
            e.printStackTrace();
            return false; // 실패
        }
    }
}
