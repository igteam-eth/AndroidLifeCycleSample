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

import static com.ethernom.helloworld.AlarmReceiver.periodList;

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

        BleReceiver.startScan(mContext);

        Intent intent = new Intent(mContext, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext, 0,
                intent,
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmService = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        alarmService.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (periodList.get(0)  * 1000), pendingIntent);

        MyApplication.appendLog("Start Alarm In MyWorkManager at :     "+ MyApplication.getCurrentDate()+"\n");
        MyApplication.appendLog("Current Alarm Create with Period Time:  "+ periodList.get(0)+"     " + MyApplication.getCurrentDate()+"\n");

        TrackerSharePreference.getConstant(mContext).setCurrentIndex(TrackerSharePreference.getConstant(mContext).getCurrentIndex()+1);



        return Result.success();
    }
}
