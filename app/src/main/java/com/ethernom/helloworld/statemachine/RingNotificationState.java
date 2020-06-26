package com.ethernom.helloworld.statemachine;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.screens.SplashScreenActivity;

public class RingNotificationState {
    private Context context;
    private String TAG = "RingNotificationState";
    TrackerSharePreference trackerSharePreference;

    public RingNotificationState(Context context) {
        this.context = context;
        trackerSharePreference = TrackerSharePreference.getConstant(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void appSwipeEvent(){
        Log.d(TAG, "appSwipeEvent");
        MyApplication.saveLogWithCurrentDate("App Swipe Event");
        initState();

        /*BeaconReceiver.stopSound();
        Utils.removeNotificationByID(context, Utils.CHANNEL_RANG);
        new BeaconRegistration().launchBLEScan(context);
        createAlarmForSamsung();*/

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notificationSwipeEvent(){
        Log.d(TAG, "notificationSwipeEvent");
        MyApplication.saveLogWithCurrentDate("Notification Swipe Event");
        initState();

       /* BeaconReceiver.stopSound();
        new BeaconRegistration().launchBLEScan(context);
        createAlarmForSamsung();*/
    }


    private void initState() {
        /*Intent intent = new  Intent(context, SplashScreenActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.getApplicationContext().startActivity(intent);*/

        Intent launchIntent = context.getApplicationContext().getPackageManager().getLaunchIntentForPackage("com.ethernom.alarmhelloworld.v12");
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.getApplicationContext().startActivity( launchIntent );

        Log.d(TAG, "initState");

    }
}
