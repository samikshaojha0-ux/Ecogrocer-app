package com.example.ecogrocer;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecogrocer.models.Order;
import com.example.ecogrocer.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private OrderHistoryAdapter adapter;
    private List<Order> historyList;
    private ProgressBar progressBar;
    private View emptyState;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
        loadHistory();
    }

    private void initViews() {
        rvHistory = findViewById(R.id.rv_order_history);
        progressBar = findViewById(R.id.progress_history);
        emptyState = findViewById(R.id.layout_empty_history);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        historyList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(historyList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(adapter);
    }

    private void loadHistory() {
        if (!firebaseHelper.isLoggedIn()) return;
        
        progressBar.setVisibility(View.VISIBLE);
        String userId = firebaseHelper.getCurrentUser().getUid();
        firebaseHelper.getDatabase().child("orders")
                .orderByChild("userId")
                .equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        historyList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Order order = ds.getValue(Order.class);
                            if (order != null) historyList.add(order);
                        }
                        
                        Collections.sort(historyList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                        
                        progressBar.setVisibility(View.GONE);
                        if (historyList.isEmpty()) emptyState.setVisibility(View.VISIBLE);
                        else emptyState.setVisibility(View.GONE);
                        
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }
}
