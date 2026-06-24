package com.example.kolokvijum2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvSenzor, tvRezultat;
    private CheckBox cbKontinenti, cbTreciKontinent;
    private ImageButton btnKamera;

    private SensorManager sensorManager;
    private Sensor proximitySensor;

    private float poslednjaVrednost = 0;
    private final float PRAG = 5.0f;

    private DatabaseHelper dbHelper;

    private Uri imageUri;
    private File imageFile;

    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSenzor = findViewById(R.id.tvSenzor);
        tvRezultat = findViewById(R.id.tvRezultat);
        cbKontinenti = findViewById(R.id.cbKontinenti);
        cbTreciKontinent = findViewById(R.id.cbTreciKontinent);
        btnKamera = findViewById(R.id.btnKamera);

        dbHelper = new DatabaseHelper(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (proximitySensor == null) {
            tvSenzor.setText("Proximity senzor nije dostupan");
        }

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(
                                MainActivity.this,
                                "Slika sačuvana: " + imageFile.getAbsolutePath(),
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );

        btnKamera.setOnClickListener(v -> pokreniKameru());

        cbKontinenti.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                dobaviKontinente();
            }
        });

        cbTreciKontinent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Integer brojDrzava = dbHelper.getCountriesOfThirdContinent();

                if (brojDrzava != null) {
                    tvRezultat.setText("Broj država trećeg kontinenta: " + brojDrzava);
                } else {
                    tvRezultat.setText("Treći kontinent ne postoji u bazi");
                }
            } else {
                tvRezultat.setText("Očitavanje blizine: " + poslednjaVrednost);
            }
        });
    }

    private void pokreniKameru() {
        try {
            imageFile = File.createTempFile(
                    "slika_",
                    ".jpg",
                    getCacheDir()
            );

            imageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    imageFile
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cameraLauncher.launch(intent);

        } catch (IOException e) {
            Toast.makeText(this, "Greška pri kreiranju fajla", Toast.LENGTH_SHORT).show();
        }
    }

    private void dobaviKontinente() {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.getContinents().enqueue(new Callback<List<Continent>>() {
            @Override
            public void onResponse(Call<List<Continent>> call, Response<List<Continent>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dbHelper.deleteAll();

                    int sacuvano = 0;

                    for (Continent c : response.body()) {
                        if (c.getPopulation() > 10000) {
                            dbHelper.insertContinent(c);
                            sacuvano++;
                        }
                    }

                    Toast.makeText(
                            MainActivity.this,
                            "Sačuvano kontinenata: " + sacuvano,
                            Toast.LENGTH_SHORT
                    ).show();
                } else {
                    Toast.makeText(MainActivity.this, "Greška u odgovoru servera", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Continent>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Greška: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        poslednjaVrednost = event.values[0];

        tvSenzor.setText("Proximity vrednost: " + poslednjaVrednost);

        if (poslednjaVrednost < PRAG) {
            Toast.makeText(this, "Blizu", Toast.LENGTH_SHORT).show();
        }

        if (!cbTreciKontinent.isChecked()) {
            tvRezultat.setText("Očitavanje blizine: " + poslednjaVrednost);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}