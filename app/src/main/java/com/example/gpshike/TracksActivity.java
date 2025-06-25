// **************************
// * Panagiotis Beligiannis *
// **************************

package com.example.gpshike;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Color;
import com.example.gpshike.PermissionUtils;
import androidx.appcompat.app.AppCompatActivity;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import android.os.Handler;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.IOException;
import android.graphics.Paint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class TracksActivity extends AppCompatActivity {

    private static final String TRACKS_FILE = "tracks_cache.txt";
    private static final long RECORDING_INTERVAL = 5000; // 5 seconds

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private EditText etTrackName;
    private Button btnRecord;
    private File tracksFile;
    private boolean isRecording = false;
    private int trackPointId = 1;
    private String currentTrackId = "";
    private Handler recordingHandler = new Handler();
    private Polyline currentTrackLine;
    private List<GeoPoint> trackPoints = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracks_activity);

        // Initialize OSMDroid configuration
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE));

        // Initialize views
        etTrackName = findViewById(R.id.etTrackName);
        btnRecord = findViewById(R.id.btnRecord);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnZoomIn = findViewById(R.id.btnZoomIn);
        Button btnZoomOut = findViewById(R.id.btnZoomOut);
        Button btnCenter = findViewById(R.id.btnCenter);
        mapView = findViewById(R.id.mapView);

        // Setup map
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);

        // Setup location overlay
        myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Initialize tracks file
        tracksFile = new File(getExternalFilesDir(null), TRACKS_FILE);

        // Button listeners
        btnBack.setOnClickListener(v -> finish());

        btnRecord.setOnClickListener(v -> toggleRecording());

        btnEdit.setOnClickListener(v -> showEditFragment());

        btnZoomIn.setOnClickListener(v -> mapView.getController().zoomIn());

        btnZoomOut.setOnClickListener(v -> mapView.getController().zoomOut());

        btnCenter.setOnClickListener(v -> centerOnCurrentLocation());

        // Center on last known location
        centerOnCurrentLocation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!PermissionUtils.hasLocationPermission(this)) {
            Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Proceed with location-dependent initialization
            initializeLocationFeatures();
        }
    }

    private void initializeLocationFeatures() {
        // 1. Initialize location provider
        GpsMyLocationProvider locationProvider = new GpsMyLocationProvider(getApplicationContext());
        locationProvider.setLocationUpdateMinDistance(10); // 10 meters minimum between updates
        locationProvider.setLocationUpdateMinTime(5000);   // 5 seconds minimum between updates

        // 2. Setup location overlay
        myLocationOverlay = new MyLocationNewOverlay(locationProvider, mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        myLocationOverlay.setDrawAccuracyEnabled(true);
        mapView.getOverlays().add(myLocationOverlay);

        // 3. Center on current location if available
        if (myLocationOverlay.getMyLocation() != null) {
            mapView.getController().animateTo(myLocationOverlay.getMyLocation());
            mapView.getController().setZoom(18.0); // Close zoom level
        }

        // 4. Setup recording handler (if you have recording functionality)
        if (recordingHandler == null) {
            recordingHandler = new Handler();
        }

        // 5. Initialize track visualization
        currentTrackLine = new Polyline();
        currentTrackLine.getOutlinePaint().setColor(0xFF0000FF); // Blue color
        currentTrackLine.getOutlinePaint().setStrokeWidth(5f);
        mapView.getOverlays().add(currentTrackLine);
    }

    private void centerOnCurrentLocation() {
        if (myLocationOverlay.getMyLocation() != null) {
            mapView.getController().animateTo(myLocationOverlay.getMyLocation());
            mapView.getController().setZoom(15.0);
        } else {
            Toast.makeText(this, "Waiting for GPS signal...", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleRecording() {
        isRecording = !isRecording;

        if (isRecording) {
            btnRecord.setText("Stop Recording");
            btnRecord.setBackgroundColor(Color.RED);
            startRecording();
        } else {
            btnRecord.setText("Start Recording");
            btnRecord.setBackgroundColor(Color.GREEN);
            stopRecording();
        }
    }

    private void startRecording() {
        String trackName = etTrackName.getText().toString().trim().toUpperCase();
        if (trackName.length() != 5) {
            Toast.makeText(this, "Track ID must be 5 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        currentTrackId = trackName;
        isRecording = true;
        btnRecord.setText("Stop Recording");
        etTrackName.setEnabled(false);

        // Initialize new track line
        trackPoints.clear();
        currentTrackLine = new Polyline();
        currentTrackLine.setColor(0xFF0000FF);
        currentTrackLine.setWidth(5.0f);
        mapView.getOverlays().add(currentTrackLine);

        // Start periodic recording
        recordingHandler.postDelayed(recordingRunnable, RECORDING_INTERVAL);
        Toast.makeText(this, "Recording started: " + currentTrackId, Toast.LENGTH_SHORT).show();
    }

    private void stopRecording() {
        isRecording = false;
        btnRecord.setText("Start Recording");
        etTrackName.setEnabled(true);
        recordingHandler.removeCallbacks(recordingRunnable);
        Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show();
    }

    private Runnable recordingRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                recordTrackPoint();
                recordingHandler.postDelayed(this, RECORDING_INTERVAL);
            }
        }
    };

    private void recordTrackPoint() {
        // Location location = myLocationOverlay.getMyLocation();
        GeoPoint location = myLocationOverlay.getMyLocation();
        if (location == null) {
            Toast.makeText(this, "GPS signal lost", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format: num_id,lat,lon,alt,track_name,timestamp
        String trackData = String.format(Locale.US, "%d,%.8f,%.8f,%.2f,%s,%s\n",
                trackPointId++,
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                currentTrackId,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        try {
            Files.write(Paths.get(tracksFile.getAbsolutePath()),
                    trackData.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            // Add point to track visualization
            GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
            trackPoints.add(point);
            currentTrackLine.setPoints(trackPoints);
            mapView.invalidate();

            // Auto-pan if point is outside visible area
            if (!mapView.getBoundingBox().contains(point)) {
                mapView.getController().animateTo(point);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error saving track point", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showEditFragment() {
        TracksEditFragment editFragment = new TracksEditFragment();
        editFragment.setOnSaveListener(() -> {
            // Refresh map when editing is done
            mapView.getOverlays().clear();
            mapView.getOverlays().add(myLocationOverlay);
            loadAndDisplayTracks();
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mapView, editFragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadAndDisplayTracks() {
        try {
            if (tracksFile.exists()) {
                String content = new String(Files.readAllBytes(tracksFile.toPath()), StandardCharsets.UTF_8);
                String[] lines = content.split("\n");

                // Group points by track name
                Map<String, List<GeoPoint>> tracks = new HashMap<>();

                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        double lat = Double.parseDouble(parts[1]);
                        double lon = Double.parseDouble(parts[2]);
                        String trackName = parts[4];

                        if (!tracks.containsKey(trackName)) {
                            tracks.put(trackName, new ArrayList<>());
                        }
                        tracks.get(trackName).add(new GeoPoint(lat, lon));
                    }
                }

                // Draw each track with different color
                int colorIndex = 0;
                int[] colors = {0xFFFF0000, 0xFF00FF00, 0xFF0000FF, 0xFFFFFF00, 0xFFFF00FF};

                for (Map.Entry<String, List<GeoPoint>> entry : tracks.entrySet()) {
                    Polyline line = new Polyline();
                    line.setPoints(entry.getValue());

                    line.setColor(colors[colorIndex % colors.length]);
                    line.setWidth(5.0f);
                    line.setTitle(entry.getKey());
                    mapView.getOverlays().add(line);
                    colorIndex++;
                }
                mapView.invalidate();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        loadAndDisplayTracks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (isRecording) {
            stopRecording();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordingHandler.removeCallbacks(recordingRunnable);
    }
}
