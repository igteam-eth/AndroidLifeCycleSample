package com.ethernom.helloworld.receiver;

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

import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.workmanager.AlarmWorkManager;
import com.ethernom.helloworld.application.MyApplication;


public class AlarmReceiver extends BroadcastReceiver  {

    private static final String TAG = "AlarmReceiver";
    private static final long interval = 30;

    // here you need to receive some action from intent and depending on this action start service or set repeating alarm
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceive(Context context, Intent intent) {

        try {


            if (TrackerSharePreference.getConstant(context).isCardExisted()){

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