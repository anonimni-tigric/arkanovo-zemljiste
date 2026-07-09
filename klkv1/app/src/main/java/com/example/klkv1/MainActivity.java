package com.example.klkv1;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int MENU_MOVIE = 1;
    private static final int REQUEST_CAMERA = 100;

    private MovieFragment movieFragment;
    private MovieReceiver movieReceiver;

    private BroadcastReceiver cameraReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean allowed = intent.getBooleanExtra("allowed", false);

            if (movieFragment != null && movieFragment.getBtnRecord() != null) {
                movieFragment.getBtnRecord().setEnabled(allowed);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(144, 238, 144));

        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle("Movie App");
        toolbar.setBackgroundColor(Color.rgb(100, 180, 100));
        setSupportActionBar(toolbar);

        root.addView(toolbar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        LinearLayout fragmentContainer = new LinearLayout(this);
        fragmentContainer.setId(ViewIdGenerator.generateViewId());
        fragmentContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(fragmentContainer, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        ));

        setContentView(root);

        movieReceiver = new MovieReceiver();
        registerReceiver(
                movieReceiver,
                new IntentFilter("com.example.klkv1.MOVIE_ADDED"),
                Context.RECEIVER_NOT_EXPORTED
        );

        registerReceiver(
                cameraReceiver,
                new IntentFilter("com.example.klkv1.CAMERA_CHECK"),
                Context.RECEIVER_NOT_EXPORTED
        );

        requestCameraPermission();

        startService(new Intent(this, CameraCheckService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_MOVIE, 0, "Movie");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_MOVIE) {
            movieFragment = new MovieFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(1, movieFragment)
                    .commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA
            );
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(movieReceiver);
        unregisterReceiver(cameraReceiver);
        stopService(new Intent(this, CameraCheckService.class));
        super.onDestroy();
    }
}