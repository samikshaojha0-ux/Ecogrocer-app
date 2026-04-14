package com.example.ecogrocer.models;

import java.util.List;

public class Product implements java.io.Serializable {
    private int id;
    private String name;
    private String size;
    private double price;
    private Double oldPrice;
    private String delivery;
    private String image;
    private String discount;
    private String category;
    private String subcategory;
    private List<BundleItem> bundleItems;

    public Product() {}

    public Product(int id, String name, String size, double price, Double oldPrice, String delivery, String image, String discount, String category, String subcategory) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.price = price;
        this.oldPrice = oldPrice;
        this.delivery = delivery;
        this.image = image;
        this.discount = discount;
        this.category = category;
        this.subcategory = subcategory;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getSize() { return size; }
    public double getPrice() { return price; }
    public Double getOldPrice() { return oldPrice; }
    public String getDelivery() { return delivery; }
    public String getImage() { return image; }
    public String getDiscount() { return discount; }
    public String getCategory() { return category; }
    public String getSubcategory() { return subcategory; }
    public List<BundleItem> getBundleItems() { return bundleItems; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSize(String size) { this.size = size; }
    public void setPrice(double price) { this.price = price; }
    public void setOldPrice(Double oldPrice) { this.oldPrice = oldPrice; }
    public void setDelivery(String delivery) { this.delivery = delivery; }
    public void setImage(String image) { this.image = image; }
    public void setDiscount(String discount) { this.discount = discount; }
    public void setCategory(String category) { this.category = category; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public void setBundleItems(List<BundleItem> bundleItems) { this.bundleItems = bundleItems; }
}
