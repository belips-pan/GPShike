// **************************
// * Panagiotis Beligiannis *
// **************************

package com.example.gpshike;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import org.osmdroid.views.overlay.Polyline;
import android.os.Handler;
import com.example.gpshike.PermissionUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WaypointsActivity extends AppCompatActivity {

    private static final String WAYPOINTS_FILE = "waypoints_cache.txt";
    private int waypointId = 1;
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private EditText etDescription;
    private File waypointsFile;
    private boolean isWaitingForLocation = false;
    private GeoPoint currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Initialize OSMDroid configuration - IMPORTANT for background tile loading
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        //Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        setContentView(R.layout.waypoints_activity);

        // Initialize views
        etDescription = findViewById(R.id.etDescription);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnRecord = findViewById(R.id.btnRecord);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnZoomIn = findViewById(R.id.btnZoomIn);
        Button btnZoomOut = findViewById(R.id.btnZoomOut);
        Button btnCenter = findViewById(R.id.btnCenter);
        mapView = findViewById(R.id.mapView);

        // Setup map with proper tile source and settings
        // Initialize map view
        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Setup location overlay
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Center map on current location if available
        myLocationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            GeoPoint startPoint = myLocationOverlay.getMyLocation();
            if (startPoint != null) {
                mapView.getController().animateTo(startPoint);
                mapView.getController().setZoom(15.0);
            }
        }));
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.setUseDataConnection(true);

        // Initialize waypoints file
        waypointsFile = new File(getExternalFilesDir(null), WAYPOINTS_FILE);
        loadLastWaypointId();

        // Button listeners
        btnBack.setOnClickListener(v -> finish());

        btnRecord.setOnClickListener(v -> {
            if (!PermissionUtils.hasLocationPermission(this)) {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (myLocationOverlay == null) {
                initializeLocationFeatures();
            }

            isWaitingForLocation = true;
            myLocationOverlay.enableMyLocation();

            Toast.makeText(this, "Acquiring GPS location...", Toast.LENGTH_SHORT).show();
        });

        btnEdit.setOnClickListener(v -> showEditFragment());
        btnZoomIn.setOnClickListener(v -> mapView.getController().zoomIn());
        btnZoomOut.setOnClickListener(v -> mapView.getController().zoomOut());
        btnCenter.setOnClickListener(v -> {
            if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(myLocationOverlay.getMyLocation());
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!PermissionUtils.hasLocationPermission(this)) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            initializeLocationFeatures();
            loadAndDisplayWaypoints();
        }
    }

    private void initializeLocationFeatures() {
        // Setup location provider
        GpsMyLocationProvider provider = new GpsMyLocationProvider(getApplicationContext());
        provider.setLocationUpdateMinDistance(5);
        provider.setLocationUpdateMinTime(3000);

        // Initialize location overlay
        myLocationOverlay = new MyLocationNewOverlay(provider, mapView);
        myLocationOverlay.setDrawAccuracyEnabled(true);
        mapView.getOverlays().add(myLocationOverlay);

        // Center on current location if available
        if (myLocationOverlay.getMyLocation() != null) {
            mapView.getController().setCenter(myLocationOverlay.getMyLocation());
            mapView.getController().setZoom(15.0);
        }

        // Set up location listener
        myLocationOverlay.runOnFirstFix(() -> {
            if (isWaitingForLocation) {
                runOnUiThread(() -> {
                    currentLocation = myLocationOverlay.getMyLocation();
                    if (currentLocation != null) {
                        recordWaypoint();
                    }
                });
            }
        });
    }

    private void loadLastWaypointId() {
        try {
            if (waypointsFile.exists()) {
                String content = new String(Files.readAllBytes(Paths.get(waypointsFile.getAbsolutePath())),
                        StandardCharsets.UTF_8);
                String[] lines = content.split("\n");
                if (lines.length > 0) {
                    String lastLine = lines[lines.length - 1];
                    String[] parts = lastLine.split(",");
                    if (parts.length > 0) {
                        waypointId = Integer.parseInt(parts[0]) + 1;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAndDisplayWaypoints() {
        try {
            if (waypointsFile.exists()) {
                String content = new String(Files.readAllBytes(Paths.get(waypointsFile.getAbsolutePath())),
                        StandardCharsets.UTF_8);
                String[] lines = content.split("\n");

                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        double lat = Double.parseDouble(parts[1]);
                        double lon = Double.parseDouble(parts[2]);
                        String desc = parts.length > 4 ? parts[4] : "";

                        GeoPoint point = new GeoPoint(lat, lon);
                        addWaypointMarker(point, desc);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recordWaypoint() {
        isWaitingForLocation = false;

        if (currentLocation == null) {
            Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etDescription.getText().toString().trim().toUpperCase();
        if (description.length() != 5) {
            Toast.makeText(this, "ID must be 5 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        String waypointData = String.format(Locale.US, "%d,%.8f,%.8f,%.2f,%s,%s\n",
                waypointId++,
                currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                currentLocation.getAltitude(),
                description,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        try {
            Files.write(Paths.get(waypointsFile.getAbsolutePath()),
                    waypointData.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            addWaypointMarker(currentLocation, description);
            Toast.makeText(this, "Waypoint recorded", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(this, "Error saving waypoint", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void addWaypointMarker(GeoPoint point, String description) {
        runOnUiThread(() -> {
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(description);
            mapView.getOverlays().add(marker);
            mapView.invalidate();
        });
    }

    private void showEditFragment() {
        WaypointsEditFragment editFragment = new WaypointsEditFragment();
        editFragment.setOnSaveListener(() -> {
            mapView.getOverlays().clear();
            if (myLocationOverlay != null) {
                mapView.getOverlays().add(myLocationOverlay);
            }
            loadAndDisplayWaypoints();
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapView, editFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        // Reload tiles when activity resumes
        mapView.setTileSource(TileSourceFactory.MAPNIK);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
