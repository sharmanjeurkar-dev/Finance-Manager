package com.example.upi_expense_tracker.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Transaction.class},version = 1,exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static volatile AppDatabase INSTANCE;
    public abstract TransactionDAO transactionDAO();

    private static final int NUMBER_OF_THREADS = 4;

    // Threads to use secondary threads instead of Main ui thread
    private static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    public static AppDatabase getDatabase(final Context context){
        //Checks if Db created by any thread multiple treads can pass before one actually created the db
        if(INSTANCE == null)
            synchronized (AppDatabase.class){
                // Checks if after the first check if one of the threads has already created a thread
                if(INSTANCE == null){
                    //Instance of this class creates the db for only once and keeps in cache for multiple use
                    INSTANCE = Room.databaseBuilder
                            (
                                    //keeps the data stored even after closing app
                                context.getApplicationContext()
                                ,AppDatabase.class
                                    // DB name
                                ,"FINANCE_MANAGER_DB"
                            ).build();// builds the db
                }
            }
        return INSTANCE; // returns the db
    }

}
