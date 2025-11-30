package com.example.upi_expense_tracker;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 *
 * Service: Which reads the notifications in the status bar and infer it's text
 *
 **/
public class NotificationService extends NotificationListenerService {

    private  static final String TAG = "UPI DEBUG";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        String package_name = sbn.getPackageName();
        String title = sbn.getNotification().extras.getString("android.title");
        String text = "";

        if(sbn.getNotification().extras.getCharSequence("android.text")!=null){
            text = sbn.getNotification().extras.getCharSequence("android.text").toString();
        }

        Log.d(TAG,"------------------------------------------");
        Log.d(TAG,"Notification Sent By: "+package_name) ;
        Log.d(TAG,"Notification title: "+title);
        Log.d(TAG,"Notification Text: "+text);
        Log.d(TAG,"------------------------------------------");

    }
    @Override
    public void  onNotificationRemoved(StatusBarNotification sbn){
        //Code
    }
}
