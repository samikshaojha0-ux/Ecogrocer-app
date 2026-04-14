package com.example.ecogrocer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecogrocer.models.CartItem;
import com.example.ecogrocer.models.Order;
import com.example.ecogrocer.models.Product;
import com.example.ecogrocer.utils.AprioriHelper;
import com.example.ecogrocer.utils.CartManager;
import com.example.ecogrocer.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductDetailActivity extends AppCompatActivity implements ProductAdapter.OnCartUpdateListener {

    private Product product;
    private ImageView ivProduct;
    private TextView tvName, tvSize, tvPrice, tvOldPrice, tvQty;
    private Button btnAdd;
    private LinearLayout layoutQty, layoutRecommendations;
    private RecyclerView rvRecommendations;
    private ProductAdapter recommendationsAdapter;
    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        product = (Product) getIntent().getSerializableExtra("product");
        if (product == null) {
            finish();
            return;
        }

        cartManager = CartManager.getInstance(this);
        initViews();
        setupData();
        loadRecommendations();
    }

    private void initViews() {
        ivProduct = findViewById(R.id.iv_product);
        tvName = findViewById(R.id.tv_name);
        tvSize = findViewById(R.id.tv_size);
        tvPrice = findViewById(R.id.tv_price);
        tvOldPrice = findViewById(R.id.tv_old_price);
        tvQty = findViewById(R.id.tv_qty);
        btnAdd = findViewById(R.id.btn_add);
        layoutQty = findViewById(R.id.layout_qty);
        layoutRecommendations = findViewById(R.id.layout_recommendations);
        rvRecommendations = findViewById(R.id.rv_recommendations);

        findViewById(R.id.toolbar).setOnClickListener(v -> finish());
        
        btnAdd.setOnClickListener(v -> {
            cartManager.addToCart(product);
            updateCartUI();
        });

        findViewById(R.id.btn_plus).setOnClickListener(v -> {
            cartManager.addToCart(product);
            updateCartUI();
        });

        findViewById(R.id.btn_minus).setOnClickListener(v -> {
            cartManager.removeFromCart(product.getId());
            updateCartUI();
        });
    }

    private void setupData() {
        tvName.setText(product.getName());
        tvSize.setText(product.getSize());
        tvPrice.setText("₹" + (int)product.getPrice());
        if (product.getOldPrice() != null) {
            tvOldPrice.setText("₹" + product.getOldPrice().intValue());
            tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvOldPrice.setVisibility(View.GONE);
        }

        Glide.with(this).load(product.getImage()).into(ivProduct);
        updateCartUI();
    }

    private void updateCartUI() {
        int qty = cartManager.getItemQuantity(product.getId());
        if (qty > 0) {
            btnAdd.setVisibility(View.GONE);
            layoutQty.setVisibility(View.VISIBLE);
            tvQty.setText(String.valueOf(qty));
        } else {
            btnAdd.setVisibility(View.VISIBLE);
            layoutQty.setVisibility(View.GONE);
        }
    }

    private void loadRecommendations() {
        FirebaseHelper.getInstance().getAllOrders(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<List<String>> transactions = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Order order = ds.getValue(Order.class);
                    if (order != null && order.getItems() != null) {
                        List<String> items = new ArrayList<>();
                        for (CartItem item : order.getItems()) {
                            if (item.getProduct() != null) {
                                items.add(String.valueOf(item.getProduct().getId()));
                            }
                        }
                        transactions.add(items);
                    }
                }

                List<AprioriHelper.AssociationRule> rules = AprioriHelper.generateRules(transactions, 0.05, 0.2);
                Set<String> current = new HashSet<>();
                current.add(String.valueOf(product.getId()));
                List<String> recIds = AprioriHelper.getRecommendations(current, rules);

                if (!recIds.isEmpty()) {
                    FirebaseHelper.getInstance().getProducts(new FirebaseHelper.OnProductsFetchListener() {
                        @Override
                        public void onSuccess(List<Product> allProducts) {
                            List<Product> recProducts = new ArrayList<>();
                            for (Product p : allProducts) {
                                if (recIds.contains(String.valueOf(p.getId()))) {
                                    recProducts.add(p);
                                }
                            }

                            if (!recProducts.isEmpty()) {
                                runOnUiThread(() -> {
                                    layoutRecommendations.setVisibility(View.VISIBLE);
                                    rvRecommendations.setLayoutManager(new LinearLayoutManager(ProductDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
                                    recommendationsAdapter = new ProductAdapter(ProductDetailActivity.this, recProducts, ProductDetailActivity.this);
                                    rvRecommendations.setAdapter(recommendationsAdapter);
                                });
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {}
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onCartUpdated() {
        updateCartUI();
    }
}
