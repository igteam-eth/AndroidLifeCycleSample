package com.ethernom.helloworld.receiver

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.util.Log

import androidx.annotation.RequiresApi
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.MyApplication.showSilentNotificationBLE
import com.ethernom.helloworld.application.MyApplication.showSilentNotificationLocation

import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.statemachine.WaitingForBeaconState
import com.ethernom.helloworld.util.StateMachine


class StartupReceiver : BroadcastReceiver() {

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Boot completed onReceive")

        when (intent.action) {
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON",
            Intent.ACTION_BOOT_COMPLETED -> {

                Log.i(TAG, "Boot completed (" + intent.action + ")")
                MyApplication.saveLogWithCurrentDate("Boot Completed")
                MyApplication.saveCurrentStateToLog(context)

                val trackerSharePreference = TrackerSharePreference.getConstant(context)

                if (trackerSharePreference.isCardRegistered!!) {

                    if (!trackerSharePreference.isBLEStatus && !trackerSharePreference.isLocationStatus) {
                        //Notify user to turn on both BLE and Location
                        trackerSharePreference.currentState = StateMachine.WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE.value
                        showSilentNotificationBLE(context)
                        return
                    }else if (!trackerSharePreference.isBLEStatus) {
                        //Notify user to enable BLE
                        trackerSharePreference.currentState = StateMachine.WAITING_FOR_BEACON_BLE_OFF_STATE.value
                        showSilentNotificationBLE(context)
                        return
                    }else if (!trackerSharePreference.isLocationStatus) {
                        //Notify user to enable location
                        trackerSharePreference.currentState = StateMachine.WAITING_FOR_BEACON_LOCATION_OFF_STATE.value
                        showSilentNotificationLocation(context)
                        return
                    }else{

                        //Launch BLE Scan Intent
                        TrackerSharePreference.getConstant(context).currentState = StateMachine.WAITING_FOR_BEACON.value
                        trackerSharePreference.isAlreadyCreateWorkerThread = false
                        trackerSharePreference.isAlreadyCreateAlarm = false
                        WaitingForBeaconState(context).launchBLEScan()
                    }
                }
            }
        }

        // Register for broadcasts on Bluetooth state change
        val btIntentFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(BluetoothStateChangeReceiver(), btIntentFilter)

        // Register for broadcasts on Location state change
        val filterLocation = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        context.registerReceiver(LocationStateChangeReceiver(), filterLocation)
    }

    companion object {

        private const val TAG = "StartupReceiver"
    }
}

