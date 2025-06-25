// **************************
// * Panagiotis Beligiannis *
// **************************

package com.example.gpshike;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TranslateActivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST = 1;
    private TextView tvStatus;
    private File selectedFile;
    private DatumParameters datumParams;
    private ProjectionParameters projParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.translate_activity);

        Button btnBack = findViewById(R.id.btnBack);
        Button btnSelectFile = findViewById(R.id.btnSelectFile);
        Button btnTranslate = findViewById(R.id.btnTranslate);
        tvStatus = findViewById(R.id.tvStatus);

        // Load saved parameters
        datumParams = loadDatumParameters();
        projParams = loadProjectionParameters();

        btnBack.setOnClickListener(v -> finish());

        btnSelectFile.setOnClickListener(v -> showFilePicker());

        btnTranslate.setOnClickListener(v -> {
            if (selectedFile == null) {
                tvStatus.setText("No file selected");
                return;
            }
            if (datumParams == null || !datumParams.isValid()) {
                tvStatus.setText("Invalid datum parameters");
                showDatumWarning();
                return;
            }
            translateFile();
        });
    }

    private void showFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, FILE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                String path = data.getData().getPath();
                selectedFile = new File(path);
                tvStatus.setText("Selected: " + selectedFile.getName());
            }
        }
    }

    private void translateFile() {
        try {
            // Read input file
            List<String> lines = FileUtils.readLines(selectedFile, "UTF-8");
            List<String> xyzLines = new ArrayList<>();
            List<String> enhLines = new ArrayList<>();

            // Process each line
            for (String line : lines) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    // Parse WGS84 coordinates
                    double lat = Double.parseDouble(parts[1]);
                    double lon = Double.parseDouble(parts[2]);
                    double height = parts.length > 3 ? Double.parseDouble(parts[3]) : 0;
                    String desc = parts.length > 4 ? parts[4] : "";

                    // 1. Convert to Cartesian (XYZ)
                    double[] xyz = wgs84ToCartesian(lat, lon, height, datumParams);
                    xyzLines.add(String.format("%s,%.3f,%.3f,%.3f,%s",
                            parts[0], xyz[0], xyz[1], xyz[2], desc));

                    // 2. If projection defined, convert to ENH
                    if (projParams != null && projParams.isValid()) {
                        double[] enh = cartesianToProjection(xyz[0], xyz[1], xyz[2], projParams);
                        enhLines.add(String.format("%s,%.3f,%.3f,%.3f,%s",
                                parts[0], enh[0], enh[1], enh[2], desc));
                    }
                }
            }

            // Write output files
            String baseName = selectedFile.getName().replaceFirst("[.][^.]+$", "");
            File outputDir = getExternalFilesDir(null);

            // Write XYZ file
            File xyzFile = new File(outputDir, baseName + ".XYZ");
            FileUtils.writeLines(xyzFile, xyzLines);

            // Write ENH file if projection available
            if (!enhLines.isEmpty()) {
                File enhFile = new File(outputDir, baseName + ".ENH");
                FileUtils.writeLines(enhFile, enhLines);
                tvStatus.setText("Created " + xyzFile.getName() + " and " + enhFile.getName());
            } else {
                tvStatus.setText("Created " + xyzFile.getName());
            }

            Toast.makeText(this, "Translation complete", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            tvStatus.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double[] wgs84ToCartesian(double lat, double lon, double height, DatumParameters params) {
        // Convert degrees to radians
        double latRad = Math.toRadians(lat);
        double lonRad = Math.toRadians(lon);

        // Calculate ellipsoid parameters
        double a = params.getSemiMajorAxis();
        double f = 1 / params.getInverseFlattening();
        double e2 = 2*f - f*f;

        // Calculate prime vertical radius of curvature
        double N = a / Math.sqrt(1 - e2 * Math.sin(latRad) * Math.sin(latRad));

        // Calculate Cartesian coordinates
        double x = (N + height) * Math.cos(latRad) * Math.cos(lonRad);
        double y = (N + height) * Math.cos(latRad) * Math.sin(lonRad);
        double z = (N*(1 - e2) + height) * Math.sin(latRad);

        // Apply 7-parameter transformation if available
        if (params.hasTransformation()) {
            x += params.getDx();
            y += params.getDy();
            z += params.getDz();
            // Note: Should also apply rotations and scale factor here
        }

        return new double[]{x, y, z};
    }

    private double[] cartesianToProjection(double x, double y, double z, ProjectionParameters params) {
        // Convert to Mercator projection (simplified example)
        double easting = x * params.getScaleFactor();
        double northing = y * params.getScaleFactor();
        double height = z;

        // Note: This is a simplified Mercator projection -
        // real implementation would need proper map projection math
        return new double[]{easting, northing, height};
    }

    private DatumParameters loadDatumParameters() {
        // Load from SharedPreferences or database
        // Return null if no parameters defined
        return new DatumParameters(
                6378137.0,  // a
                298.257223563,  // 1/f
                0, 0, 0, 0, 0, 0, 1.0  // Transformation params
        );
    }

    private ProjectionParameters loadProjectionParameters() {
        // Load from SharedPreferences or database
        // Return null if no projection defined
        return new ProjectionParameters(
                37.0,  // φ0
                23.0,  // λ0
                0.9996  // scale
        );
    }

    private void showDatumWarning() {
        new AlertDialog.Builder(this)
                .setTitle("Datum Required")
                .setMessage("You need to define datum parameters in DatumActivity first")
                .setPositiveButton("OK", null)
                .show();
    }

    // Parameter classes
    private static class DatumParameters {
        private double semiMajorAxis;
        private double inverseFlattening;
        private double dx, dy, dz; // Translations
        private double rx, ry, rz; // Rotations (arc-seconds)
        private double scale; // Scale factor (ppm)

        public DatumParameters(double a, double invF, double dx, double dy, double dz,
                               double rx, double ry, double rz, double scale) {
            this.semiMajorAxis = a;
            this.inverseFlattening = invF;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.rx = rx;
            this.ry = ry;
            this.rz = rz;
            this.scale = scale;
        }

        public boolean isValid() {
            return semiMajorAxis > 0 && inverseFlattening > 0;
        }

        public boolean hasTransformation() {
            return dx != 0 || dy != 0 || dz != 0 || rx != 0 || ry != 0 || rz != 0 || scale != 1.0;
        }

        // Getters
        public double getSemiMajorAxis() { return semiMajorAxis; }
        public double getInverseFlattening() { return inverseFlattening; }
        public double getDx() { return dx; }
        public double getDy() { return dy; }
        public double getDz() { return dz; }
    }

    private static class ProjectionParameters {
        private double lat0, lon0, scaleFactor;

        public ProjectionParameters(double lat0, double lon0, double scaleFactor) {
            this.lat0 = lat0;
            this.lon0 = lon0;
            this.scaleFactor = scaleFactor;
        }

        public boolean isValid() {
            return scaleFactor > 0;
        }

        // Getters
        public double getLat0() { return lat0; }
        public double getLon0() { return lon0; }
        public double getScaleFactor() { return scaleFactor; }
    }
}
