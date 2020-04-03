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


    public static ArrayList<Integer> periodList = new ArrayList<Integer>() {
        {
            add(4*60);
            add(5*60);
            add(15*60);
            add(15*60);
            add(15*60);
            add(12*60);
            add(58*60);
            add(30);
            add(70*60);
            add(39*60);
            add(30);
            add(91*60);
            add(110*60);
            add(60*60);
            add(91*60);
            add(225*60);
            add(21*60);
            add(12*60);
            add(30);
            add(30);
            add(30);
            add(31);
            add(30);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        int index = TrackerSharePreference.getConstant(context).getCurrentIndex();

        if (index >= periodList.size()){
            index = 0;
            TrackerSharePreference.getConstant(context).setCurrentIndex(0);
        }

        Intent mIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0,
                mIntent,
                PendingIntent.FLAG_ONE_SHOT);


        AlarmManager alarmService = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmService.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (periodList.get(index) * 1000), pendingIntent);

        MyApplication.appendLog("Start Alarm In AlarmReceiver at :     "+ MyApplication.getCurrentDate()+"\n");
        MyApplication.appendLog("Current Alarm Create with Period Time:  "+ periodList.get(index)+"     " + MyApplication.getCurrentDate()+"\n");


        TrackerSharePreference.getConstant(context).setCurrentIndex(TrackerSharePreference.getConstant(context).getCurrentIndex()+1);



    }
}
