package com.ethernom.helloworld.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;

public class NotificationDismissedReceiver extends BroadcastReceiver {

    private String TAG = "NotificationDismissedReceiver";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            boolean isNotificationDismiss = intent.getBooleanExtra("NOTIFICATION_DISMISS", false);
            if (isNotificationDismiss){
                Log.d(TAG, "Notification dismiss");
                TrackerSharePreference.getConstant(context).setAlreadyCreateWorkerThread (false);
                MyApplication.appendLog(MyApplication.getCurrentDate() +" : User dismiss notification so wee set : isAlreadyCreateWorkerThread = false\n");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
