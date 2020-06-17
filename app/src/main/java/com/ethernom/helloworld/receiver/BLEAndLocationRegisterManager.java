package com.ethernom.helloworld.receiver;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

import com.ethernom.helloworld.application.MyApplication;

public class BLEAndLocationRegisterManager extends BroadcastReceiver {

    private String TAG = BLEAndLocationRegisterManager.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        MyApplication.saveLogWithCurrentDate("BLEAndLocationRegisterManager onReceive");

        Log.d(TAG, "OnReceive");
        // Register for broadcasts on Bluetooth state change
        IntentFilter btIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.getApplicationContext().registerReceiver(new BluetoothStateChangeReceiver(), btIntentFilter);

        // Register for broadcasts on Location state change
        IntentFilter filterLocation = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
        context.getApplicationContext().registerReceiver(new LocationStateChangeReceiver(), filterLocation);
    }
}
