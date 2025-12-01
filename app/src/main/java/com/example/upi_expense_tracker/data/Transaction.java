package com.example.upi_expense_tracker.data;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private double amount;
    private String merchantName;
    private long timeStamp;
    private boolean isDebit;

    // private String source; after adding bhimpay functionality

    //Constructor

    public Transaction(double amount, String merchantName, long timeStamp, boolean isDebit) {
        this.amount = amount;
        this.merchantName = merchantName;
        this.timeStamp = timeStamp;
        this.isDebit = isDebit;
    }
}
