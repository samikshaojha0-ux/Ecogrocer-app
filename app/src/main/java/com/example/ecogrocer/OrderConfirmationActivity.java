package com.example.ecogrocer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class OrderConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        String orderId = getIntent().getStringExtra("ORDER_ID");
        TextView tvOrderId = findViewById(R.id.tv_order_id);
        if (orderId != null) {
            tvOrderId.setText("Order ID: #" + orderId.substring(orderId.length() - 8).toUpperCase());
        }

        findViewById(R.id.btn_track_order).setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderTrackingActivity.class);
            intent.putExtra("ORDER_ID", orderId);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btn_back_home).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
