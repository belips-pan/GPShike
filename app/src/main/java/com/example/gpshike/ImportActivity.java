package com.example.gpshike;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImportActivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST = 1;
    private TextView tvStatus;
    private String importType = ""; // "waypoints" or "tracks"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_activity);

        Button btnBack = findViewById(R.id.btnBack);
        Button btnImportWaypoints = findViewById(R.id.btnImportWaypoints);
        Button btnImportTracks = findViewById(R.id.btnImportTracks);
        tvStatus = findViewById(R.id.tvStatus);

        btnBack.setOnClickListener(v -> finish());

        btnImportWaypoints.setOnClickListener(v -> {
            importType = "waypoints";
            showFilePicker();
        });

        btnImportTracks.setOnClickListener(v -> {
            importType = "tracks";
            showFilePicker();
        });
    }

    private void showFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, FILE_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                importFile(uri);
            }
        }
    }

    private void importFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                tvStatus.setText("Error opening file");
                return;
            }

            File targetFile;
            if (importType.equals("waypoints")) {
                targetFile = new File(getExternalFilesDir(null), "waypoints_cache.txt");
            } else {
                targetFile = new File(getExternalFilesDir(null), "tracks_cache.txt");
            }

            try (OutputStream out = Files.newOutputStream(targetFile.toPath())) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
            inputStream.close();

            tvStatus.setText("Successfully imported " + importType);
            Toast.makeText(this, importType + " imported successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            tvStatus.setText("Import failed: " + e.getMessage());
            Toast.makeText(this, "Import failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}