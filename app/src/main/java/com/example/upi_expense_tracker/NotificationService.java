package com.example.upi_expense_tracker;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Service: Which reads the notifications in the status bar and infer it's text
 *
 **/
public class NotificationService extends NotificationListenerService {

    private  static final String TAG = "UPI DEBUG";

    public void onListenerConnected(){
        super.onListenerConnected();
        Log.d(TAG,"Service started! Listener Activated");
    }

    public void onListenerDisconnected(){
        super.onListenerConnected();
        Log.d(TAG,"Service ended Listener Deactivated");
    }


    @Override
    public void onNotificationPosted(StatusBarNotification sbn){

        //Only filtering useful packages for the service

        //GPAY package
        String GPAY_PACKAGE = "com.google.android.apps.nbu.paisa.user";
        //Whatsapp Package only for testing
        String WHATSAPP_PACKAGE = "com.whatsapp";

        if(sbn.getPackageName()!=GPAY_PACKAGE && sbn.getPackageName() != WHATSAPP_PACKAGE){
            return;
        }

        String text = "";
        if(sbn.getNotification().extras.getCharSequence("android.text")!=null){
            text = sbn.getNotification().extras.getCharSequence("android.text").toString();
        }

        if(text.isEmpty())
            return;

        Log.d(TAG, "Analyzing msg from: "+sbn.getPackageName());
        Log.d(TAG, "Content: "+text);

        final String PATTERN = "(?i)(?:rs\\.?|INR|₹)\\s*([\\d,]+\\.?\\d*)";//Regex format

        Pattern p = Pattern.compile(PATTERN);//Regex format given for the compiler to understand and decode
        Matcher m = p.matcher(text);// Given the text from msg to compare with the pattern and parse through regex format

        if(m.find()){
            String rawAmount = m.group(1); //Finds for the number after parsing the msg if in regex format
                if (rawAmount!=null){
                    String finalAmount = rawAmount.replace(",","");// Removes Comas
                    try{
                        double amount = Double.parseDouble(finalAmount);//Converts to double
                        Log.d(TAG,"Transaction found of amount: " + amount);
                    } catch (NumberFormatException e) {
                        Log.d(TAG, "Amount found not in right format");
                    }
                }
        }

    }
    @Override
    public void  onNotificationRemoved(StatusBarNotification sbn){
        //Code
    }
}
