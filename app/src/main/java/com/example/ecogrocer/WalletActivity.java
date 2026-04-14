package com.example.ecogrocer;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecogrocer.models.EcoCoinTransaction;
import com.example.ecogrocer.models.User;
import com.example.ecogrocer.utils.FirebaseHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WalletActivity extends AppCompatActivity {

    private TextView tvTotalBalance, tvEarned, tvRedeemed;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<EcoCoinTransaction> transactions = new ArrayList<>();
    private FirebaseHelper firebaseHelper;
    private LinearLayout layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
        loadWalletData();
        loadTransactionHistory();
    }

    private void initViews() {
        tvTotalBalance = findViewById(R.id.tv_total_coins);
        tvEarned = findViewById(R.id.tv_coins_earned);
        tvRedeemed = findViewById(R.id.tv_coins_redeemed);
        rvTransactions = findViewById(R.id.rv_transactions);
        layoutEmpty = findViewById(R.id.layout_empty_history);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(this, transactions);
        rvTransactions.setAdapter(adapter);
    }

    private void loadWalletData() {
        String userId = firebaseHelper.getCurrentUser().getUid();
        firebaseHelper.getUserDocument(userId, new FirebaseHelper.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    tvTotalBalance.setText(String.valueOf(user.getEcoCoins()));
                }
            }

            @Override
            public void onFailure(Exception e) {
                tvTotalBalance.setText("0");
            }
        });
    }

    private void loadTransactionHistory() {
        String userId = firebaseHelper.getCurrentUser().getUid();
        firebaseHelper.getDatabase().child("transactions")
                .orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        transactions.clear();
                        int earnedCount = 0;
                        int redeemedCount = 0;

                        for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                            EcoCoinTransaction trans = postSnapshot.getValue(EcoCoinTransaction.class);
                            if (trans != null) {
                                transactions.add(trans);
                                if ("Earned".equals(trans.getType())) {
                                    earnedCount += trans.getAmount();
                                } else {
                                    redeemedCount += trans.getAmount();
                                }
                            }
                        }

                        Collections.reverse(transactions);
                        adapter.notifyDataSetChanged();
                        
                        tvEarned.setText(String.valueOf(earnedCount));
                        tvRedeemed.setText(String.valueOf(redeemedCount));

                        layoutEmpty.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                });
    }
}
