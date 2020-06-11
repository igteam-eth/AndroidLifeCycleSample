package com.ethernom.helloworld.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.screens.DiscoverDeviceActivity
import com.ethernom.helloworld.screens.LocationBLENotifyUserActivity
import com.ethernom.helloworld.util.StateMachine
import com.ethernom.helloworld.util.Utils


class LocationStateChangeReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "${Utils.isLocationEnabled(context)}")

        val currentState = TrackerSharePreference.getConstant(context).currentState
        if (Utils.isLocationEnabled(context)) {//true
            TrackerSharePreference.getConstant(context).isLocationStatus = true
            Log.e(TAG, "Location State on")
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Location State on \n")

            when (currentState) {
                StateMachine.CARD_DISCOVERY_BLE_LOCATION_OFF.value -> {
                    if (TrackerSharePreference.getConstant(context).isBLEStatus && MyApplication.isAppInForeground(context)) {
                        //StartScan General Advertising
                        //Display List(Empty)
                        val mIntent = Intent(context, DiscoverDeviceActivity::class.java)
                        mIntent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(mIntent)
                        TrackerSharePreference.getConstant(context).currentState =
                            StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.value
                    }
                }
                StateMachine.WAITING_FOR_BEACON_LOCATION_OFF_STATE.value -> {
                    if (TrackerSharePreference.getConstant(context).isBLEStatus){
                        //TODO : Launch BLE Scan Intent
                    }
                }
                StateMachine.WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE.value ->{
                    if (!TrackerSharePreference.getConstant(context).isBLEStatus){
                        //TODO : Notify User to turn on Bluetooth
                        if (MyApplication.isAppInForeground(context)){
                            Log.d("BleReceiver", "AppInForeground");
                            val i =  Intent(context, LocationBLENotifyUserActivity::class.java);
                            i.putExtra("BLELocation", "ble")
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(i);
                        }else{
                            MyApplication.showSilentNotificationBLE(context);
                            Log.d("BleReceiver", "showNotification");
                        }
                        TrackerSharePreference.getConstant(context).currentState = StateMachine.WAITING_FOR_BEACON_BLE_OFF_STATE.value
                    }
                }
            }

        } else {//false
            TrackerSharePreference.getConstant(context).isLocationStatus = false
            Log.e(TAG, "Location State off")
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Location State off \n")

            when (currentState) {
                StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.value -> {
                    goToInitState(context)
                }
                StateMachine.CARD_REGISTER.value -> {
                    goToInitState(context)
                }
                StateMachine.WAITING_FOR_BEACON.value -> {
                    //TODO : Stop scan
                    //TODO : Go init state
                }
                StateMachine.WAITING_FOR_BEACON_BLE_OFF_STATE.value -> {
                    //go to start 2003 WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE
                    TrackerSharePreference.getConstant(context).currentState =
                        StateMachine.WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE.value
                }
            }

        }

    }

    private fun goToInitState(context: Context) {
        TrackerSharePreference.getConstant(context).currentState = StateMachine.INITIAL.value
        Utils.initState(context)
    }

    companion object {
        const val TAG = "LocationStateChange"
    }
}
