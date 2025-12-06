package com.example.upi_expense_tracker.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.upi_expense_tracker.R;
import com.example.upi_expense_tracker.data.AppDatabase;
import com.example.upi_expense_tracker.data.Transaction;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);
       TextView tvTransaction = findViewById(R.id.tv_transactions);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
        }


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