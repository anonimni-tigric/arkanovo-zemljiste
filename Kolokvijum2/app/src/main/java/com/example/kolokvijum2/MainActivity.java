package com.example.kolokvijum2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.kolokvijum2.database.AppDatabase;
import com.example.kolokvijum2.model.Country;
import com.example.kolokvijum2.retrofit.ApiService;
import com.example.kolokvijum2.retrofit.RetrofitClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textViewLokacija;
    private TextView textViewProximity;

    private FusedLocationProviderClient fusedLocationClient;

    private CheckBox checkBoxDrzave;
    private AppDatabase database;

    private SensorManager sensorManager;
    private Sensor proximitySensor;

    private static final int LOCATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewLokacija = findViewById(R.id.textViewLokacija);
        textViewProximity = findViewById(R.id.textViewProximity);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        checkBoxDrzave = findViewById(R.id.checkBoxDrzave);
        database = AppDatabase.getInstance(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (proximitySensor == null) {
            textViewProximity.setText("Proximity senzor nije dostupan");
        }

        prikaziLokaciju();

        checkBoxDrzave.setOnClickListener(v -> {
            if (checkBoxDrzave.isChecked()) {
                dobaviISacuvajDrzave();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (proximitySensor != null) {
            sensorManager.registerListener(
                    this,
                    proximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (proximitySensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float vrednost = event.values[0];

            textViewProximity.setText("Proximity:\nVrednost: " + vrednost);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void prikaziLokaciju() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        textViewLokacija.setText(
                                "Lokacija:\nLat: " + location.getLatitude()
                                        + "\nLon: " + location.getLongitude()
                        );
                    } else {
                        textViewLokacija.setText("Lokacija nije dostupna");
                    }
                });
    }

    private void dobaviISacuvajDrzave() {
        ApiService apiService = RetrofitClient
                .getRetrofit()
                .create(ApiService.class);

        apiService.getCountries().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String text = response.body().string();
                        List<Country> countries = parsirajDrzave(text);

                        new Thread(() -> {
                            database.countryDao().insertAll(countries);
                            int count = database.countryDao().getCount();

                            runOnUiThread(() -> {
                                Toast.makeText(
                                        MainActivity.this,
                                        "Sačuvano država: " + count,
                                        Toast.LENGTH_SHORT
                                ).show();

                                checkBoxDrzave.setChecked(false);
                            });
                        }).start();

                    } catch (Exception e) {
                        Toast.makeText(
                                MainActivity.this,
                                "Greška parsiranja: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();

                        checkBoxDrzave.setChecked(false);
                    }

                } else {
                    Toast.makeText(
                            MainActivity.this,
                            "Greška pri dobavljanju država",
                            Toast.LENGTH_SHORT
                    ).show();

                    checkBoxDrzave.setChecked(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(
                        MainActivity.this,
                        "Greška: " + t.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();

                checkBoxDrzave.setChecked(false);
            }
        });
    }

    private List<Country> parsirajDrzave(String text) {
        List<Country> countries = new ArrayList<>();

        String[] delovi = text.split("\\},");
        for (String deo : delovi) {
            try {
                String name = deo.split("name: '")[1].split("'")[0];
                String code = deo.split("code: '")[1].split("'")[0];

                countries.add(new Country(name, code));
            } catch (Exception ignored) {
            }
        }

        return countries;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                prikaziLokaciju();
            } else {
                textViewLokacija.setText("Dozvola za lokaciju nije odobrena");
            }
        }
    }
}