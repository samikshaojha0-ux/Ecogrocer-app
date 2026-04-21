package com.example.ecogrocer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecogrocer.utils.FirebaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RewardsActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private TextView tvCoinCount, tvNextReward;
    private ProgressBar progressReward;
    private FirebaseHelper firebaseHelper;
    private int currentCoins = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rewards);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
        setupBottomNav();
        loadEcoCoins();
        setupRedeemButtons();
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bottom_nav);
        tvCoinCount = findViewById(R.id.tv_coin_count);
        tvNextReward = findViewById(R.id.tv_next_reward);
        progressReward = findViewById(R.id.progress_reward);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Waste Pickup button
        findViewById(R.id.btn_request_pickup).setOnClickListener(v -> {
            startActivity(new Intent(this, RequestPickupActivity.class));
        });

        // View Wallet button
        findViewById(R.id.btn_view_wallet).setOnClickListener(v -> {
            startActivity(new Intent(this, WalletActivity.class));
        });

        // View Leaderboard button
        findViewById(R.id.btn_view_leaderboard).setOnClickListener(v -> {
            startActivity(new Intent(this, LeaderboardActivity.class));
        });
    }

    private void loadEcoCoins() {
        if (firebaseHelper.getCurrentUser() != null) {
            String userId = firebaseHelper.getCurrentUser().getUid();
            firebaseHelper.getUserDocument(userId, new FirebaseHelper.OnUserFetchListener() {
                @Override
                public void onSuccess(com.example.ecogrocer.models.User user) {
                    if (user != null) {
                        currentCoins = user.getEcoCoins();
                        updateCoinDisplay();
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    currentCoins = 0;
                    updateCoinDisplay();
                }
            });
        }
    }

    private void updateCoinDisplay() {
        tvCoinCount.setText(String.valueOf(currentCoins));
        int nextRewardAt = 150; // First reward threshold
        int remaining = Math.max(0, nextRewardAt - currentCoins);
        tvNextReward.setText(String.format(getString(R.string.ecocoins_to_next_reward), remaining));

        // Progress: max 170 (as shown in design)
        progressReward.setMax(170);
        progressReward.setProgress(Math.min(currentCoins, 170));
    }

    private void setupRedeemButtons() {
        // ₹100 Off Groceries - 150 coins
        findViewById(R.id.btn_redeem_1).setOnClickListener(v -> {
            if (currentCoins >= 150) {
                redeemReward(150, "₹100 Off Groceries");
            } else {
                Toast.makeText(this, "Not enough EcoCoins!", Toast.LENGTH_SHORT).show();
            }
        });

        // Sustainability Kit - 300 coins
        findViewById(R.id.btn_redeem_3).setOnClickListener(v -> {
            if (currentCoins >= 300) {
                redeemReward(300, "Sustainability Kit");
            } else {
                Toast.makeText(this, "Not enough EcoCoins!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redeemReward(int cost, String rewardName) {
        if (firebaseHelper.getCurrentUser() != null) {
            String userId = firebaseHelper.getCurrentUser().getUid();
            int newBalance = currentCoins - cost;
            firebaseHelper.updateEcoCoins(userId, newBalance,
                    aVoid -> {
                        currentCoins = newBalance;
                        updateCoinDisplay();
                        
                        // Record transaction
                        String transId = firebaseHelper.getDatabase().child("transactions").push().getKey();
                        com.example.ecogrocer.models.EcoCoinTransaction transaction = new com.example.ecogrocer.models.EcoCoinTransaction(
                                transId, userId, cost, "Redeemed", "Redeemed: " + rewardName, System.currentTimeMillis()
                        );
                        firebaseHelper.getDatabase().child("transactions").child(transId).setValue(transaction);

                        Toast.makeText(this, rewardName + " redeemed!", Toast.LENGTH_SHORT).show();
                    },
                    e -> Toast.makeText(this, "Failed to redeem. Try again.", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_rewards);
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
                startActivity(new Intent(this, PeelIdentifierActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_rewards) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_rewards);
        loadEcoCoins();
    }
}
