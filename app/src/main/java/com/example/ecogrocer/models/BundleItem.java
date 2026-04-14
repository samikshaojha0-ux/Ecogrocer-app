package com.example.ecogrocer.models;

import java.io.Serializable;

public class BundleItem implements Serializable {
    private String name;
    private String quantity;
    private double price;

    public BundleItem() {}

    public BundleItem(String name, String quantity, double price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
