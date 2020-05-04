package com.ethernom.helloworld.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.workmanager.MyWorkManager;

public class BluetoothStateChangeReceiver extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action != null && action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.d("BluetoothStateChanger", "STATE_OFF");
                    MyApplication.appendLog(MyApplication.getCurrentDate()+" : BluetoothToggle STATE_OFF");
                    BleReceiver.stopScan();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d("BluetoothStateChanger", "STATE_TURNING_OFF");
                    MyApplication.appendLog(MyApplication.getCurrentDate()+" : BluetoothToggle STATE_TURNING_OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d("BluetoothStateChanger", "STATE_ON");
                    MyApplication.appendLog(MyApplication.getCurrentDate()+" : BluetoothToggle STATE_ON");

                     //OneTimeWorkRequest
                    OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(MyWorkManager.class)
                        .addTag("WORK_MANAGER")
                        .build();
                    WorkManager.getInstance(context).enqueue(oneTimeRequest);
                    TrackerSharePreference.getConstant(context).setAlreadyCreateWorkerThread(true);

                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d("BluetoothStateChanger", "STATE_TURNING_ON");
                    MyApplication.appendLog(MyApplication.getCurrentDate()+" : BluetoothToggle STATE_TURNING_ON");
                    break;
            }
        }
    }
}
