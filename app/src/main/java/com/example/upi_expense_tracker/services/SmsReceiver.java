package com.example.upi_expense_tracker.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.upi_expense_tracker.data.AppDatabase;
import com.example.upi_expense_tracker.data.Transaction;
import com.example.upi_expense_tracker.data.TransactionDAO;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private final String TAG = "UPI SMS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (messages != null) {
                for (SmsMessage sms : messages) {
                    if (sms == null) continue;
                    String sender = sms.getDisplayOriginatingAddress();
                    String messageBody = sms.getDisplayMessageBody();
                    Log.d(TAG, "SMS From: " + sender);
                    if (isBankSender(sender)) {
                        checkForMoney(context, messageBody, sender);
                    }
                }
            }
        }
    }

    private boolean isBankSender(String sender) {
        return sender != null && sender.matches(".*[a-zA-Z].*");
    }

    private void checkForMoney(Context context, String text, String sender) {
        final String PATTERN = "(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+\\.?\\d*)";
        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(text);

        if (m.find()) {
            try {
                String raw = m.group(1).replace(",", "");
                double amount = Double.parseDouble(raw);
                
                boolean isDebit;
                String lowerCaseText = text.toLowerCase();
                if (lowerCaseText.contains("credited") || lowerCaseText.contains("received") || lowerCaseText.contains("deposited")) {
                    isDebit = false;
                } else {
                    isDebit = true; // Default to debit
                }

                String merchantName = "SMS: " + sender;
                final String MERCHANT_PATTERN = "(?i)(?:to|from|sent to|paid to|paying)\\s+([A-Za-z0-9\\s\\.\\-&'@]+?)(?:\\s+on|\\s+at|\\s*\\.|\\s*|$)";
                Pattern merchantPattern = Pattern.compile(MERCHANT_PATTERN);
                Matcher merchantMatcher = merchantPattern.matcher(text);

                if (merchantMatcher.find()) {
                    merchantName = merchantMatcher.group(1).trim();
                    if (merchantName.contains("@")) {
                        merchantName = merchantName.split("@")[0];
                    }
                }

                saveToDatabase(context, amount, merchantName, isDebit);
            } catch (Exception e) {
                Log.e("SMS", "Error parsing", e);
            }
        }
    }

    private void saveToDatabase(Context context, double amount, String merchantName, boolean isDebit) {
        long currentTime = System.currentTimeMillis();
        long checkTime = currentTime - 60000; // 1 minute window for duplicate check
        TransactionDAO dao = AppDatabase.getDatabase(context).transactionDAO();

        Transaction existing = dao.checkDuplicate(amount, checkTime);
        if (existing != null) {
            Log.d(TAG, "saveToDatabase: Duplicate Transaction found, ignoring.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, h:mm a", Locale.getDefault());
        String formattedTime = sdf.format(new Date(currentTime));

        Transaction transaction = new Transaction(amount, merchantName, formattedTime, currentTime, isDebit);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            dao.insertTransaction(transaction);
            Log.d(TAG, "💰 SMS Money Saved: " + amount + " | Merchant: " + merchantName + " | isDebit: " + isDebit);
        });
    }
}
