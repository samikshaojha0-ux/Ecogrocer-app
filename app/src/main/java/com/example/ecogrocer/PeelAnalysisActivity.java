package com.example.ecogrocer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PeelAnalysisActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private android.widget.TextView tvTitle, tvFreshness, tvTip;
    private android.widget.ImageView imgPreview;
    private android.net.Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peel_analysis);

        bottomNav = findViewById(R.id.bottom_nav);
        tvTitle = findViewById(R.id.tv_detection_title);
        tvFreshness = findViewById(R.id.tv_use_desc_1);
        tvTip = findViewById(R.id.tv_use_desc_2);
        imgPreview = findViewById(R.id.img_peel_preview);

        setupBottomNav();

        String uriStr = getIntent().getStringExtra("image_uri");
        if (uriStr != null) {
            imageUri = android.net.Uri.parse(uriStr);
            com.bumptech.glide.Glide.with(this).load(imageUri).into(imgPreview);
            uploadImageToBackend(new java.io.File(imageUri.getPath()));
        }

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Finish button
        findViewById(R.id.btn_finish).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finishAffinity();
        });
    }

    private void uploadImageToBackend(java.io.File file) {
        okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/*"), file);
        okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        com.example.ecogrocer.network.ApiService apiService = com.example.ecogrocer.network.RetrofitClient.getApiService();
        retrofit2.Call<com.example.ecogrocer.models.AnalyzeResponse> call = apiService.uploadImage(body);

        call.enqueue(new retrofit2.Callback<com.example.ecogrocer.models.AnalyzeResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.ecogrocer.models.AnalyzeResponse> call, retrofit2.Response<com.example.ecogrocer.models.AnalyzeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.ecogrocer.models.AnalyzeResponse result = response.body();
                    tvTitle.setText(result.getItem() != null ? result.getItem() + " Detected" : "Peel Detected");
                    tvFreshness.setText("Status: " + result.getStatus() + " (" + result.getFreshness() + "% Fresh)");
                    tvTip.setText(result.getTip());
                    
                    // Award EcoCoins for using the AI feature (Simulation)
                    awardCoins();
                } else {
                    android.widget.Toast.makeText(PeelAnalysisActivity.this, "Analysis failed", android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.ecogrocer.models.AnalyzeResponse> call, Throwable t) {
                android.util.Log.e("API_ERROR", t.getMessage());
                android.widget.Toast.makeText(PeelAnalysisActivity.this, "Network Error: Check if server is running", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void awardCoins() {
        com.example.ecogrocer.utils.FirebaseHelper firebaseHelper = com.example.ecogrocer.utils.FirebaseHelper.getInstance();
        if (firebaseHelper.getCurrentUser() != null) {
            String userId = firebaseHelper.getCurrentUser().getUid();
            firebaseHelper.getUserDocument(userId, new com.example.ecogrocer.utils.FirebaseHelper.OnUserFetchListener() {
                @Override
                public void onSuccess(com.example.ecogrocer.models.User user) {
                    if (user != null) {
                        int newBalance = user.getEcoCoins() + 5; // 5 coins for identifying a peel
                        firebaseHelper.updateEcoCoins(userId, newBalance, aVoid -> {
                            // Record transaction
                            String transId = firebaseHelper.getDatabase().child("transactions").push().getKey();
                            com.example.ecogrocer.models.EcoCoinTransaction transaction = new com.example.ecogrocer.models.EcoCoinTransaction(
                                    transId, userId, 5, "Earned", "AI Peel Analysis Reward", System.currentTimeMillis()
                            );
                            firebaseHelper.getDatabase().child("transactions").child(transId).setValue(transaction);
                            android.widget.Toast.makeText(PeelAnalysisActivity.this, "You earned 5 EcoCoins!", android.widget.Toast.LENGTH_SHORT).show();
                        }, e -> {});
                    }
                }
                @Override
                public void onFailure(Exception e) {}
            });
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
}
