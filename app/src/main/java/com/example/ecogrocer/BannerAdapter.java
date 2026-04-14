package com.example.ecogrocer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private List<BannerItem> bannerItems;
    private OnBannerClickListener listener;

    public interface OnBannerClickListener {
        void onBannerClick(BannerItem item);
    }

    public BannerAdapter(List<BannerItem> bannerItems, OnBannerClickListener listener) {
        this.bannerItems = bannerItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem item = bannerItems.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvSubtitle.setText(item.subtitle);
        holder.layoutBg.setBackgroundColor(item.backgroundColor);
        holder.ivIcon.setImageResource(item.iconRes);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBannerClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bannerItems.size();
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout layoutBg;
        TextView tvTitle, tvSubtitle;
        ImageView ivIcon;

        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutBg = itemView.findViewById(R.id.layout_banner_bg);
            tvTitle = itemView.findViewById(R.id.tv_banner_title);
            tvSubtitle = itemView.findViewById(R.id.tv_banner_subtitle);
            ivIcon = itemView.findViewById(R.id.iv_banner_icon);
        }
    }

    public static class BannerItem {
        String title;
        String subtitle;
        int backgroundColor;
        int iconRes;
        String action;

        public BannerItem(String title, String subtitle, int backgroundColor, int iconRes, String action) {
            this.title = title;
            this.subtitle = subtitle;
            this.backgroundColor = backgroundColor;
            this.iconRes = iconRes;
            this.action = action;
        }
    }
}
