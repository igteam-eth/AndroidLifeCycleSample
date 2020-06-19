package com.ethernom.helloworld.workmanager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.receiver.BluetoothStateChangeReceiver;
import com.ethernom.helloworld.receiver.LocationStateChangeReceiver;

public class IntentBLEAndLocationStatusWorkManager extends Worker {

    private Context mContext;
    public IntentBLEAndLocationStatusWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Result doWork() {

        Log.d("BLEAndLCWorkManager", "doWork");

        MyApplication.appendLog(MyApplication.getCurrentDate()+" : IntentBLEAndLocationStatusWorkManager (Worker thread) in doWork  "  +"\n");
        // Every initialize state we need to Launch BLE & Location Status Intent for tracker state of Bluetooth & Location state

        // Register for broadcasts on Bluetooth state change
        IntentFilter btIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.mContext.registerReceiver(new BluetoothStateChangeReceiver(), btIntentFilter);

        // Register for broadcasts on Location state change
        IntentFilter filterLocation = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
        this.mContext.registerReceiver(new LocationStateChangeReceiver(), filterLocation);

        return Result.success();
    }
}
