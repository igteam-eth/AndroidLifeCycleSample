package com.ethernom.helloworld.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.SettingSharePreference
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.screens.DiscoverDeviceActivity
import com.ethernom.helloworld.statemachine.InitializeState
import com.ethernom.helloworld.statemachine.WaitingForBeaconState
import com.ethernom.helloworld.util.StateMachine
import com.ethernom.helloworld.util.Utils


class LocationStateChangeReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "${Utils.isLocationEnabled(context)}")

        val currentState = TrackerSharePreference.getConstant(context).currentState
        if (Utils.isLocationEnabled(context)) {//true
            TrackerSharePreference.getConstant(context).isLocationStatus = true
            Utils.removeNotificationByID(context, Utils.CHANNEL_LOCATION_OFF)

            Log.e(TAG, "Location State on")
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Location State on \n")
            if (!SettingSharePreference.getConstant(context).isBeforeActivate){
                return
            }

            when (currentState) {
                StateMachine.CARD_DISCOVERY_BLE_LOCATION_OFF.value -> {
                    if (TrackerSharePreference.getConstant(context).isBLEStatus && MyApplication.isAppInForeground(
                            context
                        )
                    ) {
                        // StartScan General Advertising
                        // Display List(Empty)
                        val mIntent = Intent(context, DiscoverDeviceActivity::class.java)
                        mIntent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(mIntent)
                        TrackerSharePreference.getConstant(context).currentState =
                            StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.value
                    }
                }
                StateMachine.WAITING_FOR_BEACON_LOCATION_OFF_STATE.value -> {
                    if (TrackerSharePreference.getConstant(context).isBLEStatus) {
                        //Launch BLE Scan Intent
                        TrackerSharePreference.getConstant(context).currentState =
                            StateMachine.WAITING_FOR_BEACON.value
                        WaitingForBeaconState().launchBLEScan(context)
                    }
                }
                StateMachine.WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE.value -> {
                    if (!TrackerSharePreference.getConstant(context).isBLEStatus) {

                        TrackerSharePreference.getConstant(context).currentState =
                            StateMachine.WAITING_FOR_BEACON_BLE_OFF_STATE.value
                        //Notify User to turn on Bluetooth
                        MyApplication.showBluetoothNotification(context)

                    }
                }
            }

        } else {//false
            TrackerSharePreference.getConstant(context).isLocationStatus = false
            Log.e(TAG, "Location State off")
            // Notify user
            MyApplication.showLocationNotification(context)

            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Location State off \n")

            Log.e(TAG, "Current state : $currentState")
            when (currentState) {
                StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.value -> {
                    goToInitState(context)
                }
                StateMachine.CARD_REGISTER.value -> {
                    goToInitState(context)
                }
                StateMachine.WAITING_FOR_BEACON.value -> {
                    //Stop Scan
                    BeaconReceiver.stopScan()
                    TrackerSharePreference.getConstant(context).isAlreadyCreateWorkerThread = false
                    TrackerSharePreference.getConstant(context).isAlreadyCreateAlarm = false
                    goToInitState(context)
                }
                StateMachine.WAITING_FOR_BEACON_BLE_OFF_STATE.value -> {
                    //go to start 2003 WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE
                    TrackerSharePreference.getConstant(context).currentState =
                        StateMachine.WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE.value
                }
            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun goToInitState(context: Context) {
        TrackerSharePreference.getConstant(context).currentState = StateMachine.INITIAL.value
        // go to initial state
        // In Initial State class we study with input event , state variable and action function for intent to next state
        InitializeState().goToInitialState(context)

    }

    companion object {
        const val TAG = "LocationStateChange"
    }
}
