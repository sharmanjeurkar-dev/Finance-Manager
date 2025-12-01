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

    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean isDebit() {
        return isDebit;
    }

    public void setId(int id) {
        this.id = id;
    }
    //Constructor

    public Transaction(double amount, String merchantName, long timeStamp, boolean isDebit) {
        this.amount = amount;
        this.merchantName = merchantName;
        this.timeStamp = timeStamp;
        this.isDebit = isDebit;
    }
}
