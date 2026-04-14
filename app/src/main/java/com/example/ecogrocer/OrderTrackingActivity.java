package com.example.ecogrocer;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ecogrocer.models.Order;
import com.example.ecogrocer.network.DirectionsApiService;
import com.example.ecogrocer.network.GoogleMapsRetrofitClient;
import com.example.ecogrocer.utils.FirebaseHelper;
import com.example.ecogrocer.utils.HubSelector;
import com.example.ecogrocer.models.Hub;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String orderId;
    private Order order;

    private TextView tvStatus, tvTime, tvHub, tvBoyName, tvBoyPhone;
    private ImageView iconPlaced, iconPacked, iconDelivery, iconDelivered, btnCallBoy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        orderId = getIntent().getStringExtra("ORDER_ID");

        tvStatus = findViewById(R.id.tv_order_status_title);
        tvTime = findViewById(R.id.tv_estimated_delivery);
        tvHub = findViewById(R.id.tv_assigned_hub);
        tvBoyName = findViewById(R.id.tv_boy_name);
        tvBoyPhone = findViewById(R.id.tv_boy_phone);
        iconPlaced = findViewById(R.id.step_placed_icon);
        iconPacked = findViewById(R.id.step_packed_icon);
        iconDelivery = findViewById(R.id.step_delivery_icon);
        iconDelivered = findViewById(R.id.step_delivered_icon);
        btnCallBoy = findViewById(R.id.btn_call_boy);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fetchOrderDetails();
    }

    private void fetchOrderDetails() {
        FirebaseHelper.getInstance().getDatabase().child("orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        order = snapshot.getValue(Order.class);
                        if (order != null) {
                            updateStatusUI(order.getStatus());
                            if (tvHub != null) {
                                tvHub.setText("Dispatching from: " + order.getHubName());
                            }
                            if (order.getAssignedBoyName() != null && !order.getAssignedBoyName().isEmpty()) {
                                tvBoyName.setText(order.getAssignedBoyName());
                                tvBoyPhone.setText(order.getAssignedBoyPhone());
                                findViewById(R.id.layout_boy_details).setVisibility(android.view.View.VISIBLE);
                            } else {
                                findViewById(R.id.layout_boy_details).setVisibility(android.view.View.GONE);
                            }
                            drawPath();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderTrackingActivity.this, "Error fetching order", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateStatusUI(String status) {
        int activeColor = Color.parseColor("#4CAF50");
        int inactiveColor = Color.parseColor("#BDBDBD");

        // Reset
        iconPlaced.setColorFilter(inactiveColor);
        iconPacked.setColorFilter(inactiveColor);
        iconDelivery.setColorFilter(inactiveColor);
        iconDelivered.setColorFilter(inactiveColor);

        if (status == null) return;

        switch (status) {
            case "Order placed":
                iconPlaced.setColorFilter(activeColor);
                tvStatus.setText("Order Confirmed");
                break;
            case "Packed":
                iconPlaced.setColorFilter(activeColor);
                iconPacked.setColorFilter(activeColor);
                tvStatus.setText("Order Packed");
                break;
            case "Out for delivery":
                iconPlaced.setColorFilter(activeColor);
                iconPacked.setColorFilter(activeColor);
                iconDelivery.setColorFilter(activeColor);
                tvStatus.setText("On the way!");
                break;
            case "Delivered":
                iconPlaced.setColorFilter(activeColor);
                iconPacked.setColorFilter(activeColor);
                iconDelivery.setColorFilter(activeColor);
                iconDelivered.setColorFilter(activeColor);
                tvStatus.setText("Delivered");
                break;
        }
    }

    private void drawPath() {
        if (mMap == null || order == null) return;
        mMap.clear();

        HubSelector selector = new HubSelector(this);
        Hub hub = selector.getHubByName(order.getHubName());

        if (hub == null) return;

        LatLng origin = new LatLng(hub.getLat(), hub.getLng());
        
        // Marker for Hub
        mMap.addMarker(new MarkerOptions()
                .position(origin)
                .title("Hub: " + hub.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        // Marker for Destination
        LatLng destination;
        if (order.getDeliveryLat() != 0) {
            destination = new LatLng(order.getDeliveryLat(), order.getDeliveryLng());
        } else {
            // Mock destination for demo if no coords
            destination = new LatLng(origin.latitude + 0.02, origin.longitude + 0.015);
        }

        mMap.addMarker(new MarkerOptions()
                .position(destination)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Zoom to hub initially
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 13f));

        // Attempt to fetch road route
        fetchRoadRoute(origin, destination);
    }

    private void fetchRoadRoute(LatLng origin, LatLng destination) {
        String originStr = origin.latitude + "," + origin.longitude;
        String destStr = destination.latitude + "," + destination.longitude;
        String apiKey = getString(R.string.google_maps_key);

        DirectionsApiService apiService = GoogleMapsRetrofitClient.getClient().create(DirectionsApiService.class);
        apiService.getDirections(originStr, destStr, "driving", apiKey).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        
                        if (!jsonObject.getString("status").equals("OK")) {
                            drawFallbackLine(origin, destination);
                            return;
                        }

                        JSONArray routes = jsonObject.getJSONArray("routes");

                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            String encodedPolyline = route.getJSONObject("overview_polyline").getString("points");

                            // Road distance/duration
                            JSONArray legs = route.getJSONArray("legs");
                            if (legs.length() > 0) {
                                String distanceText = legs.getJSONObject(0).getJSONObject("distance").getString("text");
                                String durationText = legs.getJSONObject(0).getJSONObject("duration").getString("text");
                                tvTime.setText("Distance: " + distanceText + " | ETA: " + durationText);
                            }

                            List<LatLng> points = decodePoly(encodedPolyline);
                            mMap.addPolyline(new PolylineOptions()
                                    .addAll(points)
                                    .width(14) // Thicker for better visibility
                                    .color(Color.parseColor("#2E7D32")) // Premium Dark Green
                                    .jointType(com.google.android.gms.maps.model.JointType.ROUND)
                                    .startCap(new com.google.android.gms.maps.model.RoundCap())
                                    .endCap(new com.google.android.gms.maps.model.RoundCap())
                                    .geodesic(true));

                            // Fit bounds
                            LatLngBounds bounds = new LatLngBounds.Builder()
                                    .include(origin)
                                    .include(destination)
                                    .build();
                            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
                        } else {
                            drawFallbackLine(origin, destination);
                        }
                    } else {
                        drawFallbackLine(origin, destination);
                    }
                } catch (Exception e) {
                    drawFallbackLine(origin, destination);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                drawFallbackLine(origin, destination);
            }
        });
    }

    private void drawFallbackLine(LatLng origin, LatLng destination) {
        mMap.addPolyline(new PolylineOptions()
                .add(origin, destination)
                .width(10)
                .color(Color.GRAY)
                .pattern(java.util.Arrays.asList(new com.google.android.gms.maps.model.Dash(20), new com.google.android.gms.maps.model.Gap(20))));
        
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(origin)
                .include(destination)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            poly.add(new LatLng((((double) lat / 1E5)), (((double) lng / 1E5))));
        }
        return poly;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        drawPath();
    }
}
