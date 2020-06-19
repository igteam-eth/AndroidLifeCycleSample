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

public class BeaconRegistration {

    // Waiting for beacon
    @SuppressLint("SimpleDateFormat")
    public void launchBLEScan(Context context) throws ParseException {

        MyApplication.saveLogWithCurrentDate("launchBLEScan");

        TrackerSharePreference trackerSharePreference = TrackerSharePreference.getConstant(context);
        // Check if not yet  Already Create Worker Thread to start scan
        if (!trackerSharePreference.isAlreadyCreateWorkerThread()) {
            byte numDelay = 0;

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
            MyApplication.saveLogWithCurrentDate("Delay For Enqueue WorkManager : "+ numDelay);
            MyApplication.saveLogWithCurrentDate("Enqueue MyWorkManager");

            // Create work manager to call start scan for detect beacon inside
            // OneTimeWorkRequest
            OneTimeWorkRequest oneTimeRequest = new OneTimeWorkRequest.Builder(MyWorkManager.class)
                .addTag("WORK_MANAGER")
                    .setInitialDelay(numDelay, TimeUnit.SECONDS)
                    .build();
            WorkManager.getInstance(context).enqueue(oneTimeRequest);
        }else{
            MyApplication.saveLogWithCurrentDate("BLE already start scan");
        }

        // Host model is SAMSUNG  start alarm manager
        if (Build.BRAND.equalsIgnoreCase("samsung")) {
            // check if not Already Create Alarm
            if (!trackerSharePreference.isAlreadyCreateAlarm()) {
                MyApplication.saveLogWithCurrentDate("Periodic Alarm for Samsung created");
                trackerSharePreference.setAlreadyCreateAlarm(true);
                Intent startIntent = new Intent(context, AlarmReceiver.class);
                context.sendBroadcast(startIntent);
            }
        }
    }
}
