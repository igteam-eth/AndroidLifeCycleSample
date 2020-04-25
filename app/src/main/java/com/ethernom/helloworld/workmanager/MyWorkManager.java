package com.ethernom.helloworld.workmanager;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.receiver.BleReceiver;


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

        MyApplication.appendLog(MyApplication.getCurrentDate()+" : MyWorkManager(Worker thread) in doWork  "  +"\n\n");

        BleReceiver.startScan(mContext);
        return Result.success();
    }
}
