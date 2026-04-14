package com.example.ecogrocer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ecogrocer.models.Product;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.ecogrocer.utils.FirebaseHelper;

import android.content.Intent;
import java.util.List;

public class EcoStoreAdapter extends RecyclerView.Adapter<EcoStoreAdapter.ProductViewHolder> {

    private List<Product> productList;
    private Context context;

    public EcoStoreAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ecostore_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvSize.setText(product.getSize());
        
        long ecoCoins = (long) (product.getPrice() * 100);
        holder.tvEcoCoins.setText(String.format(context.getString(R.string.redeem_with_coins), ecoCoins));

        // Use Glide if available, or just set placeholder
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            String imageUrl = product.getImage();
            if (imageUrl.startsWith("/")) {
                // Handle relative paths from JSON if needed, or use placeholder
                holder.imgProduct.setImageResource(R.drawable.ic_grocery);
            } else {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_grocery)
                        .into(holder.imgProduct);
            }
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_grocery);
        }

        holder.itemView.setOnClickListener(v -> showProductDetailBottomSheet(product));

        holder.btnRedeem.setOnClickListener(v -> {
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
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(productList.size(), 20); // Limit to 20 for home screen performance
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvSize, tvEcoCoins, btnRedeem;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvSize = itemView.findViewById(R.id.tv_product_size);
            tvEcoCoins = itemView.findViewById(R.id.tv_eco_coins);
            btnRedeem = itemView.findViewById(R.id.btn_redeem);
        }
    }

    private void showProductDetailBottomSheet(com.example.ecogrocer.models.Product product) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        View sheetView = LayoutInflater.from(context).inflate(R.layout.layout_product_details, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tvBreadcrumbs = sheetView.findViewById(R.id.tv_detail_breadcrumbs);
        TextView tvName = sheetView.findViewById(R.id.tv_detail_name);
        TextView tvSize = sheetView.findViewById(R.id.tv_detail_size);
        TextView tvPrice = sheetView.findViewById(R.id.tv_detail_price);
        View tvMrp = sheetView.findViewById(R.id.tv_detail_mrp);
        View tvDiscount = sheetView.findViewById(R.id.tv_detail_discount);
        Button btnAdd = sheetView.findViewById(R.id.btn_detail_add);
        View layoutQty = sheetView.findViewById(R.id.layout_detail_qty);

        // Set Data
        tvBreadcrumbs.setText("Home / EcoStore / " + product.getName());
        tvName.setText(product.getName());
        tvSize.setText(product.getSize());

        int coins = (int)(product.getPrice() * 100);
        tvPrice.setText(coins + " Coins");
        tvMrp.setVisibility(View.GONE);
        tvDiscount.setVisibility(View.GONE);
        layoutQty.setVisibility(View.GONE);
        btnAdd.setText("Redeem Now");
        btnAdd.setVisibility(View.VISIBLE);

        btnAdd.setOnClickListener(v -> {
            int cost = (int) (product.getPrice() * 100);
            String userId = FirebaseHelper.getInstance().getCurrentUser().getUid();
            FirebaseHelper.getInstance().redeemProduct(userId, cost, product, new FirebaseHelper.OnRedeemListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, context.getString(R.string.success_redeem), Toast.LENGTH_SHORT).show();
                    bottomSheetDialog.dismiss();
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
        });

        bottomSheetDialog.show();
    }
}
