package com.example.ecogrocer;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecogrocer.models.BundleItem;
import com.example.ecogrocer.models.Product;
import com.example.ecogrocer.utils.CartManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BundleCustomizerSheet extends BottomSheetDialogFragment {

    private Product bundleProduct;
    private List<BundleItem> originalItems;
    private Map<String, Integer> itemQuantities = new HashMap<>();
    private TextView tvTotalPrice;
    private double currentTotalPrice;

    public static BundleCustomizerSheet newInstance(Product product) {
        BundleCustomizerSheet fragment = new BundleCustomizerSheet();
        Bundle args = new Bundle();
        args.putSerializable("product", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bundleProduct = (Product) getArguments().getSerializable("product");
            originalItems = bundleProduct.getBundleItems();
            if (originalItems != null) {
                for (BundleItem item : originalItems) {
                    itemQuantities.put(item.getName(), 1);
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_bundle_customizer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvTitle = view.findViewById(R.id.tv_bundle_title);
        tvTitle.setText("Customize " + bundleProduct.getName());

        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        updateTotalPrice();

        RecyclerView rvItems = view.findViewById(R.id.rv_bundle_items);
        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemAdapter adapter = new ItemAdapter();
        rvItems.setAdapter(adapter);

        view.findViewById(R.id.btn_add_bundle).setOnClickListener(v -> {
            // For now, we still add the base product
            // In a more complex app, we might create a customized product version
            CartManager.getInstance(getContext()).addToCart(bundleProduct);
            Toast.makeText(getContext(), "Customized bundle added to cart!", Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void updateTotalPrice() {
        double total = 0;
        if (originalItems != null) {
            for (BundleItem item : originalItems) {
                int qty = itemQuantities.get(item.getName());
                total += item.getPrice() * qty;
            }
        }
        currentTotalPrice = total;
        tvTotalPrice.setText("₹" + (int) currentTotalPrice);
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bundle_sub_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BundleItem item = originalItems.get(position);
            holder.tvName.setText(item.getName() + " (" + item.getQuantity() + ")");
            int qty = itemQuantities.get(item.getName());
            holder.tvQty.setText(String.valueOf(qty));

            holder.btnPlus.setOnClickListener(v -> {
                itemQuantities.put(item.getName(), qty + 1);
                updateTotalPrice();
                notifyItemChanged(position);
            });

            holder.btnMinus.setOnClickListener(v -> {
                if (qty > 0) {
                    itemQuantities.put(item.getName(), qty - 1);
                    updateTotalPrice();
                    notifyItemChanged(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return originalItems != null ? originalItems.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvName, tvQty;
            ImageView btnPlus, btnMinus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvName = itemView.findViewById(R.id.tv_item_name);
                tvQty = itemView.findViewById(R.id.tv_quantity);
                btnPlus = itemView.findViewById(R.id.btn_plus);
                btnMinus = itemView.findViewById(R.id.btn_minus);
            }
        }
    }
}
