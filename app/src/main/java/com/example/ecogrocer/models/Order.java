package com.example.ecogrocer.models;

import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private List<CartItem> items;
    private double subtotal;
    private double deliveryFee;
    private double totalAmount;
    private double discount;
    private String status; // "Order placed", "Packed", "Out for delivery", "Delivered"
    private String address;
    private String hubName;
    private long timestamp;
    private double deliveryLat;
    private double deliveryLng;
    private String paymentMethod; // "Online" or "COD"
    private String assignedBoyId;
    private String assignedBoyName;
    private String assignedBoyPhone;

    public Order() {}

    public Order(String orderId, String userId, List<CartItem> items, double subtotal, double deliveryFee, double totalAmount, String status, String address, String hubName, long timestamp, String paymentMethod) {
        this.orderId = orderId;
        this.userId = userId;
        this.items = items;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.totalAmount = totalAmount;
        this.discount = 0.0;
        this.status = status;
        this.address = address;
        this.hubName = hubName;
        this.timestamp = timestamp;
        this.deliveryLat = 0.0;
        this.deliveryLng = 0.0;
        this.paymentMethod = paymentMethod;
    }

    // Getters and Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getHubName() { return hubName; }
    public void setHubName(String hubName) { this.hubName = hubName; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getDeliveryLat() { return deliveryLat; }
    public void setDeliveryLat(double deliveryLat) { this.deliveryLat = deliveryLat; }

    public double getDeliveryLng() { return deliveryLng; }
    public void setDeliveryLng(double deliveryLng) { this.deliveryLng = deliveryLng; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getAssignedBoyId() { return assignedBoyId; }
    public void setAssignedBoyId(String assignedBoyId) { this.assignedBoyId = assignedBoyId; }

    public String getAssignedBoyName() { return assignedBoyName; }
    public void setAssignedBoyName(String assignedBoyName) { this.assignedBoyName = assignedBoyName; }

    public String getAssignedBoyPhone() { return assignedBoyPhone; }
    public void setAssignedBoyPhone(String assignedBoyPhone) { this.assignedBoyPhone = assignedBoyPhone; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
}
