// **************************
// * Panagiotis Beligiannis *
// **************************

package com.example.gpshike;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import java.util.HashMap;
import java.util.Map;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_activity);

        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Use OpenStreetMap tiles
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);

        // Set initial view to Greece
        mapView.getController().setZoom(7.0);
        mapView.getController().setCenter(new GeoPoint(39.0742, 21.8243)); // Centered on Greece

        // Request permissions
        requestPermissionsIfNecessary();

        // Initialize location overlay
        myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Setup buttons
        setupButtons();
    }

    private void setupButtons() {
        // Back button
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Zoom controls
        ImageButton btnZoomIn = findViewById(R.id.btnZoomIn);
        btnZoomIn.setOnClickListener(v -> mapView.getController().zoomIn());

        ImageButton btnZoomOut = findViewById(R.id.btnZoomOut);
        btnZoomOut.setOnClickListener(v -> mapView.getController().zoomOut());

        // My location button
        ImageButton btnMyLocation = findViewById(R.id.btnMyLocation);
        btnMyLocation.setOnClickListener(v -> {
            if (myLocationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(myLocationOverlay.getMyLocation());
            } else {
                Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    private void requestPermissionsIfNecessary() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                myLocationOverlay.enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission needed for full functionality",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Method to add a waypoint marker to the map
    public void addWaypointMarker(GeoPoint point, String title) {
        runOnUiThread(() -> {
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(title);
            mapView.getOverlays().add(marker);
            mapView.invalidate();
        });
    }

    // Method to draw a track on the map
    public void drawTrack(List<GeoPoint> points) {
        runOnUiThread(() -> {
            Polyline line = new Polyline();
            line.setPoints(points);
            line.setColor(0xFF0000FF); // Blue color
            line.setWidth(5.0f);
            mapView.getOverlays().add(line);
            mapView.invalidate();
        });
    }

    // Method to center map on a specific point
    public void centerMapOnPoint(GeoPoint point) {
        runOnUiThread(() -> {
            mapView.getController().animateTo(point);
            mapView.getController().setZoom(15.0);
        });
    }
}
