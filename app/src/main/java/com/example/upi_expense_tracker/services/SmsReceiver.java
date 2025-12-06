package com.example.upi_expense_tracker.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony; // Import this
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.upi_expense_tracker.data.AppDatabase;
import com.example.upi_expense_tracker.data.Transaction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsReceiver extends BroadcastReceiver {

    private final String TAG = "UPI SMS";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            // MODERN WAY: Let Android handle the PDU parsing automatically
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

            if (messages != null) {
                for (SmsMessage sms : messages) {
                    if (sms == null) continue;

                    String sender = sms.getDisplayOriginatingAddress();
                    String messageBody = sms.getDisplayMessageBody(); // Handles multi-part messages better

                    Log.d(TAG, "SMS From: " + sender);


                    if (isBankSender(sender)) {
                        checkForMoney(context, messageBody, sender);
                    }
                }
            }
        }
    }

    private boolean isBankSender(String sender) {
        // Simple check: Bank senders usually contain letters (e.g. "VM-SBIUPI")
        // Personal numbers are usually just digits.
        return sender != null && sender.matches(".*[a-zA-Z].*");
    }

    private void checkForMoney(Context context, String text, String sender) {
        // Same regex as Notification Listener
        final String PATTERN = "(?i)(?:rs\\.?|inr|₹)\\s*([\\d,]+\\.?\\d*)";
        Pattern p = Pattern.compile(PATTERN);
        Matcher m = p.matcher(text);

        if (m.find()) {
            try {
                String raw = m.group(1).replace(",", "");
                double amount = Double.parseDouble(raw);

                // Only save debits/spends
                if (text.toLowerCase().contains("debited") ||
                        text.toLowerCase().contains("sent") ||
                        text.toLowerCase().contains("paid")) {

                    saveToDatabase(context, amount, "SMS: " + sender);
                }
            } catch (Exception e) {
                Log.e("SMS", "Error parsing", e);
            }
        }
    }

    private void saveToDatabase(Context context, double amount, String source) {
        Transaction transaction = new Transaction(
                amount,
                "Unknown Merchant",
                System.currentTimeMillis(),
                true
        );

        AppDatabase.databaseWriteExecutor.execute(() -> {
            AppDatabase.getDatabase(context)
                    .transactionDAO()
                    .insertTransaction(transaction);
            Log.d(TAG, "💰 SMS Money Saved: " + amount);
        });
    }
}