package com.example.upi_expense_tracker.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upi_expense_tracker.R;
import com.example.upi_expense_tracker.adapter.TransactionAdapter;
import com.example.upi_expense_tracker.data.AppDatabase;
import com.example.upi_expense_tracker.data.Transaction;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;

    private TextView latestMerchant;
    private TextView latestTime;
    private TextView latestAmount;
    private TextView totalSpentAmount;
    private RecyclerView previousPaymentsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latestMerchant = findViewById(R.id.latest_merchant);
        latestTime = findViewById(R.id.latest_time);
        latestAmount = findViewById(R.id.latest_amount);
        totalSpentAmount = findViewById(R.id.total_spent_amount);
        previousPaymentsRecyclerView = findViewById(R.id.recycler_view_previous_payments);
        previousPaymentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchTransactions();
    }

    private void fetchTransactions() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Transaction> transactionList = AppDatabase.getDatabase(getApplicationContext())
                    .transactionDAO()
                    .getAllTransactions();
            double totalSpent = AppDatabase.getDatabase(getApplicationContext())
                    .transactionDAO()
                    .getTotalSpent();

            runOnUiThread(() -> {
                if (transactionList.isEmpty()) {
                    latestMerchant.setText("No payments found");
                    totalSpentAmount.setText("$0.00");
                } else {
                    // Set latest payment
                    Transaction latestTransaction = transactionList.get(0);
                    latestMerchant.setText(latestTransaction.getMerchantName());
                    latestTime.setText(latestTransaction.getTime());

                    if (latestTransaction.isDebit()) {
                        latestAmount.setText(String.format(Locale.getDefault(), "-$%.2f", latestTransaction.getAmount()));
                        latestAmount.setTextColor(ContextCompat.getColor(this, R.color.red));
                    } else {
                        latestAmount.setText(String.format(Locale.getDefault(), "+$%.2f", latestTransaction.getAmount()));
                        latestAmount.setTextColor(ContextCompat.getColor(this, R.color.green));
                    }

                    // Set total spent
                    totalSpentAmount.setText(String.format(Locale.getDefault(), "$%.2f", totalSpent));

                    // Set previous payments
                    if (transactionList.size() > 1) {
                        List<Transaction> previousTransactions = transactionList.subList(1, transactionList.size());
                        TransactionAdapter adapter = new TransactionAdapter(previousTransactions);
                        previousPaymentsRecyclerView.setAdapter(adapter);
                    }
                }
            });
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Listening Enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied: Cannot read Bank SMS", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
