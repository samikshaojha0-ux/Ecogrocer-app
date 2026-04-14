package com.example.ecogrocer.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private int ecoCoins;
    private double carbonSaved;
    private String address;
    private List<String> orderHistory;

    // Default constructor required for Firestore
    public User() {
        this.ecoCoins = 0;
        this.carbonSaved = 0.0;
        this.address = "";
        this.orderHistory = new ArrayList<>();
    }

    public User(String userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.ecoCoins = 0;
        this.carbonSaved = 0.0;
        this.address = "";
        this.orderHistory = new ArrayList<>();
    }

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public int getEcoCoins() { return ecoCoins; }
    public double getCarbonSaved() { return carbonSaved; }
    public String getAddress() { return address; }
    public List<String> getOrderHistory() { return orderHistory; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEcoCoins(int ecoCoins) { this.ecoCoins = ecoCoins; }
    public void setCarbonSaved(double carbonSaved) { this.carbonSaved = carbonSaved; }
    public void setAddress(String address) { this.address = address; }
    public void setOrderHistory(List<String> orderHistory) { this.orderHistory = orderHistory; }
}
