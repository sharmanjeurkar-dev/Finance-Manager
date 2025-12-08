package com.example.upi_expense_tracker.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upi_expense_tracker.R;
import com.example.upi_expense_tracker.data.Transaction;

import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.merchantName.setText(transaction.getMerchantName());
        holder.transactionTime.setText(transaction.getTime());

        if (transaction.isDebit()) {
            holder.transactionAmount.setText(String.format(Locale.getDefault(), "-$%.2f", transaction.getAmount()));
            holder.transactionAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        } else {
            holder.transactionAmount.setText(String.format(Locale.getDefault(), "+$%.2f", transaction.getAmount()));
            holder.transactionAmount.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.green));
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView merchantName;
        TextView transactionTime;
        TextView transactionAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            merchantName = itemView.findViewById(R.id.merchant_name);
            transactionTime = itemView.findViewById(R.id.transaction_time);
            transactionAmount = itemView.findViewById(R.id.transaction_amount);
        }
    }
}
