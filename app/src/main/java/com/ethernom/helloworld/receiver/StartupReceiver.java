package com.ethernom.helloworld.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.receiver.BluetoothStateChangeReceiver;
import com.ethernom.helloworld.util.StateMachine;


public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context sContext, Intent intent) {
        Log.i(TAG, "Boot completed onReceive");

        switch (intent.getAction()) {

            case "android.intent.action.QUICKBOOT_POWERON":
            case "com.htc.intent.action.QUICKBOOT_POWERON":
            case Intent.ACTION_BOOT_COMPLETED: {

                Log.i(TAG, "Boot completed (" + intent.getAction() + ")");
                MyApplication.appendLog(MyApplication.getCurrentDate() + " : Boot completed (" + intent.getAction() + ") \n");

                MyApplication.appendLog(MyApplication.getCurrentDate() + " : Before Boot State : " + TrackerSharePreference.getConstant(sContext).getCurrentState() + " \n");

                MyApplication.appendLog(MyApplication.getCurrentDate() + " : Is Card Register: " + TrackerSharePreference.getConstant(sContext).isCardRegistered() + "  \n");


                //set current state to initialization
                TrackerSharePreference.getConstant(sContext).setCurrentState(StateMachine.INITIAL.getValue());
                MyApplication.appendLog(MyApplication.getCurrentDate() + " : Current State : " + TrackerSharePreference.getConstant(sContext).getCurrentState() + " \n");
                if(TrackerSharePreference.getConstant(sContext).isCardRegistered()) {
                    // Go to Beacon receive (2000) state
                    TrackerSharePreference.getConstant(sContext).setCurrentState(StateMachine.WAITING_FOR_BEACON.getValue());
                } else {
                    // Go to Card discover (1001) state
                    TrackerSharePreference.getConstant(sContext).setCurrentState(StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.getValue());
                }
                Log.e("EthernomHelloworld","Current State:"+ TrackerSharePreference.getConstant(sContext).getCurrentState());

                MyApplication.appendLog(MyApplication.getCurrentDate() + " : Current State : "+ TrackerSharePreference.getConstant(sContext).getCurrentState() + " \n");

                // Register for broadcasts on BluetoothAdapter state change
                IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                sContext.getApplicationContext().registerReceiver(new BluetoothStateChangeReceiver(), filter);

            }
        }
    }
}
