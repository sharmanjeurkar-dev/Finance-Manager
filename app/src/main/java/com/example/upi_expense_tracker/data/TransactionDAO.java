package com.example.upi_expense_tracker.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TransactionDAO {

    //Insert the data coming from the Notification Listener Service
    @Insert
    void insertTransaction(Transaction transaction);

    //Selecting all the data and making it visible
    @Query("Select * from transactions order by timeStamp DESC")
    List <Transaction> getAllTransactions();

    //Calculate the total spent
    @Query("Select SUM(amount) from transactions where isDebit=1 ")
    double getTotalSpent();

    @Query("SELECT * FROM transactions WHERE amount = :amount AND timeStamp > :timeLimit LIMIT 1")
    Transaction checkDuplicate(double amount, long timeLimit);

}
