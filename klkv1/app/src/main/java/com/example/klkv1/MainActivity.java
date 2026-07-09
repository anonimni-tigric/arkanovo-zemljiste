package com.example.klkv1;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int MENU_MOVIE = 1;

    private MovieFragment movieFragment;
    private MovieReceiver movieReceiver;

    private final BroadcastReceiver cameraReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent == null) {
                        return;
                    }

                    if ("com.example.klkv1.CAMERA_CHECK"
                            .equals(intent.getAction())) {

                        boolean allowed =
                                intent.getBooleanExtra("allowed", false);

                        if (movieFragment != null
                                && movieFragment.getView() != null) {

                            Button btnRecord =
                                    movieFragment.getView()
                                            .findViewById(R.id.btnRecord);

                            if (btnRecord != null) {
                                btnRecord.setEnabled(allowed);
                            }
                        }
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        movieReceiver = new MovieReceiver();

        IntentFilter movieFilter =
                new IntentFilter(
                        "com.example.klkv1.MOVIE_ADDED"
                );

        ContextCompat.registerReceiver(
                this,
                movieReceiver,
                movieFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        IntentFilter cameraFilter =
                new IntentFilter(
                        "com.example.klkv1.CAMERA_CHECK"
                );

        ContextCompat.registerReceiver(
                this,
                cameraReceiver,
                cameraFilter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        requestCameraPermission();

        Intent serviceIntent =
                new Intent(
                        this,
                        CameraCheckService.class
                );

        startService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(
                0,
                MENU_MOVIE,
                0,
                "Movie"
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(
            @NonNull MenuItem item
    ) {
        if (item.getItemId() == MENU_MOVIE) {

            movieFragment = new MovieFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.fragmentContainer,
                            movieFragment
                    )
                    .commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    REQUEST_CAMERA_PERMISSION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_CAMERA_PERMISSION) {

            boolean allowed =
                    grantResults.length > 0
                            && grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;

            if (movieFragment != null
                    && movieFragment.getView() != null) {

                Button btnRecord =
                        movieFragment.getView()
                                .findViewById(R.id.btnRecord);

                if (btnRecord != null) {
                    btnRecord.setEnabled(allowed);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(movieReceiver);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            unregisterReceiver(cameraReceiver);
        } catch (IllegalArgumentException ignored) {
        }

        stopService(
                new Intent(
                        this,
                        CameraCheckService.class
                )
        );

        super.onDestroy();
    }
}