// **************************
// * Panagiotis Beligiannis *
// **************************

package com.example.gpshike;

import android.location.GpsStatus;
import android.location.LocationManager;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class SatellitesActivity extends AppCompatActivity implements GpsStatus.Listener {

    private RadarChart radarChart;
    private TextView tvSatelliteCount;
    private LocationManager locationManager;
    private List<Satellite> currentSatellites = new ArrayList<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.satellites_activity);

        // Initialize views
        Button btnBack = findViewById(R.id.btnBack);
        radarChart = findViewById(R.id.radarChart);
        tvSatelliteCount = findViewById(R.id.tvSatelliteCount);

        btnBack.setOnClickListener(v -> finish());
        setupRadarChart(radarChart);

        // Initialize Location Manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Check and request permissions
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startSatelliteUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSatelliteUpdates();
            } else {
                tvSatelliteCount.setText("Location permission required");
            }
        }
    }

    private void startSatelliteUpdates() {
        if (locationManager != null) {
            try {
                locationManager.addGpsStatusListener(this);
            } catch (SecurityException e) {
                tvSatelliteCount.setText("Permission error");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationManager != null &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.addGpsStatusListener(this);
            } catch (SecurityException e) {
                // Handle error
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null) {
            locationManager.removeGpsStatusListener(this);
        }
    }

    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                updateSatelliteData();
                break;
        }
    }

    private void updateSatelliteData() {
        if (locationManager == null) return;

        try {
            GpsStatus status = locationManager.getGpsStatus(null);
            currentSatellites.clear();

            // Iterate through all satellites
            for (GpsSatellite satellite : status.getSatellites()) {
                String constellation = getConstellationFromPrn(satellite.getPrn());
                currentSatellites.add(new Satellite(
                        constellation,
                        satellite.getPrn(),
                        satellite.getAzimuth(),
                        satellite.getElevation(),
                        satellite.getSnr()
                ));
            }

            runOnUiThread(this::updateChart);
        } catch (SecurityException e) {
            tvSatelliteCount.setText("Permission error");
        }
    }

    private String getConstellationFromPrn(int prn) {
        // Determine constellation based on PRN number
        if (prn >= 1 && prn <= 32) return "GPS";
        if (prn >= 65 && prn <= 96) return "GLONASS";
        if (prn >= 201 && prn <= 236) return "Galileo";
        if (prn >= 301 && prn <= 336) return "BeiDou";
        return "Unknown";
    }

    private void updateChart() {
        // Group by constellation
        List<Satellite> gpsSatellites = new ArrayList<>();
        List<Satellite> glonassSatellites = new ArrayList<>();
        List<Satellite> galileoSatellites = new ArrayList<>();
        List<Satellite> beidouSatellites = new ArrayList<>();

        for (Satellite sat : currentSatellites) {
            switch (sat.getConstellation()) {
                case "GPS": gpsSatellites.add(sat); break;
                case "GLONASS": glonassSatellites.add(sat); break;
                case "Galileo": galileoSatellites.add(sat); break;
                case "BeiDou": beidouSatellites.add(sat); break;
            }
        }

        // Create datasets
        List<IRadarDataSet> dataSets = new ArrayList<>();
        if (!gpsSatellites.isEmpty()) {
            dataSets.add(createSatelliteDataSet(gpsSatellites, "GPS", Color.BLUE));
        }
        if (!glonassSatellites.isEmpty()) {
            dataSets.add(createSatelliteDataSet(glonassSatellites, "GLONASS", Color.RED));
        }
        if (!galileoSatellites.isEmpty()) {
            dataSets.add(createSatelliteDataSet(galileoSatellites, "Galileo", Color.GREEN));
        }
        if (!beidouSatellites.isEmpty()) {
            dataSets.add(createSatelliteDataSet(beidouSatellites, "BeiDou", Color.MAGENTA));
        }

        // Update chart
        RadarData data = new RadarData(dataSets);
        radarChart.setData(data);
        radarChart.invalidate();

        // Update satellite count
        tvSatelliteCount.setText(String.format("GPS: %d | GLONASS: %d | Galileo: %d | BeiDou: %d",
                gpsSatellites.size(),
                glonassSatellites.size(),
                galileoSatellites.size(),
                beidouSatellites.size()));
    }

    private void setupRadarChart(RadarChart chart) {
        // Basic chart setup
        chart.setBackgroundColor(Color.WHITE);
        chart.getDescription().setEnabled(false);
        chart.setWebLineWidth(1f);
        chart.setWebColor(Color.LTGRAY);
        chart.setWebLineWidthInner(1f);
        chart.setWebColorInner(Color.LTGRAY);
        chart.setWebAlpha(100);

        // XAxis (azimuth)
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(10f);
        xAxis.setXOffset(0f);
        xAxis.setYOffset(0f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
                return directions[(int) value % directions.length];
            }
        });
        xAxis.setTextColor(Color.BLACK);

        // YAxis (elevation)
        YAxis yAxis = chart.getYAxis();
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(90f);
        yAxis.setDrawLabels(false);

        // Disable interactions
        chart.setTouchEnabled(false);
        chart.setRotationEnabled(false);
    }

    private RadarDataSet createSatelliteDataSet(List<Satellite> satellites, String label, int color) {
        List<RadarEntry> entries = new ArrayList<>();

        for (Satellite sat : satellites) {
            // Convert polar coordinates to radar chart coordinates
            float azimuth = sat.getAzimuth(); // 0-360 degrees
            float elevation = 90 - sat.getElevation(); // 0-90 degrees (90 at zenith)

            // Normalize azimuth to 0-7 (for 8 directions)
            float normalizedAzimuth = (azimuth / 45f) % 8;

            // Scale by signal strength (SNR)
            float snrFactor = sat.getSnr() / 50f; // Normalize SNR (assuming max 50)
            entries.add(new RadarEntry(elevation * snrFactor, normalizedAzimuth));
        }

        RadarDataSet set = new RadarDataSet(entries, label);
        set.setColor(color);
        set.setFillColor(color);
        set.setDrawFilled(true);
        set.setFillAlpha(60);
        set.setLineWidth(2f);
        set.setDrawHighlightCircleEnabled(true);
        set.setDrawValues(false);

        return set;
    }

    private static class Satellite {
        private String constellation;
        private int prn;
        private float azimuth;
        private float elevation;
        private float snr;

        public Satellite(String constellation, int prn, float azimuth, float elevation, float snr) {
            this.constellation = constellation;
            this.prn = prn;
            this.azimuth = azimuth;
            this.elevation = elevation;
            this.snr = snr;
        }

        public String getConstellation() { return constellation; }
        public float getAzimuth() { return azimuth; }
        public float getElevation() { return elevation; }
        public float getSnr() { return snr; }
    }
}
