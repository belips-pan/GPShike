// **************************
// * Panagiotis Beligiannis *
// **************************

package com.example.gpshike;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private Class<?> pendingActivity; // Stores activity waiting for permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initViews();
        checkLocationPermission();
    }

    private void initViews() {
        Button btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> finish());

        // Initialize all ImageButtons
        ImageButton btnDatum = findViewById(R.id.btnDatum);
        ImageButton btnMaps = findViewById(R.id.btnMaps);
        ImageButton btnWaypoints = findViewById(R.id.btnWaypoints);
        ImageButton btnTracks = findViewById(R.id.btnTracks);
        ImageButton btnImport = findViewById(R.id.btnImport);
        ImageButton btnExport = findViewById(R.id.btnExport);
        ImageButton btnSatellites = findViewById(R.id.btnSatellites);
        ImageButton btnTranslate = findViewById(R.id.btnTranslate);

        // Set click listeners
        btnDatum.setOnClickListener(v -> launchActivity(DatumActivity.class));
        btnMaps.setOnClickListener(v -> launchActivity(MapsActivity.class));
        btnWaypoints.setOnClickListener(v -> launchActivity(WaypointsActivity.class));
        btnTracks.setOnClickListener(v -> launchActivity(TracksActivity.class));
        btnImport.setOnClickListener(v -> launchActivity(ImportActivity.class));
        btnExport.setOnClickListener(v -> launchActivity(ExportActivity.class));
        btnSatellites.setOnClickListener(v -> launchActivity(SatellitesActivity.class));
        btnTranslate.setOnClickListener(v -> launchActivity(TranslateActivity.class));
        Button btnClearCache = findViewById(R.id.btnClearCache);
        btnClearCache.setOnClickListener(v -> clearCaches());
    }

    private void clearCaches() {
        try {
            // Clear waypoints cache
            File waypointsFile = new File(getFilesDir(), "waypoints_cache.txt");
            if (waypointsFile.exists()) {
                FileWriter waypointsWriter = new FileWriter(waypointsFile, false);
                waypointsWriter.write("");
                waypointsWriter.close();
            }

            // Clear tracks cache
            File tracksFile = new File(getFilesDir(), "tracks_cache.txt");
            if (tracksFile.exists()) {
                FileWriter tracksWriter = new FileWriter(tracksFile, false);
                tracksWriter.write("");
                tracksWriter.close();
            }

            Toast.makeText(this, "Caches cleared", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error clearing caches", Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Cache clear failed", e);
        }
    }

    private void launchActivity(Class<?> targetActivity) {
        // Check if activity requires location permission
        if (requiresLocationPermission(targetActivity)) {
            if (hasLocationPermission()) {
                startTargetActivity(targetActivity);
            } else {
                pendingActivity = targetActivity;
                requestLocationPermission();
            }
        } else {
            startTargetActivity(targetActivity);
        }
    }

    private boolean requiresLocationPermission(Class<?> activity) {
        // List of activities that need location permission
        return activity == MapsActivity.class || 
               activity == TracksActivity.class ||
               activity == WaypointsActivity.class ||
               activity == SatellitesActivity.class;
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, 
               Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startTargetActivity(Class<?> targetActivity) {
        try {
            startActivity(new Intent(this, targetActivity));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open " + targetActivity.getSimpleName(), 
                         Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "Activity launch failed", e);
        }
    }

    private void checkLocationPermission() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Location Required")
                    .setMessage("This feature needs location access to work properly")
                    .setPositiveButton("OK", (dialog, which) -> 
                        ActivityCompat.requestPermissions(this,
                            new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            },
                            LOCATION_PERMISSION_REQUEST_CODE))
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && 
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingActivity != null) {
                    startTargetActivity(pendingActivity);
                    pendingActivity = null;
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
