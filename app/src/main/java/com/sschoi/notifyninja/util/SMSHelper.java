package com.sschoi.notifyninja.util;

import android.content.Context;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import java.util.ArrayList;

public class SMSHelper {

    public static boolean sendSMS(Context context, String phoneNumber, String message) {
    if (phoneNumber == null || phoneNumber.isEmpty() || message == null) return false;

    try {
        SmsManager sms;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SubscriptionManager subManager = context.getSystemService(SubscriptionManager.class);
            if (subManager == null) return false;

            int subId = SubscriptionManager.getDefaultSmsSubscriptionId();
            sms = SmsManager.getSmsManagerForSubscriptionId(subId);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            int subId = SubscriptionManager.getDefaultSmsSubscriptionId();
            sms = SmsManager.getSmsManagerForSubscriptionId(subId);
        } else {
            sms = SmsManager.getDefault();
        }

        if (sms == null) return false;

        ArrayList<String> parts = sms.divideMessage(message);
        sms.sendMultipartTextMessage(phoneNumber, null, parts, null, null);

        return true;
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}

}
