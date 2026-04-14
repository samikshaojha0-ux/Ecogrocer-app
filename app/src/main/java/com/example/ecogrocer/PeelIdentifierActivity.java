package com.example.ecogrocer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.example.ecogrocer.R;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PeelIdentifierActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peel_identifier);

        bottomNav = findViewById(R.id.bottom_nav);
        setupBottomNav();

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Take Photo
        findViewById(R.id.btn_take_photo).setOnClickListener(v -> {
            com.github.dhaval2404.imagepicker.ImagePicker.with(this)
                    .cameraOnly()
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        // Upload Image
        findViewById(R.id.btn_upload_image).setOnClickListener(v -> {
            com.github.dhaval2404.imagepicker.ImagePicker.with(this)
                    .galleryOnly()
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start();
        });

        // Finish
        findViewById(R.id.btn_finish).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finishAffinity();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == android.app.Activity.RESULT_OK) {
            android.net.Uri uri = data.getData();
            if (uri != null) {
                Intent intent = new Intent(this, PeelAnalysisActivity.class);
                intent.putExtra("image_uri", uri.toString());
                startActivity(intent);
            }
        } else if (resultCode == com.github.dhaval2404.imagepicker.ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, com.github.dhaval2404.imagepicker.ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_camera);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_grocery) {
                startActivity(new Intent(this, GroceryActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_camera) {
                return true;
            } else if (id == R.id.nav_rewards) {
                startActivity(new Intent(this, RewardsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_camera);
    }
}
