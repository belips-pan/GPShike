package com.example.gpshike;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ExportActivity extends AppCompatActivity {

    private TextView tvStatus;
    private String exportType = ""; // "waypoints" or "tracks"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_activity);

        Button btnBack = findViewById(R.id.btnBack);
        Button btnExportWaypoints = findViewById(R.id.btnExportWaypoints);
        Button btnExportTracks = findViewById(R.id.btnExportTracks);
        tvStatus = findViewById(R.id.tvStatus);

        btnBack.setOnClickListener(v -> finish());

        btnExportWaypoints.setOnClickListener(v -> {
            exportType = "waypoints";
            exportFile();
        });

        btnExportTracks.setOnClickListener(v -> {
            exportType = "tracks";
            exportFile();
        });
    }

    private void exportFile() {
        try {
            File sourceFile;
            if (exportType.equals("waypoints")) {
                sourceFile = new File(getExternalFilesDir(null), "waypoints_cache.txt");
            } else {
                sourceFile = new File(getExternalFilesDir(null), "tracks_cache.txt");
            }

            if (!sourceFile.exists()) {
                tvStatus.setText("No " + exportType + " data to export");
                Toast.makeText(this, "No " + exportType + " data found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a content URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    sourceFile);

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Exported " + exportType);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Here are the exported " + exportType);

            // Start the share activity
            startActivity(Intent.createChooser(shareIntent, "Export " + exportType));

            tvStatus.setText(exportType + " exported successfully");
            Toast.makeText(this, exportType + " exported", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            tvStatus.setText("Export failed: " + e.getMessage());
            Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}