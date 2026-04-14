package com.example.ecogrocer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ecogrocer.utils.FirebaseHelper;
import com.example.ecogrocer.utils.LocationHelper;
import com.example.ecogrocer.utils.MemoryData;
import com.google.android.material.textfield.TextInputLayout;
import androidx.annotation.NonNull;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etPhone, etAddress;
    private Button btnSave;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;
    private LocationHelper locationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        firebaseHelper = FirebaseHelper.getInstance();
        locationHelper = new LocationHelper(this);
        initViews();
        loadCurrentProfile();
    }

    private void initViews() {
        etName = findViewById(R.id.et_edit_name);
        etPhone = findViewById(R.id.et_edit_phone);
        etAddress = findViewById(R.id.et_edit_address);
        btnSave = findViewById(R.id.btn_save);
        progressBar = findViewById(R.id.progress_save);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Save button
        btnSave.setOnClickListener(v -> saveProfile());

        // Location Fetch
        TextInputLayout tilAddress = findViewById(R.id.til_address);
        tilAddress.setEndIconOnClickListener(v -> {
            Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
            locationHelper.checkLocationPermission(new LocationHelper.LocationResultCallback() {
                @Override
                public void onSuccess(String address) {
                    etAddress.setText(address);
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void loadCurrentProfile() {
        // Pre-fill from local cache for speed
        String localName = MemoryData.getName(this);
        String localPhone = MemoryData.getPhone(this);
        String localAddress = MemoryData.getAddress(this);

        if (!localName.isEmpty()) etName.setText(localName);
        if (!localPhone.isEmpty()) etPhone.setText(localPhone);
        if (!localAddress.isEmpty()) etAddress.setText(localAddress);

        if (firebaseHelper.getCurrentUser() != null) {
            String userId = firebaseHelper.getCurrentUser().getUid();
            firebaseHelper.getUserDocument(userId, new FirebaseHelper.OnUserFetchListener() {
                @Override
                public void onSuccess(com.example.ecogrocer.models.User user) {
                    if (user != null) {
                        if (user.getName() != null) etName.setText(user.getName());
                        if (user.getPhone() != null) etPhone.setText(user.getPhone());
                        if (user.getAddress() != null) etAddress.setText(user.getAddress());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(EditProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_empty_field));
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError(getString(R.string.error_empty_field));
            etPhone.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        if (firebaseHelper.getCurrentUser() != null) {
            String userId = firebaseHelper.getCurrentUser().getUid();

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("phone", phone);
            updates.put("address", address);

            firebaseHelper.updateUserProfile(userId, updates,
                    aVoid -> {
                        // Update local cache
                        MemoryData.savedName(name, this);
                        MemoryData.savedPhone(phone, this);
                        MemoryData.savedAddress(address, this);

                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(this, getString(R.string.success_profile_updated),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    e -> {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(this, "Failed to update profile: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.getCurrentLocation(new LocationHelper.LocationResultCallback() {
                    @Override
                    public void onSuccess(String address) {
                        etAddress.setText(address);
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(EditProfileActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Permission denied. Cannot fetch location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
