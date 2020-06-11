package com.ethernom.helloworld.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.screens.DiscoverDeviceActivity
import com.ethernom.helloworld.util.StateMachine
import com.ethernom.helloworld.util.Utils


class LocationStateChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "${Utils.isLocationEnabled(context)}")
        //true
        if (Utils.isLocationEnabled(context)) {
            TrackerSharePreference.getConstant(context).isLocationStatus = true
            Log.e(TAG, "Location State on")
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Location State on \n")

            if (TrackerSharePreference.getConstant(context).isCardRegistered) {
                // Launch BLE Scan Intent
            } else {

                if (Utils.isBluetoothEnable() && MyApplication.isAppInForeground(context)) {
                    //StartScan General Advertising
                    //Display List(Empty)
                    TrackerSharePreference.getConstant(context).currentState = StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.value
                    val mIntent =  Intent(context, DiscoverDeviceActivity::class.java)
                    mIntent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(mIntent)

                }
            }

        } else {//false
            TrackerSharePreference.getConstant(context).isLocationStatus = false
            Log.e(TAG, "Location State off")
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Location State off \n")

            if (TrackerSharePreference.getConstant(context).isCardRegistered) {
                //stop scan

            } else {
                TrackerSharePreference.getConstant(context).currentState = StateMachine.INITIAL.value
            }

        }

    }

    companion object {
        const val TAG = "LocationStateChange"
    }
}
