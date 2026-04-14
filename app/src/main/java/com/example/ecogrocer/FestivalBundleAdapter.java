package com.example.ecogrocer;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.ecogrocer.models.Product;
import com.example.ecogrocer.utils.CartManager;
import java.util.List;

public class FestivalBundleAdapter extends RecyclerView.Adapter<FestivalBundleAdapter.ViewHolder> {

    private androidx.appcompat.app.AppCompatActivity activity;
    private List<Product> productList;

    public FestivalBundleAdapter(androidx.appcompat.app.AppCompatActivity activity, List<Product> productList) {
        this.activity = activity;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_festival_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvInfo.setText(product.getSize());
        holder.tvPrice.setText("₹" + (int)product.getPrice());
        holder.tvDelivery.setText(product.getDelivery());

        if (product.getOldPrice() != null && product.getOldPrice() > product.getPrice()) {
            holder.tvOldPrice.setVisibility(View.VISIBLE);
            holder.tvOldPrice.setText("₹" + product.getOldPrice().intValue());
            holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvOldPrice.setVisibility(View.GONE);
        }

        if (product.getDiscount() != null && !product.getDiscount().isEmpty()) {
            holder.tvDiscount.setVisibility(View.VISIBLE);
            holder.tvDiscount.setText(product.getDiscount());
        } else {
            holder.tvDiscount.setVisibility(View.GONE);
        }

        Glide.with(activity)
                .load(product.getImage())
                .placeholder(R.drawable.ic_grocery)
                .into(holder.imgProduct);

        holder.btnAdd.setOnClickListener(v -> {
            CartManager.getInstance(activity).addToCart(product);
            Toast.makeText(activity, product.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
        });

        holder.itemView.setOnClickListener(v -> {
            if (product.getBundleItems() != null && !product.getBundleItems().isEmpty()) {
                BundleCustomizerSheet.newInstance(product)
                        .show(activity.getSupportFragmentManager(), "bundle_customizer");
            } else {
                android.content.Intent intent = new android.content.Intent(activity, ProductDetailActivity.class);
                intent.putExtra("product", product);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName, tvInfo, tvPrice, tvOldPrice, tvDiscount, tvDelivery, btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvInfo = itemView.findViewById(R.id.tv_product_info);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvOldPrice = itemView.findViewById(R.id.tv_old_price);
            tvDiscount = itemView.findViewById(R.id.tv_discount);
            tvDelivery = itemView.findViewById(R.id.tv_delivery_badge);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }
}
