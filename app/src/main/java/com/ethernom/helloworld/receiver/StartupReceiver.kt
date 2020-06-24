package com.ethernom.helloworld.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import androidx.annotation.RequiresApi
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.MyApplication.showBluetoothNotification
import com.ethernom.helloworld.application.MyApplication.showLocationNotification

import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.statemachine.BeaconRegistration
import com.ethernom.helloworld.util.StateMachine
import com.ethernom.helloworld.workmanager.IntentBLEAndLocationStatusWorkManager

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




//                if (trackerSharePreference.isCardRegistered!!) {
//
//                    if (!trackerSharePreference.isBLEStatus && !trackerSharePreference.isLocationStatus) {
//                        //Notify user to turn on both BLE and Location
//                        showBluetoothNotification(context)
//                        trackerSharePreference.currentState = StateMachine.WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE.value
//                        return
//                    }else if (!trackerSharePreference.isBLEStatus) {
//                        //Notify user to enable BLE
//                        showBluetoothNotification(context)
//                        trackerSharePreference.currentState = StateMachine.WAITING_FOR_BEACON_BLE_OFF_STATE.value
//                        return
//                    }else if (!trackerSharePreference.isLocationStatus) {
//                        //Notify user to enable location
//                        showLocationNotification(context)
//                        trackerSharePreference.currentState = StateMachine.WAITING_FOR_BEACON_LOCATION_OFF_STATE.value
//                        return
//                    } else {
//                        //Launch BLE Scan Intent
//                        trackerSharePreference.isAlreadyCreateWorkerThread = false
//                        trackerSharePreference.isAlreadyCreateAlarm = false
//                        BeaconRegistration().launchBLEScan(context)
//
//                        // Host model is
//                        MyApplication.saveLogWithCurrentDate("BRAND: ${Build.BRAND}")
//
//                        // Host model is SAMSUNG  start alarm manager
//                        if (Build.BRAND.equals("samsung", ignoreCase = true)) {
//                            // check if not Already Create Alarm
//                            if (!trackerSharePreference.isAlreadyCreateAlarm) {
//                                MyApplication.saveLogWithCurrentDate("Periodic Alarm for Samsung created")
//                                trackerSharePreference.isAlreadyCreateAlarm = true
//                                val startIntent = Intent(context, AlarmReceiver::class.java)
//                                context.sendBroadcast(startIntent)
//                            }
//                        }
//                    }
//                }

                // Create BLE_LOCATION_WORK_MANAGER to Intent Location & BLE status
                // Every initialize state we need to Launch BLE & Location Status Intent for tracker state of Bluetooth & Location state
                // OneTimeWorkRequest
                val oneTimeRequest =
                    OneTimeWorkRequest.Builder(IntentBLEAndLocationStatusWorkManager::class.java)
                        .addTag("BLE_LOCATION_WORK_MANAGER").build()
                WorkManager.getInstance(context).enqueue(oneTimeRequest)
            }
        }
    }

    companion object {
        private const val TAG = "StartupReceiver"
    }
}

