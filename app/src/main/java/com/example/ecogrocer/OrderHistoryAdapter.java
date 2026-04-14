package com.example.ecogrocer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecogrocer.models.CartItem;
import com.example.ecogrocer.models.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {

    private List<Order> orders;

    public OrderHistoryAdapter(List<Order> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        
        String idText = "#" + order.getOrderId().substring(order.getOrderId().length() - 8).toUpperCase();
        holder.tvOrderId.setText(idText);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(order.getTimestamp())));
        
        StringBuilder itemsText = new StringBuilder();
        for (int i = 0; i < order.getItems().size(); i++) {
            CartItem item = order.getItems().get(i);
            if (item.getProduct() != null) {
                itemsText.append(item.getProduct().getName());
            }
            if (i < order.getItems().size() - 1) itemsText.append(", ");
        }
        holder.tvItems.setText(itemsText.toString());
        
        holder.tvAmount.setText("₹" + (int)order.getTotalAmount());
        holder.tvStatus.setText(order.getStatus());
        
        holder.itemView.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(v.getContext(), OrderTrackingActivity.class);
            intent.putExtra("ORDER_ID", order.getOrderId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvDate, tvItems, tvAmount, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvDate = itemView.findViewById(R.id.tv_order_date);
            tvItems = itemView.findViewById(R.id.tv_order_items);
            tvAmount = itemView.findViewById(R.id.tv_order_amount);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
        }
    }
}
