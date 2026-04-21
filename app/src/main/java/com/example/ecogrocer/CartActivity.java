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
    private com.google.android.material.textfield.TextInputEditText etPromoCode;
    private android.widget.TextView tvPromoMessage, tvDiscount, tvDeliveryFee;
    private View layoutDiscount;
    private double appliedDiscount = 0;
    private double currentDeliveryFee = 25;
    private final int BASE_DELIVERY_FEE = 25;
    private int currentUserCoins = 0;
    private boolean pointsUsed = false;
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
        
        etPromoCode = findViewById(R.id.et_promo_code);
        tvPromoMessage = findViewById(R.id.tv_promo_message);
        tvDiscount = findViewById(R.id.tv_discount);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        layoutDiscount = findViewById(R.id.layout_discount);
        
        tvUserCoins = findViewById(R.id.tv_user_ecocoins);
        btnUsePoints = findViewById(R.id.btn_use_points);
        
        findViewById(R.id.btn_apply_promo).setOnClickListener(v -> validatePromoCode());
        btnUsePoints.setOnClickListener(v -> handlePointRedemption());
        
        loadUserPoints();
        
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

    private void validatePromoCode() {
        String code = etPromoCode.getText().toString().trim().toUpperCase();
        if (code.isEmpty()) {
            showPromoError("Please enter a code");
            return;
        }

        double subtotal = cartManager.getSubtotal();
        appliedDiscount = 0;
        currentDeliveryFee = BASE_DELIVERY_FEE;

        if (code.equals("FREEDEL")) {
            currentDeliveryFee = 0;
            showPromoSuccess("Free Delivery applied!");
        } else if (code.equals("ECOSAVE")) {
            if (subtotal >= 300) {
                appliedDiscount = 50;
                showPromoSuccess("₹50 discount applied!");
            } else {
                showPromoError("Valid on orders above ₹300");
                return;
            }
        } else if (code.equals("WELCOME10")) {
            appliedDiscount = subtotal * 0.1;
            showPromoSuccess("10% discount applied!");
        } else {
            showPromoError("Invalid promo code");
            return;
        }
        
        updateUI();
    }

    private void showPromoError(String msg) {
        tvPromoMessage.setVisibility(View.VISIBLE);
        tvPromoMessage.setText(msg);
        tvPromoMessage.setTextColor(getResources().getColor(R.color.error));
        appliedDiscount = 0;
        currentDeliveryFee = BASE_DELIVERY_FEE;
        updateUI();
    }

    private void showPromoSuccess(String msg) {
        tvPromoMessage.setVisibility(View.VISIBLE);
        tvPromoMessage.setText(msg);
        tvPromoMessage.setTextColor(getResources().getColor(R.color.green_primary));
    }

    private android.widget.TextView tvUserCoins;
    private com.google.android.material.button.MaterialButton btnUsePoints;

    private void loadUserPoints() {
        FirebaseHelper firebaseHelper = FirebaseHelper.getInstance();
        if (!firebaseHelper.isLoggedIn()) return;

        firebaseHelper.getUserDocument(firebaseHelper.getCurrentUser().getUid(), new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(com.example.ecogrocer.models.User user) {
                if (user != null) {
                    currentUserCoins = user.getEcoCoins();
                    tvUserCoins.setText("Balance: " + currentUserCoins + " EcoPoints");
                    
                    if (currentUserCoins < 200) {
                        btnUsePoints.setEnabled(false);
                        btnUsePoints.setAlpha(0.5f);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {}
        });
    }

    private void handlePointRedemption() {
        if (pointsUsed) {
            pointsUsed = false;
            currentDeliveryFee = BASE_DELIVERY_FEE;
            btnUsePoints.setText("FREE DELIVERY");
            btnUsePoints.setTextColor(getResources().getColor(R.color.green_primary));
        } else {
            if (currentUserCoins >= 200) {
                pointsUsed = true;
                currentDeliveryFee = 0;
                btnUsePoints.setText("POINTS APPLIED");
                btnUsePoints.setTextColor(getResources().getColor(R.color.text_secondary));
                Toast.makeText(this, "200 EcoPoints applied for free delivery!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Insufficient EcoPoints", Toast.LENGTH_SHORT).show();
            }
        }
        updateUI();
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
                double total = subtotal + currentDeliveryFee - appliedDiscount;
                
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
                                    orderId, userId, cartManager.getCartItems(), subtotal, currentDeliveryFee, total,
                                    "Order placed", userAddress, assignedHub, System.currentTimeMillis(), paymentMethod
                            );
                            pendingOrder.setDiscount(appliedDiscount);
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
                                    orderId, userId, cartManager.getCartItems(), subtotal, currentDeliveryFee, total,
                                    "Order placed", userAddress, assignedHub, System.currentTimeMillis(), paymentMethod
                            );
                            pendingOrder.setDiscount(appliedDiscount);
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

                if (pointsUsed) {
                    int remainingCoins = currentUserCoins - 200;
                    FirebaseHelper.getInstance().updateEcoCoins(pendingOrder.getUserId(), remainingCoins,
                        success -> {}, failure -> {});
                }

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
            double grandTotal = subtotal + currentDeliveryFee - appliedDiscount;

            tvSubtotal.setText("₹" + (int)subtotal);
            tvDeliveryFee.setText("₹" + (int)currentDeliveryFee);
            
            if (appliedDiscount > 0) {
                layoutDiscount.setVisibility(View.VISIBLE);
                tvDiscount.setText("-₹" + (int)appliedDiscount);
            } else {
                layoutDiscount.setVisibility(View.GONE);
            }
            
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
