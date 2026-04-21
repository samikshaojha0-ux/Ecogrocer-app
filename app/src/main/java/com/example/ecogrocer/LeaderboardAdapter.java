package com.example.ecogrocer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecogrocer.models.User;
import java.util.List;
import java.util.Locale;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private List<User> userList;
    private boolean sortByCoins;

    public LeaderboardAdapter(List<User> userList, boolean sortByCoins) {
        this.userList = userList;
        this.sortByCoins = sortByCoins;
    }

    public void setSortByCoins(boolean sortByCoins) {
        this.sortByCoins = sortByCoins;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvRank.setText(String.valueOf(position + 1));
        holder.tvName.setText(user.getName() != null ? user.getName() : "Eco Hero");

        if (sortByCoins) {
            holder.tvScore.setText(String.valueOf(user.getEcoCoins()));
            holder.tvImpact.setText(String.format(Locale.getDefault(), "Carbon Saved: %.1f kg", user.getCarbonSaved()));
        } else {
            holder.tvScore.setText(String.format(Locale.getDefault(), "%.1f", user.getCarbonSaved()));
            holder.tvImpact.setText(String.format(Locale.getDefault(), "Points: %d", user.getEcoCoins()));
        }

        // Apply visual rank highlighting for top 3
        if (position == 0) {
            holder.tvRank.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.eco_coin_gold));
            holder.tvRank.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 22);
        } else if (position == 1) {
            holder.tvRank.setTextColor(0xFFACACAC); // Silver
            holder.tvRank.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 20);
        } else if (position == 2) {
            holder.tvRank.setTextColor(0xFFCD7F32); // Bronze
            holder.tvRank.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 19);
        } else {
            holder.tvRank.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_primary));
            holder.tvRank.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvImpact, tvScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvImpact = itemView.findViewById(R.id.tv_user_impact);
            tvScore = itemView.findViewById(R.id.tv_score);
        }
    }
}
