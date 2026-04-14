package com.example.ecogrocer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import android.content.pm.PackageManager;
import com.example.ecogrocer.utils.LocationHelper;
import com.example.ecogrocer.utils.MemoryData;
import com.google.android.material.textfield.TextInputLayout;

import com.example.ecogrocer.models.User;
import com.example.ecogrocer.utils.FirebaseHelper;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvLogin;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;
    private LocationHelper locationHelper;
    private EditText etAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseHelper = FirebaseHelper.getInstance();
        locationHelper = new LocationHelper(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etName = findViewById(R.id.et_signup_name);
        etEmail = findViewById(R.id.et_signup_email);
        etPhone = findViewById(R.id.et_signup_phone);
        etPassword = findViewById(R.id.et_signup_password);
        etConfirmPassword = findViewById(R.id.et_signup_confirm_password);
        btnSignUp = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_signup);
        etAddress = findViewById(R.id.et_signup_address);
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> signUpUser());

        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });

        // Location Fetch
        TextInputLayout tilAddress = findViewById(R.id.til_signup_address);
        tilAddress.setEndIconOnClickListener(v -> {
            Toast.makeText(this, "Fetching location...", Toast.LENGTH_SHORT).show();
            locationHelper.checkLocationPermission(new LocationHelper.LocationResultCallback() {
                @Override
                public void onSuccess(String address) {
                    etAddress.setText(address);
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(SignupActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void signUpUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_empty_field));
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_empty_field));
            etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError(getString(R.string.error_empty_field));
            etPhone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError(getString(R.string.error_empty_field));
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError(getString(R.string.error_short_password));
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_password_mismatch));
            etConfirmPassword.requestFocus();
            return;
        }

        showLoading(true);

        // Create user in Firebase Auth
        firebaseHelper.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult().getUser() != null) {
                        String userId = task.getResult().getUser().getUid();

                        // Create user document in Firestore
                        User user = new User(userId, name, email, phone);
                        user.setAddress(address);
                        firebaseHelper.createUserDocument(user,
                                aVoid -> {
                                    // Save locally to MemoryData
                                    MemoryData.savedName(name, SignupActivity.this);
                                    MemoryData.savedPhone(phone, SignupActivity.this);
                                    MemoryData.savedAddress(address, SignupActivity.this);

                                    showLoading(false);
                                    Toast.makeText(SignupActivity.this,
                                            getString(R.string.success_account_created),
                                            Toast.LENGTH_SHORT).show();

                                    // Navigate to OTP verification
                                    Intent intent = new Intent(SignupActivity.this, OtpVerificationActivity.class);
                                    intent.putExtra("phone", phone);
                                    startActivity(intent);
                                    finishAffinity();
                                },
                                e -> {
                                    showLoading(false);
                                    Toast.makeText(SignupActivity.this,
                                            getString(R.string.error_signup_failed) + ": " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        showLoading(false);
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : getString(R.string.error_signup_failed);
                        Toast.makeText(SignupActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!show);
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
                        Toast.makeText(SignupActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Permission denied. Cannot fetch location.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
