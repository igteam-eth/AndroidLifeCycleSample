package com.ethernom.helloworld.services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class StateService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("StateService", "onStartCommand");
        new CountDownTimer(100000,4000)
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {

            }
        }.start();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}