package com.example.ecogrocer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecogrocer.models.Product;
import com.example.ecogrocer.utils.FestivalHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FestivalBundlesActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private FestivalBundleAdapter adapter;
    private List<Product> allFestivalProducts = new ArrayList<>();
    private List<Product> displayedProducts = new ArrayList<>();
    private boolean isAscending = true;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_festival_bundles);

        initViews();
        loadProducts();
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        rvProducts = findViewById(R.id.rv_all_festival_bundles);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        
        adapter = new FestivalBundleAdapter(this, displayedProducts);
        rvProducts.setAdapter(adapter);

        TextView btnSort = findViewById(R.id.btn_sort_price);
        btnSort.setOnClickListener(v -> {
            isAscending = !isAscending;
            btnSort.setText(isAscending ? "Price: Low to High" : "Price: High to Low");
            sortProducts();
        });

        findViewById(R.id.btn_filter_festival).setOnClickListener(v -> showFilterDialog());
    }

    private void loadProducts() {
        try {
            InputStream is = getAssets().open("products.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            List<Product> products = new Gson().fromJson(json, new TypeToken<List<Product>>(){}.getType());
            allFestivalProducts.clear();
            for (Product p : products) {
                if ("Festival Bundles".equals(p.getCategory())) {
                    allFestivalProducts.add(p);
                }
            }
            filterProducts("All");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterProducts(String festival) {
        currentFilter = festival;
        displayedProducts.clear();
        if ("All".equals(festival)) {
            displayedProducts.addAll(allFestivalProducts);
        } else {
            for (Product p : allFestivalProducts) {
                if (festival.equals(p.getSubcategory())) {
                    displayedProducts.add(p);
                }
            }
        }
        sortProducts();
    }

    private void sortProducts() {
        Collections.sort(displayedProducts, (p1, p2) -> {
            if (isAscending) return Double.compare(p1.getPrice(), p2.getPrice());
            else return Double.compare(p2.getPrice(), p1.getPrice());
        });
        adapter.notifyDataSetChanged();
    }

    private void showFilterDialog() {
        String[] festivals = {"All", "Navratri", "Ganpati", "Diwali", "Holi"};
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Filter by Festival");
        builder.setItems(festivals, (dialog, which) -> {
            ((TextView)findViewById(R.id.btn_filter_festival)).setText(festivals[which]);
            filterProducts(festivals[which]);
        });
        builder.show();
    }
}
