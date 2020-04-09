package com.ethernom.helloworld;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.ethernom.helloworld.receiver.BleReceiver;

public class BLEWorkerManager extends Worker {


    private Context mContext;
    private String TAG = "BLEWorkerManager";

    public BLEWorkerManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mContext = context;

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Result doWork() {

        Log.d(TAG, "doWork");

        BleReceiver.startScanning(mContext);

        return Result.success();
    }




}
