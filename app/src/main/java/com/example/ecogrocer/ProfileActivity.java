package com.example.ecogrocer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecogrocer.utils.FirebaseHelper;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmail, tvPhone, tvAddress, tvEcoCoins, tvCarbonSaved;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
        loadUserProfile();
    }

    private void initViews() {
        tvName = findViewById(R.id.tv_profile_name);
        tvEmail = findViewById(R.id.tv_profile_email);
        tvPhone = findViewById(R.id.tv_profile_phone);
        tvAddress = findViewById(R.id.tv_profile_address);
        tvEcoCoins = findViewById(R.id.tv_profile_ecocoins);
        tvCarbonSaved = findViewById(R.id.tv_profile_carbon_saved);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Edit Profile
        findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        // Order History
        findViewById(R.id.card_order_history).setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));

        // Logout
        findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserProfile() {
        if (firebaseHelper.getCurrentUser() != null) {
            String userId = firebaseHelper.getCurrentUser().getUid();
            firebaseHelper.getUserDocument(userId, new FirebaseHelper.OnUserFetchListener() {
                @Override
                public void onSuccess(com.example.ecogrocer.models.User user) {
                    if (user != null) {
                        if (user.getName() != null) tvName.setText(user.getName());
                        if (user.getEmail() != null) tvEmail.setText(user.getEmail());
                        if (user.getPhone() != null) tvPhone.setText(user.getPhone());
                        if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                            tvAddress.setText(user.getAddress());
                        } else {
                            tvAddress.setText("Not set");
                        }
                        tvEcoCoins.setText(String.valueOf(user.getEcoCoins()));
                        
                        // Format carbon saved to 2 decimal places
                        if (tvCarbonSaved != null) {
                            tvCarbonSaved.setText(String.format("%.2f kg", user.getCarbonSaved()));
                        }
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_confirm))
                .setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    firebaseHelper.signOut();
                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(getString(R.string.no), null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }
}
