package com.example.gpshike;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        // Initialize Exit Button
        Button btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> finish());

        // Initialize ImageButtons
        ImageButton btnDatum = findViewById(R.id.btnDatum);
        ImageButton btnMaps = findViewById(R.id.btnMaps);
        ImageButton btnWaypoints = findViewById(R.id.btnWaypoints);
        ImageButton btnTracks = findViewById(R.id.btnTracks);
        ImageButton btnImport = findViewById(R.id.btnImport);
        ImageButton btnExport = findViewById(R.id.btnExport);
        ImageButton btnSatellites = findViewById(R.id.btnSatellites);
        ImageButton btnTranslate = findViewById(R.id.btnTranslate);

        // Set Click Listeners
        btnDatum.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, DatumActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Error opening screen", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Failed to launch", e);
            }
        });
        btnDatum.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, WaypointsActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Error opening screen", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Failed to launch", e);
            }
        });
        btnDatum.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, DatumActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Error opening screen", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Failed to launch", e);
            }
        });
        btnDatum.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, TracksActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Error opening screen", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Failed to launch", e);
            }
        });
        btnDatum.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, ImportActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Error opening screen", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Failed to launch", e);
            }
        });
        btnDatum.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, ExportActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Error opening screen", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Failed to launch", e);
            }
        });
        btnDatum.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, SatellitesActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Error opening screen", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Failed to launch", e);
            }
        });
        btnDatum.setOnClickListener(v -> {
            try {
                startActivity(new Intent(MainActivity.this, TranslateActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Error opening screen", Toast.LENGTH_SHORT).show();
                Log.e("MainActivity", "Failed to launch", e);
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Explain why you need the permission (optional)
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs location permission to track your hikes")
                        .setPositiveButton("OK", (dialog, which) ->
                                requestLocationPermission())
                        .create()
                        .show();
            } else {
                requestLocationPermission();
            }
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted - proceed with location access
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied - disable location functionality
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void launchActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity.this, cls);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}