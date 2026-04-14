package com.example.ecogrocer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import android.view.View;
import com.example.ecogrocer.utils.FirebaseHelper;
import com.example.ecogrocer.utils.FestivalHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private FirebaseHelper firebaseHelper;
    private BottomNavigationView bottomNav;
    private RecyclerView rvEcoStore;
    private EcoStoreAdapter ecoStoreAdapter;
    private List<com.example.ecogrocer.models.Product> ecoStoreProducts = new ArrayList<>();
    private ViewPager2 viewPagerBanners;
    private android.widget.LinearLayout layoutDots;
    private List<BannerAdapter.BannerItem> bannerItems = new ArrayList<>();
    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseHelper = FirebaseHelper.getInstance();
        
        // One-time seeding of historical transactions for recommendation algorithm
        android.content.SharedPreferences prefs = getSharedPreferences("EcoGrocerSettings", MODE_PRIVATE);
        if (!prefs.getBoolean("seed_done", false)) {
            firebaseHelper.seedHistoricalTransactions();
            prefs.edit().putBoolean("seed_done", true).apply();
            android.widget.Toast.makeText(this, "Seeding smart recommendations...", android.widget.Toast.LENGTH_SHORT).show();
        }

        initViews();
        setupBottomNav();
        loadUserData();
        setupBanners();
        setupEcoStore();
        setupFestivalBundles();
        setupCardListeners();
    }

    private void initViews() {
        tvGreeting = findViewById(R.id.tv_greeting);
        bottomNav = findViewById(R.id.bottom_nav);
        rvEcoStore = findViewById(R.id.rv_ecostore_products);
        viewPagerBanners = findViewById(R.id.view_pager_banners);
        layoutDots = findViewById(R.id.layout_dots);
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
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
                startActivity(new Intent(this, RewardsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void loadUserData() {
        if (firebaseHelper.getCurrentUser() != null) {
            String userId = firebaseHelper.getCurrentUser().getUid();
            firebaseHelper.getUserDocument(userId, new FirebaseHelper.OnUserFetchListener() {
                @Override
                public void onSuccess(com.example.ecogrocer.models.User user) {
                    if (user != null && user.getName() != null) {
                        tvGreeting.setText(String.format(getString(R.string.hello_user), user.getName()));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    tvGreeting.setText(String.format(getString(R.string.hello_user), "User"));
                }
            });
        }
    }

    private void setupEcoStore() {
        rvEcoStore.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        loadEcoStoreProducts();
        ecoStoreAdapter = new EcoStoreAdapter(this, ecoStoreProducts);
        rvEcoStore.setAdapter(ecoStoreAdapter);
    }

    private void loadEcoStoreProducts() {
        String json = null;
        try {
            InputStream is = getAssets().open("products.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            List<com.example.ecogrocer.models.Product> allProducts = gson.fromJson(json, new TypeToken<List<com.example.ecogrocer.models.Product>>(){}.getType());
            
            if (allProducts != null) {
                // Shuffle to show different items on home screen
                Collections.shuffle(allProducts);
                ecoStoreProducts.clear();
                // Take a decent sample to "look like a shop" but keep it snappy
                ecoStoreProducts.addAll(allProducts.subList(0, Math.min(allProducts.size(), 50)));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setupBanners() {
        // Add static banner items with actions
        bannerItems.add(new BannerAdapter.BannerItem("Fresh Groceries", "Get 10% off on your first order!", 
                getResources().getColor(R.color.green_primary), R.drawable.ic_grocery, "GROCERY"));
        bannerItems.add(new BannerAdapter.BannerItem("Eco Rewards", "Earn EcoCoins for every sustainable action.", 
                getResources().getColor(R.color.eco_coin_gold), R.drawable.ic_rewards, "ECOSTORE"));
        bannerItems.add(new BannerAdapter.BannerItem("Identify Peels", "Turn kitchen waste into useful compost.", 
                getResources().getColor(R.color.green_accent), R.drawable.ic_camera, "CAMERA"));

        BannerAdapter bannerAdapter = new BannerAdapter(bannerItems, item -> {
            switch(item.action) {
                case "GROCERY":
                    startActivity(new Intent(this, GroceryActivity.class));
                    break;
                case "ECOSTORE":
                    Intent intent = new Intent(this, GroceryActivity.class);
                    intent.putExtra("is_eco_store", true);
                    startActivity(intent);
                    break;
                case "CAMERA":
                    startActivity(new Intent(this, PeelIdentifierActivity.class));
                    break;
            }
        });
        viewPagerBanners.setAdapter(bannerAdapter);

        // Auto-scroll logic
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = (viewPagerBanners.getCurrentItem() + 1) % bannerItems.size();
                viewPagerBanners.setCurrentItem(nextItem);
                sliderHandler.postDelayed(this, 3000);
            }
        };

        // Dot indicator setup
        setupDots(bannerItems.size());
        viewPagerBanners.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);
            }
        });
    }

    private void setupDots(int count) {
        layoutDots.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(24, 24);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            layoutDots.addView(dot);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < layoutDots.getChildCount(); i++) {
            layoutDots.getChildAt(i).setBackgroundResource(
                i == position ? R.drawable.dot_active : R.drawable.dot_inactive
            );
        }
    }

    private void setupCardListeners() {
        // Start Shopping button
        findViewById(R.id.btn_start_shopping).setOnClickListener(v ->
                startActivity(new Intent(this, GroceryActivity.class)));

        // Browse groceries
        findViewById(R.id.btn_browse).setOnClickListener(v ->
                startActivity(new Intent(this, GroceryActivity.class)));

        // Upload Peels
        findViewById(R.id.btn_upload_peels).setOnClickListener(v ->
                startActivity(new Intent(this, PeelIdentifierActivity.class)));

        // EcoStore card
        findViewById(R.id.card_ecostore).setOnClickListener(v -> {
            Intent intent = new Intent(this, GroceryActivity.class);
            intent.putExtra("is_eco_store", true);
            startActivity(intent);
        });

        // View Rewards
        findViewById(R.id.btn_view_rewards).setOnClickListener(v ->
                startActivity(new Intent(this, RewardsActivity.class)));

        // Notification icon
        findViewById(R.id.iv_notification).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_home);
        sliderHandler.postDelayed(sliderRunnable, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    private void setupFestivalBundles() {
        FestivalHelper.Festival festival = FestivalHelper.getCurrentFestival();
        if (festival == FestivalHelper.Festival.NONE) {
            findViewById(R.id.layout_festival_bundles).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.layout_festival_bundles).setVisibility(View.VISIBLE);

        // Setup Banner
        View banner = findViewById(R.id.banner_festival);
        TextView tvBannerText = findViewById(R.id.tv_festival_banner_text);
        tvBannerText.setText(festival.bannerText);
        FestivalHelper.applyFestivalTheme(this, festival, banner);

        // Make banner clickable
        banner.setOnClickListener(v -> {
            startActivity(new Intent(this, FestivalBundlesActivity.class));
        });

        // Setup RecyclerView
        RecyclerView rvFestival = findViewById(R.id.rv_festival_bundles);
        rvFestival.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<com.example.ecogrocer.models.Product> festivalProducts = loadFestivalProducts(festival);
        FestivalBundleAdapter adapter = new FestivalBundleAdapter(this, festivalProducts);
        rvFestival.setAdapter(adapter);

        // View All click
        findViewById(R.id.btn_view_all_festivals).setOnClickListener(v -> {
            startActivity(new Intent(this, FestivalBundlesActivity.class));
        });
        setupFoodBundles();
    }

    private void setupFoodBundles() {
        List<com.example.ecogrocer.models.Product> foodProducts = loadFoodProducts();
        if (foodProducts.isEmpty()) {
            findViewById(R.id.layout_food_bundles).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.layout_food_bundles).setVisibility(View.VISIBLE);

        RecyclerView rvFood = findViewById(R.id.rv_food_bundles);
        rvFood.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        FestivalBundleAdapter adapter = new FestivalBundleAdapter(this, foodProducts);
        rvFood.setAdapter(adapter);

        findViewById(R.id.btn_view_all_food).setOnClickListener(v -> {
            // Reusing the same activity for now or can create a new one
            Intent intent = new Intent(this, FestivalBundlesActivity.class);
            intent.putExtra("category", "Meal Kits");
            startActivity(intent);
        });
    }

    private List<com.example.ecogrocer.models.Product> loadFoodProducts() {
        List<com.example.ecogrocer.models.Product> allProducts = loadAllProducts();
        List<com.example.ecogrocer.models.Product> filtered = new java.util.ArrayList<>();

        if (allProducts != null) {
            for (com.example.ecogrocer.models.Product p : allProducts) {
                if ("Meal Kits".equals(p.getCategory())) {
                    filtered.add(p);
                }
            }
        }
        return filtered;
    }

    private List<com.example.ecogrocer.models.Product> loadAllProducts() {
        try {
            InputStream is = getAssets().open("products.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");

            com.google.gson.Gson gson = new com.google.gson.Gson();
            return gson.fromJson(json, new com.google.gson.reflect.TypeToken<List<com.example.ecogrocer.models.Product>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    private List<com.example.ecogrocer.models.Product> loadFestivalProducts(FestivalHelper.Festival festival) {
        List<com.example.ecogrocer.models.Product> filtered = new ArrayList<>();
        List<com.example.ecogrocer.models.Product> allProducts = loadAllProducts();

        if (allProducts != null) {
            for (com.example.ecogrocer.models.Product p : allProducts) {
                if ("Festival Bundles".equals(p.getCategory())) {
                    // Requirement: Sep/Oct (returning NAVRATRI) should show both Navratri and Ganpati
                    if (festival == FestivalHelper.Festival.NAVRATRI) {
                        if ("Navratri".equals(p.getSubcategory()) || "Ganpati".equals(p.getSubcategory())) {
                            filtered.add(p);
                        }
                    } else if (festival.subcategory.equals(p.getSubcategory())) {
                        filtered.add(p);
                    }
                }
            }
        }
        return filtered;
    }
}
