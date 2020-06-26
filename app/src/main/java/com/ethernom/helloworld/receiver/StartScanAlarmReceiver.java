package com.ethernom.helloworld.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.application.MyApplication;

public class StartScanAlarmReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        MyApplication.saveLogWithCurrentDate("StartScanAlarmReceiver called");
        Log.d("StartScanAlarmReceiver","StartScanAlarmReceiver called");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                BeaconReceiver.startScan(context.getApplicationContext());
            }
        }, 8000);
    }
}
