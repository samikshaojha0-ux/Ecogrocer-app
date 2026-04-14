package com.example.ecogrocer.models;

public class DeliveryBoy {
    private String boyId;
    private String name;
    private String phoneNumber;
    private String address;
    private String assignedHubId;
    private double currentLat;
    private double currentLng;

    public DeliveryBoy() {}

    public DeliveryBoy(String boyId, String name, String phoneNumber, String address, String assignedHubId) {
        this.boyId = boyId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.assignedHubId = assignedHubId;
    }

    // Getters and Setters
    public String getBoyId() { return boyId; }
    public void setBoyId(String boyId) { this.boyId = boyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAssignedHubId() { return assignedHubId; }
    public void setAssignedHubId(String assignedHubId) { this.assignedHubId = assignedHubId; }

    public double getCurrentLat() { return currentLat; }
    public void setCurrentLat(double currentLat) { this.currentLat = currentLat; }

    public double getCurrentLng() { return currentLng; }
    public void setCurrentLng(double currentLng) { this.currentLng = currentLng; }
}
