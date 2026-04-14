package com.example.ecogrocer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.ecogrocer.utils.CartManager;
import com.example.ecogrocer.models.Product;
import com.example.ecogrocer.utils.FirebaseHelper;
import android.widget.LinearLayout;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.ecogrocer.models.CartItem;
import com.example.ecogrocer.models.Order;
import com.example.ecogrocer.utils.AprioriHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnCartUpdateListener listener;
    private boolean isEcoStore = false;

    public interface OnCartUpdateListener {
        void onCartUpdated();
    }

    public ProductAdapter(Context context, List<Product> productList, OnCartUpdateListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    public void setEcoStore(boolean ecoStore) {
        isEcoStore = ecoStore;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        
        if (isEcoStore) {
            long ecoCoins = (long) (product.getPrice() * 100);
            holder.tvInfo.setText(ecoCoins + " Coins  |  " + product.getSize());
            holder.tvOldPrice.setVisibility(View.GONE);
            holder.btnAdd.setText(R.string.redeem);
            holder.btnAdd.setBackgroundResource(R.drawable.bg_add_button); // Keep same or use a gold one
        } else {
            holder.tvInfo.setText("₹" + (int)product.getPrice() + "  |  " + product.getSize());
            holder.btnAdd.setText(R.string.add);
            
            if (product.getOldPrice() != null && product.getOldPrice() > product.getPrice()) {
                holder.tvOldPrice.setVisibility(View.VISIBLE);
                holder.tvOldPrice.setText("₹" + product.getOldPrice().intValue());
                holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.tvOldPrice.setVisibility(View.GONE);
            }
        }

        if (product.getDiscount() != null && !product.getDiscount().isEmpty()) {
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(product.getDiscount());
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }

        // Image loading
        String imageUrl = product.getImage();
        if (imageUrl != null && imageUrl.startsWith("http")) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_store)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_store);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });

        holder.btnAdd.setOnClickListener(v -> {
            if (isEcoStore) {
                // Redeem logic
                int cost = (int) (product.getPrice() * 100);
                String userId = com.example.ecogrocer.utils.FirebaseHelper.getInstance().getCurrentUser().getUid();
                
                com.example.ecogrocer.utils.FirebaseHelper.getInstance().redeemProduct(userId, cost, product, new com.example.ecogrocer.utils.FirebaseHelper.OnRedeemListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, context.getString(R.string.success_redeem), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onInsufficientCoins() {
                        Toast.makeText(context, context.getString(R.string.not_enough_coins), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(context, "Redemption failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                CartManager.getInstance(context).addToCart(product);
                Toast.makeText(context, product.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onCartUpdated();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvInfo, tvOldPrice, tvDiscount, btnAdd;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvInfo = itemView.findViewById(R.id.tv_product_info);
            tvOldPrice = itemView.findViewById(R.id.tv_old_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }

    private void loadRecommendationsForProduct(Product clickedProduct, LinearLayout layoutRecommendations, RecyclerView rvRecommendations) {
        // This method is now handled by ProductDetailActivity
    }
}
