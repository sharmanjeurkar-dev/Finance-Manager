package com.example.upi_expense_tracker.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.upi_expense_tracker.R;
import com.example.upi_expense_tracker.data.AppDatabase;
import com.example.upi_expense_tracker.data.Transaction;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       TextView tvTransaction = findViewById(R.id.tv_transactions);

       //Fetch data on background
        AppDatabase.databaseWriteExecutor.execute(()->{

            List<Transaction> list = AppDatabase.getDatabase(getApplicationContext())
                    .transactionDAO()
                    .getAllTransactions();

            StringBuilder display = new StringBuilder();
            for(Transaction t: list) {
                display.append("Rs").append(t.getAmount());
                display.append("\n------------------------------------\n");
            }
            runOnUiThread(() -> {
                if (list.isEmpty()) {
                    tvTransaction.setText("No expenses found yet.");
                } else {
                    tvTransaction.setText(display.toString());
                }
            });

        });


    }
}