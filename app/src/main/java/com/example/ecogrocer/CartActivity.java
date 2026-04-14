package com.example.ecogrocer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecogrocer.models.Order;
import com.example.ecogrocer.utils.FirebaseHelper;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import org.json.JSONObject;

import com.example.ecogrocer.utils.CartManager;
import com.example.ecogrocer.models.CartItem;
import com.example.ecogrocer.models.Product;
import com.example.ecogrocer.utils.AprioriHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartChangeListener, PaymentResultListener, ProductAdapter.OnCartUpdateListener {

    private RecyclerView rvCart;
    private CartAdapter adapter;
    private CartManager cartManager;
    private TextView tvSubtotal, tvGrandTotal, tvBottomTotal;
    private View layoutCartContent;
    private LinearLayout layoutEmptyCart, layoutCheckoutBar;
    private android.widget.RadioGroup rgPaymentMethod;
    private final int DELIVERY_FEE = 25;
    private Order pendingOrder;

    private RecyclerView rvRecommendations;
    private LinearLayout layoutRecommendations;
    private ProductAdapter recommendationsAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> recommendedProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        Checkout.preload(getApplicationContext());
        cartManager = CartManager.getInstance(this);
        loadAllProducts();
        initViews();
        setupRecyclerView();
        setupRecommendationsRecyclerView();
        updateUI();
        generateAprioriRecommendations();
    }

    private void initViews() {
        rvCart = findViewById(R.id.rv_cart);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvGrandTotal = findViewById(R.id.tv_grand_total);
        tvBottomTotal = findViewById(R.id.tv_bottom_total);
        layoutCartContent = findViewById(R.id.layout_cart_content);
        layoutEmptyCart = findViewById(R.id.layout_empty_cart);
        layoutCheckoutBar = findViewById(R.id.layout_checkout_bar);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        rvRecommendations = findViewById(R.id.rv_recommendations);
        layoutRecommendations = findViewById(R.id.layout_recommendations);
        
        Button btnCheckout = findViewById(R.id.btn_checkout);
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_online) {
                btnCheckout.setText("Pay & Place Order");
            } else {
                btnCheckout.setText("Place Order (COD)");
            }
        });
        
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_shop_now).setOnClickListener(v -> finish());
        
        findViewById(R.id.btn_checkout).setOnClickListener(v -> {
            prepareOrder();
        });
    }

    private void prepareOrder() {
        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        if (!firebaseHelper.isLoggedIn()) {
            Toast.makeText(this, "Please login to place order", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseHelper.getCurrentUser().getUid();

        firebaseHelper.getUserDocument(userId, new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(com.example.ecogrocer.models.User user) {
                String userAddress = (user != null && user.getAddress() != null && !user.getAddress().isEmpty())
                        ? user.getAddress()
                        : "Mumbai";

                String orderId = firebaseHelper.getDatabase().child("orders").push().getKey();
                double subtotal = cartManager.getSubtotal();
                double total = subtotal + DELIVERY_FEE;
                
                com.example.ecogrocer.utils.HubSelector hubSelector = new com.example.ecogrocer.utils.HubSelector(CartActivity.this);
                String assignedHub = hubSelector.findClosestHub(userAddress);
                String paymentMethod = rgPaymentMethod.getCheckedRadioButtonId() == R.id.rb_online ? "Online" : "COD";

                com.google.firebase.database.DatabaseReference boysRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("delivery_boys");
                boysRef.orderByChild("assignedHubId").equalTo(assignedHub).limitToFirst(1)
                    .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                            String boyId = "";
                            String boyName = "Searching for EcoBoy...";
                            String boyPhone = "";

                            if (snapshot.exists()) {
                                for (com.google.firebase.database.DataSnapshot ds : snapshot.getChildren()) {
                                    com.example.ecogrocer.models.DeliveryBoy boy = ds.getValue(com.example.ecogrocer.models.DeliveryBoy.class);
                                    if (boy != null) {
                                        boyId = boy.getBoyId();
                                        boyName = boy.getName();
                                        boyPhone = boy.getPhoneNumber();
                                    }
                                }
                            }

                            pendingOrder = new Order(
                                    orderId, userId, cartManager.getCartItems(), subtotal, DELIVERY_FEE, total,
                                    "Order placed", userAddress, assignedHub, System.currentTimeMillis(), paymentMethod
                            );
                            pendingOrder.setAssignedBoyId(boyId);
                            pendingOrder.setAssignedBoyName(boyName);
                            pendingOrder.setAssignedBoyPhone(boyPhone);

                            if (paymentMethod.equals("COD")) {
                                placeProcessedOrder();
                            } else {
                                startPayment(total);
                            }
                        }

                        @Override
                        public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                            // Fallback to basic order
                            pendingOrder = new Order(
                                    orderId, userId, cartManager.getCartItems(), subtotal, DELIVERY_FEE, total,
                                    "Order placed", userAddress, assignedHub, System.currentTimeMillis(), paymentMethod
                            );
                            if (paymentMethod.equals("COD")) placeProcessedOrder();
                            else startPayment(total);
                        }
                    });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(CartActivity.this, "Error fetching profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startPayment(double amount) {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_TpaFRSIneagnvk");

        try {
            JSONObject options = new JSONObject();
            options.put("name", "EcoGrocer");
            options.put("description", "Sustainable Groceries Order");
            options.put("currency", "INR");
            options.put("amount", (int)(amount * 100)); // amount in paise

            JSONObject prefill = new JSONObject();
            prefill.put("email", FirebaseHelper.getInstance().getCurrentUser().getEmail());
            options.put("prefill", prefill);

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Payment error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        placeProcessedOrder();
    }

    private void placeProcessedOrder() {
        if (pendingOrder != null) {
            FirebaseHelper.getInstance().placeOrder(pendingOrder, aVoid -> {
                // Carbon saved calculation: base 1.5kg + 0.1kg per item
                double carbonSaved = 1.5 + (pendingOrder.getItems().size() * 0.1);
                FirebaseHelper.getInstance().incrementCarbonSaved(pendingOrder.getUserId(), carbonSaved, 
                    success -> {}, failure -> {});

                cartManager.clearCart();
                android.content.Intent intent = new android.content.Intent(CartActivity.this, OrderConfirmationActivity.class);
                intent.putExtra("ORDER_ID", pendingOrder.getOrderId());
                startActivity(intent);
                finish();
            }, e -> {
                Toast.makeText(CartActivity.this, "Failed to place order: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment failed! Please try again.", Toast.LENGTH_LONG).show();
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, cartManager.getCartItems(), this);
        rvCart.setAdapter(adapter);
    }

    private void updateUI() {
        if (cartManager.getCartItems().isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            layoutCartContent.setVisibility(View.GONE);
            layoutCheckoutBar.setVisibility(View.GONE);
            layoutRecommendations.setVisibility(View.GONE);
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            layoutCartContent.setVisibility(View.VISIBLE);
            layoutCheckoutBar.setVisibility(View.VISIBLE);

            double subtotal = cartManager.getSubtotal();
            double grandTotal = subtotal + DELIVERY_FEE;

            tvSubtotal.setText("₹" + (int)subtotal);
            tvGrandTotal.setText("₹" + (int)grandTotal);
            tvBottomTotal.setText("₹" + (int)grandTotal);
            
            // Re-evaluate recommendations based on new cart state
            refreshRecommendationsDisplay();
        }
    }

    @Override
    public void onCartChanged() {
        updateUI();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCartUpdated() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateUI();
    }

    private void loadAllProducts() {
        FirebaseHelper.getInstance().getProducts(new FirebaseHelper.OnProductsFetchListener() {
            @Override
            public void onSuccess(List<Product> products) {
                allProducts = products;
                // Once products are loaded, we can refresh recommendation display if rules are already there
                runOnUiThread(() -> refreshRecommendationsDisplay());
            }

            @Override
            public void onFailure(Exception e) {
                // Silently fail or log
            }
        });
    }

    private void setupRecommendationsRecyclerView() {
        rvRecommendations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recommendationsAdapter = new ProductAdapter(this, recommendedProducts, this);
        rvRecommendations.setAdapter(recommendationsAdapter);
    }

    private List<AprioriHelper.AssociationRule> currentRules = new ArrayList<>();

    private void generateAprioriRecommendations() {
        FirebaseHelper.getInstance().getAllOrders(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                List<List<String>> transactions = new ArrayList<>();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null && order.getItems() != null) {
                        List<String> itemsInTxn = new ArrayList<>();
                        for (CartItem item : order.getItems()) {
                            if (item.getProduct() != null) {
                                itemsInTxn.add(String.valueOf(item.getProduct().getId()));
                            }
                        }
                        if (!itemsInTxn.isEmpty()) {
                            transactions.add(itemsInTxn);
                        }
                    }
                }

                // Minimum support ratio 0.1 (10% of orders), min confidence 0.3 (30%)
                // Since this is dummy data, adjust if rules are not triggering
                currentRules = AprioriHelper.generateRules(transactions, 0.05, 0.2);
                
                runOnUiThread(() -> refreshRecommendationsDisplay());
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
            }
        });
    }

    private void refreshRecommendationsDisplay() {
        if (currentRules.isEmpty() || cartManager.getCartItems().isEmpty()) {
            layoutRecommendations.setVisibility(View.GONE);
            return;
        }

        Set<String> currentCartItemIds = new HashSet<>();
        for (CartItem item : cartManager.getCartItems()) {
            currentCartItemIds.add(String.valueOf(item.getProduct().getId()));
        }

        List<String> recommendedIds = AprioriHelper.getRecommendations(currentCartItemIds, currentRules);
        
        recommendedProducts.clear();
        for (Product product : allProducts) {
            if (recommendedIds.contains(String.valueOf(product.getId()))) {
                recommendedProducts.add(product);
            }
        }

        if (recommendedProducts.isEmpty()) {
            layoutRecommendations.setVisibility(View.GONE);
        } else {
            layoutRecommendations.setVisibility(View.VISIBLE);
            recommendationsAdapter.notifyDataSetChanged();
        }
    }
}
