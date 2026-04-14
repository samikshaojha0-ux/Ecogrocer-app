package com.example.ecogrocer.utils;

import android.content.Context;
import com.example.ecogrocer.models.Hub;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HubSelector {

    private List<Hub> hubs;

    public HubSelector(Context context) {
        loadHubsFromAssets(context);
        loadHubs(context);
    }

    private void loadHubs(Context context) {
        FirebaseHelper.getInstance().getHubs(new FirebaseHelper.OnHubsFetchListener() {
            @Override
            public void onSuccess(List<Hub> firebaseHubs) {
                if (firebaseHubs == null || firebaseHubs.isEmpty()) {
                    // Sync if empty
                    syncHubsFromAssets(context);
                } else {
                    hubs = firebaseHubs;
                }
            }

            @Override
            public void onFailure(Exception e) {
                // Keep the asset-loaded hubs
            }
        });
    }

    private void loadHubsFromAssets(Context context) {
        try {
            InputStream is = context.getAssets().open("hubs.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            Type listType = new TypeToken<List<Hub>>() {}.getType();
            hubs = new Gson().fromJson(json, listType);
        } catch (Exception e) {
            hubs = new ArrayList<>();
        }
    }

    private void syncHubsFromAssets(Context context) {
        loadHubsFromAssets(context);
        if (hubs != null && !hubs.isEmpty()) {
            FirebaseHelper.getInstance().saveHubs(hubs, 
                aVoid -> { /* Hubs synced */ },
                e -> { /* Sync failed */ }
            );
        }
    }

    public String findClosestHub(String address) {
        if (address == null || address.isEmpty()) return "Eco Grocer – Dadar Central Hub";

        String lowerAddress = address.toLowerCase();
        for (Hub hub : hubs) {
            if (lowerAddress.contains(hub.getAreaKeyword().toLowerCase())) {
                return hub.getName();
            }
        }

        // Default if no match found
        return "Eco Grocer – Dadar Central Hub";
    }

    public Hub getHubByName(String name) {
        if (hubs == null || name == null) return null;
        String searchName = name.toLowerCase();
        for (Hub hub : hubs) {
            if (searchName.contains(hub.getName().toLowerCase()) || hub.getName().toLowerCase().contains(searchName)) {
                return hub;
            }
        }
        return null;
    }
}
