package com.ethernom.helloworld.servcie;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.receiver.BeaconReceiver;
import com.ethernom.helloworld.receiver.TaskRemoveAlarm;
import com.ethernom.helloworld.util.StateMachine;

public class AppSwipeEventService extends Service {

    private String TAG = "AppSwipeEvent";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "onTaskRemoved");

        if (StateMachine.RING_NOTIFICATION_STATE.getValue().equals(TrackerSharePreference.getConstant(this).getCurrentState())){
            BeaconReceiver.stopSound();
            getApplicationContext().sendBroadcast(new Intent(getApplicationContext(), TaskRemoveAlarm.class));
        }
    }
}
