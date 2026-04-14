package com.example.ecogrocer;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ecogrocer.models.WastePickupRequest;
import com.example.ecogrocer.utils.FirebaseHelper;

public class RequestPickupActivity extends AppCompatActivity {

    private EditText etWeight;
    private RadioGroup rgTimeSlots;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_pickup);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
    }

    private void initViews() {
        etWeight = findViewById(R.id.et_weight);
        rgTimeSlots = findViewById(R.id.rg_time_slots);
        btnSubmit = findViewById(R.id.btn_submit_request);
        progressBar = findViewById(R.id.progress_request);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String weightStr = etWeight.getText().toString().trim();
        int selectedId = rgTimeSlots.getCheckedRadioButtonId();

        if (weightStr.isEmpty()) {
            etWeight.setError("Enter weight");
            return;
        }

        if (selectedId == -1) {
            Toast.makeText(this, "Select a time slot", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(weightStr);
        RadioButton selectedSlot = findViewById(selectedId);
        String timeSlot = selectedSlot.getText().toString();

        if (weight <= 0) {
            etWeight.setError("Weight must be greater than 0");
            return;
        }

        // Calculation: 1 kg = 10 EcoCoins
        int estimatedCoins = (int) (weight * 10);

        setLoading(true);

        String userId = firebaseHelper.getCurrentUser().getUid();
        String requestId = firebaseHelper.getDatabase().child("waste_pickups").push().getKey();

        WastePickupRequest request = new WastePickupRequest(
                requestId,
                userId,
                weight,
                timeSlot,
                "Pending",
                estimatedCoins,
                System.currentTimeMillis()
        );

        firebaseHelper.getDatabase().child("waste_pickups").child(requestId).setValue(request)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Pickup requested! Coins will be added after collection.", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Failed to request pickup. Try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean loading) {
        btnSubmit.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
