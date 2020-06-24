package com.ethernom.helloworld.servcie;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.statemachine.RingNotificationState;

public class AppSwipeEvent extends Service {

    private String TAG = "AppSwipeEvent";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        MyApplication.saveLogWithCurrentDate("NotificationSwipeEvent onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved");
        new RingNotificationState(this).appSwipeEvent();
    }
}
