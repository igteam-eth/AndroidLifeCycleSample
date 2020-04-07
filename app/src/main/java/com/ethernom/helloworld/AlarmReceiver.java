package com.ethernom.helloworld;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class AlarmReceiver extends BroadcastReceiver {


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        MyApplication.appendLog("AlarmReceiver onReceive Scan Counter:   " + TrackerSharePreference.getConstant(context).getScanCounter()+ "    " + MyApplication.getCurrentDate()+"\n");

        if (TrackerSharePreference.getConstant(context).getScanCounter() == 0){
            BleReceiver.startScan(context);
            TrackerSharePreference.getConstant(context).setScanCounter(TrackerSharePreference.getConstant(context).getScanCounter() + 1);
        }

        Intent mIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0,
                mIntent,
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmService = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmService.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (5 * 60 * 1000), pendingIntent);

        MyApplication.appendLog("Start Alarm In AlarmReceiver at :     "+ MyApplication.getCurrentDate()+"\n");
        MyApplication.appendLog("Current Alarm Create with Period Time:  5mins     " + MyApplication.getCurrentDate()+"\n");

    }
}
