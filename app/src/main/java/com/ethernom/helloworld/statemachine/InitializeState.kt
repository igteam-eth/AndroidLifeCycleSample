package com.ethernom.helloworld.statemachine

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import androidx.annotation.RequiresApi
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.MyApplication.showSilentNotificationBLE
import com.ethernom.helloworld.application.MyApplication.showSilentNotificationLocation
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.screens.DiscoverDeviceActivity
import com.ethernom.helloworld.screens.MainActivity
import com.ethernom.helloworld.util.StateMachine

class InitializeState {

    @RequiresApi(Build.VERSION_CODES.O)
    fun goToInitialState(context: Context) {
        val trackerSharePreference = TrackerSharePreference.getConstant(context)
        if (!trackerSharePreference.isCardRegistered){
            // card registered & ble and location are off
            if (!trackerSharePreference.isLocationStatus && !trackerSharePreference.isBLEStatus){
                //- Launch BLE & Location Status Intent
                //- Display (Notify user to turn on BLE)
                //- Display (Notify user to turn on Location)
                // change to 0000
                trackerSharePreference.currentState = StateMachine.INITIAL.value
                showSilentNotificationBLE(context)
                if (MyApplication.isAppInForeground(context)){
                    val intent = Intent(context, DiscoverDeviceActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)

                    // App in foreground
                }else{
                    // App in background
                }
                return
            }else if (!trackerSharePreference.isBLEStatus){
                //- Launch BLE & Location Status Intent
                //- Display (Notify user to turn on BLE)
                // change to 1000
                trackerSharePreference.currentState = StateMachine.CARD_DISCOVERY_BLE_LOCATION_OFF.value
                showSilentNotificationBLE(context)

                if (MyApplication.isAppInForeground(context)){

                    val intent = Intent(context, DiscoverDeviceActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)

                    // App in foreground
                }else{
                    // App in background
                }
                return
            }else if (!trackerSharePreference.isLocationStatus){
                //- Launch BLE & Location Status Intent
                //- Display (Notify user to turn on Location)
                // change to 1000
                trackerSharePreference.currentState = StateMachine.CARD_DISCOVERY_BLE_LOCATION_OFF.value
                showSilentNotificationLocation(context)
                if (MyApplication.isAppInForeground(context)){

                    val intent = Intent(context, DiscoverDeviceActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    // App in foreground
                }else{
                    // App in background
                }
                return
            }else if (trackerSharePreference.isLocationStatus && trackerSharePreference.isBLEStatus){
                //ble and location are true
                // change to 1001
                trackerSharePreference.currentState = StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.value
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground
                    val intent = Intent(context, DiscoverDeviceActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }else{
                    // App in background
                }
            }
        } else {
            // card registered & ble and location are off
            if (!trackerSharePreference.isLocationStatus && !trackerSharePreference.isBLEStatus){
                //- Launch BLE & Location Status Intent
                //- Display (Notify user to turn on BLE)
                //- Display (Notify user to turn on Location)
                //- Display Registered Device"
                // change to 2003
                trackerSharePreference.currentState = StateMachine.WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE.value
                showSilentNotificationBLE(context)
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }else{
                    // App in background
                }
                return
            }else if (!trackerSharePreference.isBLEStatus){
                //- Launch BLE & Location Status Intent
                //- Display (Notify user to turn on BLE)
                //- Display Registered Device"
                // change to 2001
                trackerSharePreference.currentState = StateMachine.WAITING_FOR_BEACON_BLE_OFF_STATE.value
                showSilentNotificationBLE(context)
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }else{
                    // App in background
                }
                return
            }else if (!trackerSharePreference.isLocationStatus){
                //- Launch BLE & Location Status Intent
                //- Display (Notify user to turn on Location)
                //- Display Registered Device"
                // change to 2002
                trackerSharePreference.currentState = StateMachine.WAITING_FOR_BEACON_LOCATION_OFF_STATE.value
                showSilentNotificationLocation(context)
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }else{
                    // App in background
                }
                return
            }else if (trackerSharePreference.isLocationStatus && trackerSharePreference.isBLEStatus){
                //ble and location are true
                // change to 2000

                trackerSharePreference.currentState = StateMachine.WAITING_FOR_BEACON.value
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }else{
                    // App in background
                }
            }
        }

    }
}
