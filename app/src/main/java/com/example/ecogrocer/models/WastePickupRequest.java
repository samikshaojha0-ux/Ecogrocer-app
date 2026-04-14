package com.example.ecogrocer.models;

public class WastePickupRequest {
    private String id;
    private String userId;
    private double weight;
    private String timeSlot;
    private String status; // "Pending", "Collected", "Cancelled"
    private int coinsAwarded;
    private long timestamp;

    public WastePickupRequest() {}

    public WastePickupRequest(String id, String userId, double weight, String timeSlot, String status, int coinsAwarded, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.weight = weight;
        this.timeSlot = timeSlot;
        this.status = status;
        this.coinsAwarded = coinsAwarded;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCoinsAwarded() { return coinsAwarded; }
    public void setCoinsAwarded(int coinsAwarded) { this.coinsAwarded = coinsAwarded; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
