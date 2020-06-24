package com.ethernom.helloworld.statemachine;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.receiver.AlarmReceiver;
import com.ethernom.helloworld.receiver.BeaconReceiver;

public class RingNotificationState {
    private Context context;
    TrackerSharePreference trackerSharePreference;

    public RingNotificationState(Context context) {
        this.context = context;
        trackerSharePreference = TrackerSharePreference.getConstant(context);
    }

    public void appSwipeEvent(){
        MyApplication.saveLogWithCurrentDate("AppSwipe Event");
        trackerSharePreference.setAlreadyCreateWorkerThread(false);
        new BeaconRegistration().launchBLEScan(context);
        createAlarmForSamsung();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notificationSwipeEvent(){
        BeaconReceiver.stopSound();
        MyApplication.saveLogWithCurrentDate("Notification Swipe Event");
        TrackerSharePreference trackerSharePreference = TrackerSharePreference.getConstant(context);
        trackerSharePreference.setAlreadyCreateWorkerThread(false);
        new BeaconRegistration().launchBLEScan(context);
        createAlarmForSamsung();
    }


    private void createAlarmForSamsung() {
        // Host model is
        MyApplication.saveLogWithCurrentDate("BRAND: ${Build.BRAND}");

        // Host model is SAMSUNG  start alarm manager
        if (Build.BRAND.equalsIgnoreCase("samsung")) {
            // check if not Already Create Alarm
            if (!trackerSharePreference.isAlreadyCreateAlarm()) {
                MyApplication.saveLogWithCurrentDate("Periodic Alarm for Samsung created");
                trackerSharePreference.setAlreadyCreateAlarm(true);
                Intent startIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
                context.getApplicationContext().sendBroadcast(startIntent);
            }
        }
    }
}
