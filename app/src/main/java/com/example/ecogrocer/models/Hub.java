package com.example.ecogrocer.models;

public class Hub {
    private String name;
    private String region;
    private String areaKeyword;
    private double lat;
    private double lng;

    public Hub() {}

    public Hub(String name, String region, String areaKeyword, double lat, double lng) {
        this.name = name;
        this.region = region;
        this.areaKeyword = areaKeyword;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() { return name; }
    public String getRegion() { return region; }
    public String getAreaKeyword() { return areaKeyword; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}
