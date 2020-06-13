package com.ethernom.helloworld.statemachine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.receiver.AlarmReceiver;
import com.ethernom.helloworld.util.Utils;
import com.ethernom.helloworld.workmanager.MyWorkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class WaitingForBeaconState {
    private Context context;

    public WaitingForBeaconState(Context context) {
        this.context = context;
    }

    // Waiting for beacon
    @SuppressLint("SimpleDateFormat")
    public void launchBLEScan() throws ParseException {
        // Check if not yet  Already Create Worker Thread to start scan
        if (!TrackerSharePreference.getConstant(context).isAlreadyCreateWorkerThread()) {
            byte numDelay = 0;
            TrackerSharePreference.getConstant(context).setAlreadyCreateWorkerThread(true);
            MyApplication.appendLog(MyApplication.getCurrentDate()+ " : Enqueue WorkManager\n");

            if (!TrackerSharePreference.getConstant(context).isBeaconTimeStamp().equals("")) {
                long diffInMs = Objects.requireNonNull(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").parse(MyApplication.getCurrentDate())).getTime() - Objects.requireNonNull(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").parse(TrackerSharePreference.getConstant(context).isBeaconTimeStamp())).getTime();
                byte diffInSec = (byte) TimeUnit.MILLISECONDS.toSeconds(diffInMs);

                Log.d(TAG, "Seconds: "+ diffInSec);


                if (diffInSec >= Utils.BEACON_DELAY_PERIOD) {
                    numDelay = 0;
                } else {
                    numDelay = (byte) (Utils.BEACON_DELAY_PERIOD - diffInSec);
                }
            }

            Log.d(TAG, "Delay Seconds:"+ numDelay);

            // Create work manager to call start scan for detect beacon inside
            // OneTimeWorkRequest
            OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(MyWorkManager.class)
                .addTag("WORK_MANAGER")
                    .setInitialDelay(numDelay, TimeUnit.SECONDS)
                    .build();
            WorkManager.getInstance(context).enqueue(oneTimeRequest);
        }

        MyApplication.appendLog(MyApplication.getCurrentDate() +" : Host brand " + Build.BRAND + "\n");

        // Host model is SAMSUNG  start alarm manager
        if (Build.BRAND.equalsIgnoreCase("samsung")) {
            MyApplication.appendLog(MyApplication.getCurrentDate() +" : Alarm Enabled \n");

            // check if not Already Create Alarm
            if (!TrackerSharePreference.getConstant(context).isAlreadyCreateAlarm()) {
                TrackerSharePreference.getConstant(context).setAlreadyCreateAlarm(true);
                Intent startIntent = new Intent(context, AlarmReceiver.class);
                context.sendBroadcast(startIntent);
            }
        }
    }


}
