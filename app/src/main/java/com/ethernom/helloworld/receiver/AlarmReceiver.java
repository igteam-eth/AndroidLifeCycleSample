package com.ethernom.helloworld.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.workmanager.AlarmWorkManager;


public class AlarmReceiver extends BroadcastReceiver  {

    private static final String TAG = "AlarmReceiver";
    private static long interval = 1;
    private static long count = 0;


    // here you need to receive some action from intent and depending on this action start service or set repeating alarm

    public void onReceive(Context context, Intent intent) {

        try {

            if (TrackerSharePreference.getConstant(context).isCardRegistered()){

                if(count >= 2) {
                    interval = 30;
                }
                count ++;
                MyApplication.appendLog(MyApplication.getCurrentDate() + " : AlarmReceiver onReceive " + "\n");
                //OneTimeWorkRequest
                OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(AlarmWorkManager.class)
                        .addTag("ALARM_WORK_MANAGER")
                        .build();
                WorkManager.getInstance(context).enqueue(oneTimeRequest);

                MyApplication.appendLog(MyApplication.getCurrentDate() + " : AlarmReceiver onReceive launch worker thread(AlarmWorkManager)  " + "\n");

                Intent mIntent = new Intent(context, AlarmReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        context, 0,
                        mIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                MyApplication.appendLog(MyApplication.getCurrentDate() + " : AlarmReceiver : Create an Alarm in " +interval+ " secs \n");
                AlarmManager alarmService = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (alarmService != null)
                    alarmService.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + ( interval) * 1000, pendingIntent);

            }

        } catch (Throwable e) {
            Log.d(TAG, Log.getStackTraceString(e));
        }
    }
}