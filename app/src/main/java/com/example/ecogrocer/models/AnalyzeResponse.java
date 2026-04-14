package com.example.ecogrocer.models;

public class AnalyzeResponse {
    private String item;
    private String status;
    private int freshness;
    private String prediction;
    private String tip;

    public String getItem() { return item; }
    public String getStatus() { return status; }
    public int getFreshness() { return freshness; }
    public String getPrediction() { return prediction; }
    public String getTip() { return tip; }
}
