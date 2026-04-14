package com.example.ecogrocer.utils;

import com.example.ecogrocer.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    private static FirebaseHelper instance;
    private final FirebaseAuth mAuth;
    private final DatabaseReference mDatabase;
    private static final String USERS_NODE = "users";

    private FirebaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public DatabaseReference getDatabase() {
        return mDatabase;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return mAuth.getCurrentUser() != null;
    }

    public void signOut() {
        mAuth.signOut();
    }

    /**
     * Create a new user node in Realtime Database
     */
    public void createUserDocument(User user, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        mDatabase.child(USERS_NODE)
                .child(user.getUserId())
                .setValue(user)
                .addOnSuccessListener(onSuccessListener -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    /**
     * Get user data from Realtime Database
     */
    public void getUserDocument(String userId, final OnUserFetchListener listener) {
        mDatabase.child(USERS_NODE)
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            listener.onSuccess(user);
                        } else {
                            listener.onSuccess(null);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onFailure(databaseError.toException());
                    }
                });
    }

    /**
     * Update user profile fields
     */
    public void updateUserProfile(String userId, Map<String, Object> updates,
                                   OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        mDatabase.child(USERS_NODE)
                .child(userId)
                .updateChildren(updates)
                .addOnSuccessListener(onSuccessListener -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    /**
     * Update EcoCoins for a user
     */
    public void updateEcoCoins(String userId, int newCoinCount,
                                OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        mDatabase.child(USERS_NODE)
                .child(userId)
                .child("ecoCoins")
                .setValue(newCoinCount)
                .addOnSuccessListener(onSuccessListener -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    /**
     * Update Carbon Saved for a user
     */
    public void incrementCarbonSaved(String userId, double addedCarbon,
                                      OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        getUserDocument(userId, new OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    double newCarbon = user.getCarbonSaved() + addedCarbon;
                    mDatabase.child(USERS_NODE)
                            .child(userId)
                            .child("carbonSaved")
                            .setValue(newCarbon)
                            .addOnSuccessListener(onSuccessListener -> onSuccess.onSuccess(null))
                            .addOnFailureListener(onFailure);
                } else {
                    onFailure.onFailure(new Exception("User not found"));
                }
            }

            @Override
            public void onFailure(Exception e) {
                onFailure.onFailure(e);
            }
        });
    }

    /**
     * Place a new order
     */
    public void placeOrder(com.example.ecogrocer.models.Order order, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        // Updated to save at a flat orders node for easier delivery boy tracking
        mDatabase.child("orders")
                .child(order.getOrderId())
                .setValue(order)
                .addOnSuccessListener(onSuccessListener -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    /**
     * Update order status
     */
    public void updateOrderStatus(String orderId, String status) {
        mDatabase.child("orders").child(orderId).child("status").setValue(status);
    }

    /**
     * Get all orders to calculate Apriori recommendations
     */
    public void getAllOrders(final ValueEventListener listener) {
        mDatabase.child("orders").addListenerForSingleValueEvent(listener);
    }

    /**
     * Save/Update products in Firebase
     */
    public void saveProducts(java.util.List<com.example.ecogrocer.models.Product> products, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        mDatabase.child("products").setValue(products)
                .addOnSuccessListener(onSuccessListener -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    /**
     * Get products from Firebase
     */
    public void getProducts(final OnProductsFetchListener listener) {
        mDatabase.child("products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    java.util.List<com.example.ecogrocer.models.Product> products = new java.util.ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        products.add(snapshot.getValue(com.example.ecogrocer.models.Product.class));
                    }
                    listener.onSuccess(products);
                } else {
                    listener.onSuccess(new java.util.ArrayList<>());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    public interface OnProductsFetchListener {
        void onSuccess(java.util.List<com.example.ecogrocer.models.Product> products);
        void onFailure(Exception e);
    }

    /**
     * Save/Update hubs in Firebase
     */
    public void saveHubs(java.util.List<com.example.ecogrocer.models.Hub> hubs, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        mDatabase.child("hubs").setValue(hubs)
                .addOnSuccessListener(onSuccessListener -> onSuccess.onSuccess(null))
                .addOnFailureListener(onFailure);
    }

    /**
     * Get hubs from Firebase
     */
    public void getHubs(final OnHubsFetchListener listener) {
        mDatabase.child("hubs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    java.util.List<com.example.ecogrocer.models.Hub> hubs = new java.util.ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        hubs.add(snapshot.getValue(com.example.ecogrocer.models.Hub.class));
                    }
                    listener.onSuccess(hubs);
                } else {
                    listener.onSuccess(new java.util.ArrayList<>());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                listener.onFailure(databaseError.toException());
            }
        });
    }

    public interface OnHubsFetchListener {
        void onSuccess(java.util.List<com.example.ecogrocer.models.Hub> hubs);
        void onFailure(Exception e);
    }

    /**
     * Seed initial transactions for Apriori Recommendation
     */
    public void seedHistoricalTransactions() {
        android.util.Log.d("FirebaseHelper", "Seeding transactions...");
        String[][] rawData = {
            {"7", "52", "41"}, {"12", "13", "19"}, {"44", "45", "205", "43"},
            {"33", "35"}, {"63", "45", "59", "59"}, {"12", "20", "51"},
            {"48", "44", "45"}, {"7", "59", "59"}, {"33", "35", "52"},
            {"12", "13", "19"}, {"44", "50", "66"}, {"75", "19", "44"},
            {"45", "205", "63"}, {"35", "52", "43"}, {"12", "13", "20"},
            {"12", "13", "44", "205"}, {"45", "205", "43", "51"}, {"63", "45", "44"},
            {"7", "52", "47"}, {"35", "43", "52"}, {"12", "13", "20"},
            {"75", "44", "45"}, {"48", "51", "43"}, {"50", "66", "44"},
            {"59", "61", "59"}, {"7", "52", "59"}, {"12", "13", "19", "51"},
            {"45", "205", "63", "66"}, {"35", "52", "47"}, {"44", "205", "45", "63"},
            {"12", "13", "75"}, {"33", "45", "63"}, {"35", "52", "43"},
            {"44", "66", "59"}, {"45", "51", "205"}, {"12", "20", "13"},
            {"7", "59", "52"}, {"48", "44", "205"}, {"35", "43", "52", "47"},
            {"75", "19", "44", "205"}
        };

        getProducts(new OnProductsFetchListener() {
            @Override
            public void onSuccess(java.util.List<com.example.ecogrocer.models.Product> products) {
                for (String[] ids : rawData) {
                    java.util.List<com.example.ecogrocer.models.CartItem> items = new java.util.ArrayList<>();
                    for (String idStr : ids) {
                        int id = Integer.parseInt(idStr);
                        for (com.example.ecogrocer.models.Product p : products) {
                            if (p.getId() == id) {
                                items.add(new com.example.ecogrocer.models.CartItem(p, 1));
                                break;
                            }
                        }
                    }
                    if (!items.isEmpty()) {
                        String orderId = mDatabase.child("orders").push().getKey();
                        com.example.ecogrocer.models.Order order = new com.example.ecogrocer.models.Order();
                        order.setOrderId(orderId);
                        order.setItems(items);
                        order.setStatus("Delivered");
                        mDatabase.child("orders").child(orderId).setValue(order);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {}
        });
    }

    /**
     * Logic for redeeming a product using EcoCoins
     */
    public void redeemProduct(String userId, int cost, com.example.ecogrocer.models.Product product, final OnRedeemListener listener) {
        getUserDocument(userId, new OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getEcoCoins() >= cost) {
                    // Update coins
                    int remainingCoins = user.getEcoCoins() - cost;
                    updateEcoCoins(userId, remainingCoins, unused -> {
                        // Place order
                        String orderId = "REDEEM-" + System.currentTimeMillis();
                        java.util.List<com.example.ecogrocer.models.CartItem> items = new java.util.ArrayList<>();
                        items.add(new com.example.ecogrocer.models.CartItem(product, 1));
                        
                        com.example.ecogrocer.models.Order order = new com.example.ecogrocer.models.Order(
                                orderId, userId, items, 0, 0, 0, "Order placed (Redeemed)", user.getAddress(), "EcoStore", System.currentTimeMillis(), "EcoCoins Redemption"
                        );
                        
                        placeOrder(order, success -> listener.onSuccess(), failure -> listener.onFailure(failure));
                    }, failure -> listener.onFailure(failure));
                } else {
                    listener.onInsufficientCoins();
                }
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
            }
        });
    }

    public interface OnRedeemListener {
        void onSuccess();
        void onInsufficientCoins();
        void onFailure(Exception e);
    }

    public interface OnUserFetchListener {
        void onSuccess(com.example.ecogrocer.models.User user);
        void onFailure(Exception e);
    }
}
