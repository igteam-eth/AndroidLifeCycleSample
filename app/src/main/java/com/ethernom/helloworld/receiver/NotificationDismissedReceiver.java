package com.ethernom.helloworld.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.statemachine.BeaconRegistration;
import com.ethernom.helloworld.statemachine.RingNotificationState;

public class NotificationDismissedReceiver extends BroadcastReceiver {

    private String TAG = "NotificationDismissedReceiver";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Notification dismiss");
        try {
            boolean isNotificationDismiss = intent.getBooleanExtra("NOTIFICATION_DISMISS", false);
            if (isNotificationDismiss){
               new RingNotificationState(context).notificationSwipeEvent();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
