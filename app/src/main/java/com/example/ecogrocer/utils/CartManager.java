package com.example.ecogrocer.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ecogrocer.models.CartItem;
import com.example.ecogrocer.models.Product;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final String PREF_NAME = "EcoGrocerCart";
    private static final String KEY_CART = "cart_items";
    private static CartManager instance;
    private SharedPreferences sharedPreferences;
    private List<CartItem> cartItems;
    private Gson gson;

    private CartManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        loadCart();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context.getApplicationContext());
        }
        return instance;
    }

    private void loadCart() {
        String json = sharedPreferences.getString(KEY_CART, null);
        if (json == null) {
            cartItems = new ArrayList<>();
        } else {
            Type type = new TypeToken<List<CartItem>>() {}.getType();
            cartItems = gson.fromJson(json, type);
        }
    }

    private void saveCart() {
        String json = gson.toJson(cartItems);
        sharedPreferences.edit().putString(KEY_CART, json).apply();
    }

    public void addToCart(Product product) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                item.setQuantity(item.getQuantity() + 1);
                saveCart();
                return;
            }
        }
        cartItems.add(new CartItem(product, 1));
        saveCart();
    }

    public void removeFromCart(int productId) {
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            if (item.getProduct().getId() == productId) {
                if (item.getQuantity() > 1) {
                    item.setQuantity(item.getQuantity() - 1);
                } else {
                    cartItems.remove(i);
                }
                saveCart();
                return;
            }
        }
    }

    public int getItemQuantity(int productId) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == productId) {
                return item.getQuantity();
            }
        }
        return 0;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public int getCartCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public double getSubtotal() {
        double subtotal = 0;
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
        }
        return subtotal;
    }

    public void clearCart() {
        cartItems.clear();
        saveCart();
    }
}
