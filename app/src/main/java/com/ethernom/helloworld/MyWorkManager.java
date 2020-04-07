package com.ethernom.helloworld;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


public class MyWorkManager extends Worker {

    private Context mContext;
    public MyWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Result doWork() {


        MyApplication.appendLog("Start MyWorkManager    " + MyApplication.getCurrentDate()+"\n");

        Intent intent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, 0,
                intent,
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmService = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        alarmService.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (5 * 60 * 1000), pendingIntent);

        MyApplication.appendLog("Start Alarm In MyWorkManager at :     "+ MyApplication.getCurrentDate()+"\n");
        MyApplication.appendLog("Current Alarm Create with Period Time:  5mins  " + MyApplication.getCurrentDate()+"\n");

        return Result.success();
    }
}
