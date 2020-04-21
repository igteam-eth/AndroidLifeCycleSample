package com.ethernom.helloworld;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class AlarmReceiver extends BroadcastReceiver {


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("AlarmReceiver", "onReceive");
        MyApplication.appendLog(MyApplication.getCurrentDate()+ " : AlarmReceiver : onReceive\n");

        /*Intent mIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0,
                mIntent,
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmService = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmService.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (15 * 60 * 1000), pendingIntent);

        MyApplication.appendLog(MyApplication.getCurrentDate()+ " : AlarmReceiver : Create New Alarm in 15min\n");*/

    }
}
