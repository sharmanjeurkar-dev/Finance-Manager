package com.example.upi_expense_tracker.services;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;



public class ScreenReader extends AccessibilityService {

    private static final String TAG = "UPI_ACCESSIBILITY";


    //Main brain
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Used for accessing the root node whenever screen is changed
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

        //If screen not changed
        if(rootNode == null) return;

        //To get text from the child clusters in the root node
        String textOnScreen = getAllTextFromNode(rootNode);

        if(     textOnScreen.toLowerCase().contains("successful") ||
                textOnScreen.toLowerCase().contains("paid successfully")
          )
        {
            Log.d(TAG, "onAccessibilityEvent: Screen Detected");
            Log.d(TAG, "onAccessibilityEvent: Raw Screen Text Extracted" + textOnScreen);
        }



    }

    //Gets all the text from the screen
    private String getAllTextFromNode(AccessibilityNodeInfo rootNode) {
        //Checks if root node is null or not or in recursive implication if the child is empty or not ---> base condition
        if(rootNode == null) return "";

        StringBuilder sb = new StringBuilder();

        //Actual logic for extraction of text from the child
        if(rootNode.getText() != null)
            sb.append(rootNode.getText()).append(" ");

        //Used for accessing number of child groups or individual child nodes( after recursion)
        for(int i = 0; i<rootNode.getChildCount();i++){

            // To check if their if their is a child inside child node
            AccessibilityNodeInfo child = rootNode.getChild(i);
            if(child!=null){
                //Recursion
                sb.append(getAllTextFromNode(child));
            }
        }

        return sb.toString();
    }



    @Override
    public void onInterrupt() {

    }
}
