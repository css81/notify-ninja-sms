package com.sschoi.notifyninja;

import android.telephony.SmsManager;

public class SMSHelper {

    public static void sendSMS(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return;
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
}
