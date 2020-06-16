package com.ethernom.helloworld.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

public class BLEAndLocationRegisterManager extends AlarmReceiver {

    private String TAG = BLEAndLocationRegisterManager.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "OnReceive");
        super.onReceive(context, intent);
        // Register for broadcasts on Bluetooth state change
        IntentFilter btIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.getApplicationContext().registerReceiver(new BluetoothStateChangeReceiver(), btIntentFilter);

        // Register for broadcasts on Location state change
        IntentFilter filterLocation = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
        context.getApplicationContext().registerReceiver(new LocationStateChangeReceiver(), filterLocation);
    }
}
