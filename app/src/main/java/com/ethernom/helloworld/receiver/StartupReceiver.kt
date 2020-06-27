package com.ethernom.helloworld.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import androidx.annotation.RequiresApi
import com.ethernom.helloworld.application.MyApplication

import com.ethernom.helloworld.application.TrackerSharePreference

class StartupReceiver : BroadcastReceiver() {

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Boot completed onReceive")

        when (intent.action) {
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_BOOT_COMPLETED -> {

                // Show notification to user for launch tracker app
                MyApplication.requiredLaunchAppNotification(context)
                MyApplication.saveLogWithCurrentDate("Boot Completed")

                val trackerSharePreference = TrackerSharePreference.getConstant(context)
                trackerSharePreference.isAlreadyCreateWorkerThread = false
                trackerSharePreference.isAlreadyCreateAlarm = false
                trackerSharePreference.setBeaconTimestamp("")

            }
        }
    }

    companion object {
        private const val TAG = "StartupReceiver"
    }
}

