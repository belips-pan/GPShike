package com.example.gpshike;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DatumActivity extends AppCompatActivity {

    private EditText etEllipsoidName, etSemiMajorAxis, etInverseFlattening;
    private EditText etXTranslation, etYTranslation, etZTranslation;
    private EditText etXRotation, etYRotation, etZRotation, etScaleFactor;
    private EditText etProjectionLat, etProjectionLon, etProjectionScale;
    private CheckBox cbUseForTranslation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datum_activity);

        // Initialize all views
        initViews();

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveParameters());
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        // Ellipsoid parameters
        etEllipsoidName = findViewById(R.id.etEllipsoidName);
        etSemiMajorAxis = findViewById(R.id.etSemiMajorAxis);
        etInverseFlattening = findViewById(R.id.etInverseFlattening);

        // 7-Parameter transformation
        etXTranslation = findViewById(R.id.etXTranslation);
        etYTranslation = findViewById(R.id.etYTranslation);
        etZTranslation = findViewById(R.id.etZTranslation);
        etXRotation = findViewById(R.id.etXRotation);
        etYRotation = findViewById(R.id.etYRotation);
        etZRotation = findViewById(R.id.etZRotation);
        etScaleFactor = findViewById(R.id.etScaleFactor);

        // Projection parameters
        etProjectionLat = findViewById(R.id.etProjectionLat);
        etProjectionLon = findViewById(R.id.etProjectionLon);
        etProjectionScale = findViewById(R.id.etProjectionScale);

        // Checkbox
        cbUseForTranslation = findViewById(R.id.cbUseForTranslation);
    }

    private void saveParameters() {
        // Get all values from EditText fields
        String ellipsoidName = etEllipsoidName.getText().toString();
        String semiMajorAxis = etSemiMajorAxis.getText().toString();
        String inverseFlattening = etInverseFlattening.getText().toString();

        String xTrans = etXTranslation.getText().toString();
        String yTrans = etYTranslation.getText().toString();
        String zTrans = etZTranslation.getText().toString();
        String xRot = etXRotation.getText().toString();
        String yRot = etYRotation.getText().toString();
        String zRot = etZRotation.getText().toString();
        String scale = etScaleFactor.getText().toString();

        String projLat = etProjectionLat.getText().toString();
        String projLon = etProjectionLon.getText().toString();
        String projScale = etProjectionScale.getText().toString();

        boolean useForTranslation = cbUseForTranslation.isChecked();

        // Validate required fields
        if (ellipsoidName.isEmpty() || semiMajorAxis.isEmpty() || inverseFlattening.isEmpty()) {
            Toast.makeText(this, "Please fill all ellipsoid parameters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences or database
        // For now, just show a confirmation
        Toast.makeText(this, "Parameters saved" +
                        (useForTranslation ? " (will be used in TranslateActivity)" : ""),
                Toast.LENGTH_LONG).show();

        // Here you would typically save to SharedPreferences or database
        // and pass these parameters to TranslateActivity when needed
    }

    // Helper method to parse double safely
    private double parseDouble(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}