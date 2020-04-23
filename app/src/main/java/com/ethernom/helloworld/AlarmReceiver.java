package com.ethernom.helloworld;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;


public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    // here you need to receive some action from intent and depending on this action start service or set repeating alarm
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceive(Context context, Intent intent) {

        try {

            MyApplication.appendLog(MyApplication.getCurrentDate() + " : AlarmReceiver onReceive " + "\n");

            //OneTimeWorkRequest
            OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(AlarmWorkManager.class)
                    .addTag("WORK_MANAGER")
                    .build();
            WorkManager.getInstance(context).enqueue(oneTimeRequest);

            MyApplication.appendLog(MyApplication.getCurrentDate() + " : AlarmReceiver onReceive launch worker thread(AlarmWorkManager)  " + "\n");

            Intent mIntent = new Intent(context, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0,
                    mIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            MyApplication.appendLog(MyApplication.getCurrentDate() + " : AlarmReceiver : Create an Alarm in 30 secs \n");
            AlarmManager alarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmService != null)
                alarmService.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ( 30) * 1000, pendingIntent);
        } catch (Throwable e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }
}