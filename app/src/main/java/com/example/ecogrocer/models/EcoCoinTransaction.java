package com.example.ecogrocer.models;

public class EcoCoinTransaction {
    private String id;
    private String userId;
    private int amount;
    private String type; // "Earned", "Redeemed"
    private String description;
    private long timestamp;

    public EcoCoinTransaction() {}

    public EcoCoinTransaction(String id, String userId, int amount, String type, String description, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
