package com.ethernom.helloworld.workmanager;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.receiver.BeaconReceiver;

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
        
        Log.d("APP_MyWorkManager", "doWork");
        MyApplication.saveLogWithCurrentDate("MyWorkManager(Worker thread) in doWork");

        // Check if not yet  Already Create Worker Thread to start scan
        if (!TrackerSharePreference.getConstant(mContext).isAlreadyCreateWorkerThread()) {
            BeaconReceiver.startScan(mContext);
        }

        return Result.success();
    }
}
