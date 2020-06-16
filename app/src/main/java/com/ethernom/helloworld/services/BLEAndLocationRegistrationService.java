package com.ethernom.helloworld.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ethernom.helloworld.receiver.BLEAndLocationRegisterManager;

public class BLEAndLocationRegistrationService extends Service {

    private String TAG = BLEAndLocationRegistrationService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");

        //getApplicationContext().sendBroadcast(new Intent(getApplicationContext(), BLEAndLocationRegisterManager.class));

    }
}
