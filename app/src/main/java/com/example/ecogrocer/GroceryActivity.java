package com.example.ecogrocer;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.view.View;
import android.widget.FrameLayout;
import com.example.ecogrocer.utils.CartManager;
import com.example.ecogrocer.models.Product;
import com.example.ecogrocer.utils.FirebaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GroceryActivity extends AppCompatActivity implements ProductAdapter.OnCartUpdateListener {

    private BottomNavigationView bottomNav;
    private RecyclerView rvProducts;
    private EditText etSearch;
    private ProductAdapter adapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> filteredProducts = new ArrayList<>();
    private TextView tvCartCount, tvHeaderTitle;
    private boolean isEcoStore = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery);
        
        isEcoStore = getIntent().getBooleanExtra("is_eco_store", false);

        initViews();
        setupRecyclerView();
        loadProducts();
        setupBottomNav();
        setupListeners();
        
        // Initial badge update
        updateCartBadge();
    }

    private void initViews() {
        bottomNav = findViewById(R.id.bottom_nav);
        rvProducts = findViewById(R.id.rv_products);
        etSearch = findViewById(R.id.et_search);
        tvCartCount = findViewById(R.id.tv_cart_count);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        tvHeaderTitle = findViewById(R.id.tv_header_title);
        View btnCart = findViewById(R.id.btn_cart);

        if (isEcoStore) {
            tvHeaderTitle.setText(R.string.eco_store);
            btnCart.setVisibility(View.GONE);
        } else {
            tvHeaderTitle.setText(R.string.eco_grocer);
            btnCart.setVisibility(View.VISIBLE);
        }

        // Cart button
        btnCart.setOnClickListener(v -> {
            startActivity(new Intent(this, CartActivity.class));
        });
    }

    private void updateCartBadge() {
        int count = CartManager.getInstance(this).getCartCount();
        if (count > 0) {
            tvCartCount.setVisibility(View.VISIBLE);
            tvCartCount.setText(String.valueOf(count));
        } else {
            tvCartCount.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCartUpdated() {
        updateCartBadge();
    }

    private void setupRecyclerView() {
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, filteredProducts, this);
        adapter.setEcoStore(isEcoStore);
        rvProducts.setAdapter(adapter);
    }

    private void loadProducts() {
        FirebaseHelper.getInstance().getProducts(new FirebaseHelper.OnProductsFetchListener() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products == null || products.isEmpty()) {
                    // One-time sync from assets if Firebase is empty
                    syncProductsFromAssets();
                } else {
                    allProducts = products;
                    runOnUiThread(() -> {
                        // Default selection after loading
                        findViewById(R.id.tab_fruits).performClick();
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(GroceryActivity.this, "Error fetching from Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void syncProductsFromAssets() {
        String json = null;
        try {
            InputStream is = getAssets().open("products.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);

            Gson gson = new Gson();
            allProducts = gson.fromJson(json, new TypeToken<List<Product>>(){}.getType());

            // Upload to Firebase
            FirebaseHelper.getInstance().saveProducts(allProducts, 
                aVoid -> {
                    Toast.makeText(this, "Products synced to Firebase!", Toast.LENGTH_SHORT).show();
                    // Load into UI
                    runOnUiThread(() -> findViewById(R.id.tab_fruits).performClick());
                }, 
                e -> Toast.makeText(this, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );

        } catch (IOException ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Error reading local products", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.nav_grocery);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (id == R.id.nav_grocery) {
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

    private void setupListeners() {
        // Category tabs
        TextView tabFruits = findViewById(R.id.tab_fruits);
        TextView tabVegetables = findViewById(R.id.tab_vegetables);
        TextView tabGrains = findViewById(R.id.tab_grains);
        TextView tabDairy = findViewById(R.id.tab_dairy);
        TextView tabMasala = findViewById(R.id.tab_masala);

        tabFruits.setOnClickListener(v -> {
            selectTab(tabFruits, tabVegetables, tabGrains, tabDairy, tabMasala);
            filterByCategory("Fruits");
        });
        tabVegetables.setOnClickListener(v -> {
            selectTab(tabVegetables, tabFruits, tabGrains, tabDairy, tabMasala);
            filterByCategory("Vegetables");
        });
        tabGrains.setOnClickListener(v -> {
            selectTab(tabGrains, tabFruits, tabVegetables, tabDairy, tabMasala);
            filterByCategory("Grains");
        });
        tabDairy.setOnClickListener(v -> {
            selectTab(tabDairy, tabFruits, tabVegetables, tabGrains, tabMasala);
            filterByCategory("Dairy");
        });
        tabMasala.setOnClickListener(v -> {
            selectTab(tabMasala, tabFruits, tabVegetables, tabGrains, tabDairy);
            filterByCategory("Masala");
        });

        // Search logic
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (allProducts == null) return;

        if (query.isEmpty()) {
            // Restore category filter if search is cleared
            // For now, just show current category or everything
            filterByCategory("Fruits"); // Or whatever was selected
            return;
        }

        filteredProducts.clear();
        String lowerQuery = query.toLowerCase().trim();
        for (Product product : allProducts) {
            if (product.getName() != null && product.getName().toLowerCase().contains(lowerQuery)) {
                filteredProducts.add(product);
            } else if (product.getSubcategory() != null && product.getSubcategory().toLowerCase().contains(lowerQuery)) {
                filteredProducts.add(product);
            }
        }
        adapter.updateList(filteredProducts);
    }

    private void filterByCategory(String type) {
        if (allProducts == null || allProducts.isEmpty()) return;

        filteredProducts.clear();
        for (Product product : allProducts) {
            String sub = product.getSubcategory() != null ? product.getSubcategory().toLowerCase() : "";
            String cat = product.getCategory() != null ? product.getCategory().toLowerCase() : "";
            String name = product.getName() != null ? product.getName().toLowerCase() : "";

            if (type.equals("Fruits") && sub.equals("fruits")) {
                filteredProducts.add(product);
            } else if (type.equals("Vegetables") && sub.equals("vegetables")) {
                filteredProducts.add(product);
            } else if (type.equals("Grains") && 
                    (cat.contains("atta") || sub.contains("rice") || sub.contains("dal") || sub.contains("grains") || cat.contains("rice") || sub.contains("maida") || sub.contains("besan"))) {
                filteredProducts.add(product);
            } else if (type.equals("Dairy") && (cat.contains("dairy") || sub.contains("butter") || sub.contains("cheese") || name.contains("amul") || name.contains("milk"))) {
                filteredProducts.add(product);
            } else if (type.equals("Masala") && ((cat.contains("masala") || sub.contains("masala") || sub.contains("oil") || cat.contains("oil")) && !sub.contains("fruits"))) {
                filteredProducts.add(product);
            }
        }
        adapter.updateList(filteredProducts);
    }

    private void selectTab(TextView selected, TextView... others) {
        selected.setBackgroundResource(R.drawable.bg_chip_selected);
        selected.setTextColor(getResources().getColor(R.color.white));

        for (TextView other : others) {
            other.setBackgroundResource(R.drawable.bg_chip_unselected);
            other.setTextColor(getResources().getColor(R.color.text_primary));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.setSelectedItemId(R.id.nav_grocery);
        updateCartBadge();
    }
}
