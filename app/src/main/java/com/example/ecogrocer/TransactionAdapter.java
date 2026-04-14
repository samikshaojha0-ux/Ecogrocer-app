package com.example.ecogrocer;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecogrocer.models.EcoCoinTransaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<EcoCoinTransaction> transactionList;

    public TransactionAdapter(Context context, List<EcoCoinTransaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        EcoCoinTransaction trans = transactionList.get(position);

        holder.tvDescription.setText(trans.getDescription());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(trans.getTimestamp())));

        if ("Earned".equals(trans.getType())) {
            holder.tvAmount.setText("+" + trans.getAmount());
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.green_primary));
            holder.imgType.setImageResource(R.drawable.ic_rewards);
            holder.imgType.setBackgroundResource(R.drawable.bg_circle_gold);
        } else {
            holder.tvAmount.setText("-" + trans.getAmount());
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.error));
            holder.imgType.setImageResource(R.drawable.ic_rewards);
            holder.imgType.setBackgroundResource(R.drawable.bg_circle_gold); // Or a different bg for redemption
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgType;
        TextView tvDescription, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgType = itemView.findViewById(R.id.img_type);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvAmount = itemView.findViewById(R.id.tv_amount);
        }
    }
}
