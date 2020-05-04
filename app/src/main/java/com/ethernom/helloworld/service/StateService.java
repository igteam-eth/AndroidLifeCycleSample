package com.ethernom.helloworld.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class StateService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("StateService", "onStartCommand");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}