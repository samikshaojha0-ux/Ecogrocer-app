package com.example.ecogrocer;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecogrocer.models.User;
import com.example.ecogrocer.utils.FirebaseHelper;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private List<User> allUsers;
    private ProgressBar progressBar;
    private TabLayout tabLayout;
    private FirebaseHelper firebaseHelper;
    private boolean sortByCoins = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        firebaseHelper = FirebaseHelper.getInstance();
        initViews();
        loadAllUsers();
    }

    private void initViews() {
        rvLeaderboard = findViewById(R.id.rv_leaderboard);
        progressBar = findViewById(R.id.progress_leaderboard);
        tabLayout = findViewById(R.id.tab_layout);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        String currentUid = "";
        if (firebaseHelper.getCurrentUser() != null) {
            currentUid = firebaseHelper.getCurrentUser().getUid();
        }

        allUsers = new ArrayList<>();
        adapter = new LeaderboardAdapter(allUsers, currentUid, true);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                sortByCoins = tab.getPosition() == 0;
                sortAndDisplay();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadAllUsers() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseHelper.getDatabase().child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allUsers.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (user != null) {
                        allUsers.add(user);
                    }
                }
                sortAndDisplay();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void sortAndDisplay() {
        if (sortByCoins) {
            Collections.sort(allUsers, (u1, u2) -> Integer.compare(u2.getEcoCoins(), u1.getEcoCoins()));
        } else {
            Collections.sort(allUsers, (u1, u2) -> Double.compare(u2.getCarbonSaved(), u1.getCarbonSaved()));
        }
        adapter.setSortByCoins(sortByCoins);
    }
}
