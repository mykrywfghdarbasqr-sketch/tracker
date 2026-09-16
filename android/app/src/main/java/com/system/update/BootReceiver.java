package com.system.update;

import android.content.*;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        Intent svc = new Intent(ctx, UploadService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            ctx.startForegroundService(svc);
        } else {
            ctx.startService(svc);
        }
    }
}
