package com.example.upi_expense_tracker.services;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.upi_expense_tracker.data.AppDatabase;
import com.example.upi_expense_tracker.data.Transaction;
import com.example.upi_expense_tracker.data.TransactionDAO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationService extends NotificationListenerService {

    private  static final String TAG = "UPI DEBUG";
    private String lastProcessedText = "";
    private long lastProcessedTime = 0;

    @Override
    public void onListenerConnected(){
        super.onListenerConnected();
        Log.d(TAG,"Service started! Listener Activated");
    }

    @Override
    public void onListenerDisconnected(){
        super.onListenerDisconnected();
        Log.d(TAG,"Service ended Listener Deactivated");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        String GPAY_PACKAGE = "com.google.android.apps.nbu.paisa.user";
        String PHONEPE_PACKAGE = "com.phonepe.app";
        String PAYTM_PACKAGE = "net.one97.paytm";
        String WHATSAPP_PACKAGE = "com.whatsapp"; // For testing

        String packageName = sbn.getPackageName();
        if(!GPAY_PACKAGE.equals(packageName) && !PHONEPE_PACKAGE.equals(packageName) && !PAYTM_PACKAGE.equals(packageName) && !WHATSAPP_PACKAGE.equals(packageName)){
            return;
        }

        String text = "";
        if(sbn.getNotification().extras.getCharSequence("android.text")!=null){
            text = sbn.getNotification().extras.getCharSequence("android.text").toString();
        }

        if(text.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        if (text.equals(lastProcessedText) && (currentTime - lastProcessedTime) < 2000) {
            Log.d(TAG, "Duplicate notification ignored: " + text);
            return;
        }

        final String AMOUNT_PATTERN = "(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+\\.?\\d*)";
        Pattern amountPattern = Pattern.compile(AMOUNT_PATTERN);
        Matcher amountMatcher = amountPattern.matcher(text);

        if(amountMatcher.find()){
            String rawAmount = amountMatcher.group(1);
            if (rawAmount!=null){
                String finalAmount = rawAmount.replace(",", "");
                try{
                    double amount = Double.parseDouble(finalAmount);
                    
                    boolean isDebit;
                    String lowerCaseText = text.toLowerCase();
                    if (lowerCaseText.contains("credited") || lowerCaseText.contains("received") || lowerCaseText.contains("deposited")) {
                        isDebit = false;
                    } else {
                        isDebit = true; // Default to debit
                    }

                    String merchantName = "Unknown";
                    final String MERCHANT_PATTERN = "(?i)(?:to|from|sent to|paid to|paying)\\s+([A-Za-z0-9\\s\\.\\-&'@]+?)(?:\\s+on|\\s+at|\\s*\\.|\\s*|$)";
                    Pattern merchantPattern = Pattern.compile(MERCHANT_PATTERN);
                    Matcher merchantMatcher = merchantPattern.matcher(text);

                    if (merchantMatcher.find()) {
                        merchantName = merchantMatcher.group(1).trim();
                        if(merchantName.contains("@")){
                           merchantName = merchantName.split("@")[0];
                        }
                    }

                    lastProcessedText = text;
                    lastProcessedTime = currentTime;
                    saveToDatabase(amount, merchantName, isDebit);
                } catch (NumberFormatException e) {
                    Log.d(TAG, "Amount found not in right format");
                }
            }
        }
    }

    public void saveToDatabase(double amount, String merchantName, boolean isDebit){
        TransactionDAO dao = AppDatabase.getDatabase(getApplicationContext()).transactionDAO();
        long timeCheck = System.currentTimeMillis() - 60000; // 1 minute window

        Transaction existing = dao.checkDuplicate(amount, timeCheck);
        
        if (existing!=null){
            Log.d(TAG, "saveToDatabase: Transaction already existing duplicate transaction avoided");
            return;
        }

        long currentTime = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, h:mm a", Locale.getDefault());
        String formattedTime = sdf.format(new Date(currentTime));

        Transaction transaction = new Transaction(
                amount,
                merchantName,
                formattedTime,
                currentTime,
                isDebit
        );

        AppDatabase.databaseWriteExecutor.execute(()->{
            dao.insertTransaction(transaction);
            Log.d(TAG, "Transaction saved to Database. Amount: " + amount + ", Merchant: " + merchantName + ", Debit: " + isDebit);
        });
    }

    @Override
    public void  onNotificationRemoved(StatusBarNotification sbn){
        //Code
    }
}
