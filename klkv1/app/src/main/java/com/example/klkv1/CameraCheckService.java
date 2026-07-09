package com.example.klkv1;


import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

public class CameraCheckService extends Service {

    private Handler handler;

    private final Runnable cameraCheckRunnable =
            new Runnable() {
                @Override
                public void run() {

                    checkCameraPermission();

                    handler.postDelayed(
                            this,
                            60_000
                    );
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler(
                Looper.getMainLooper()
        );
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {

        handler.removeCallbacks(
                cameraCheckRunnable
        );

        handler.post(
                cameraCheckRunnable
        );

        return START_STICKY;
    }

    private void checkCameraPermission() {

        boolean allowed =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                )
                        == PackageManager.PERMISSION_GRANTED;

        Intent broadcastIntent =
                new Intent(
                        "com.example.klkv1.CAMERA_CHECK"
                );

        broadcastIntent.putExtra(
                "allowed",
                allowed
        );


        broadcastIntent.setPackage(
                getPackageName()
        );

        sendBroadcast(broadcastIntent);
    }

    @Override
    public void onDestroy() {

        if (handler != null) {
            handler.removeCallbacks(
                    cameraCheckRunnable
            );
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}