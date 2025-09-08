package com.sschoi.notifyninja;

import android.content.Context;
import android.os.Build;
import android.telephony.SmsManager;

public class SMSHelper {

    public static void sendSMS(Context context, String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isEmpty() || message == null) return;

        SmsManager sms = (Build.VERSION.SDK_INT >= 31)
                ? context.getSystemService(SmsManager.class)
                : SmsManager.getDefault();

        for (String part : sms.divideMessage(message)) {
            sms.sendTextMessage(phoneNumber, null, part, null, null);
        }
    }
}
