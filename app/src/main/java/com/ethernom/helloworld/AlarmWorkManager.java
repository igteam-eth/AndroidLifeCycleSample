package com.ethernom.helloworld;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class AlarmWorkManager extends Worker {

    private Context mContext;

    public AlarmWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.mContext = context;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Result doWork() {


        MyApplication.appendLog(MyApplication.getCurrentDate() + " : AlarmWorkManager doWork \n");

        Intent mIntent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, 0,
                mIntent,
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmService = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        //alarmService.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (15 *60 * 1000), pendingIntent);
        alarmService.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
        MyApplication.appendLog(MyApplication.getCurrentDate() + " : AlarmWorkManager : Create an Alarm in 15min\n");
        return Result.success();
    }
}
