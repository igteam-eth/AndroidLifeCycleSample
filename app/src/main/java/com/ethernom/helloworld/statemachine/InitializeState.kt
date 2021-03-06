package com.ethernom.helloworld.statemachine

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.screens.DiscoverDeviceActivity
import com.ethernom.helloworld.screens.MainActivity
import com.ethernom.helloworld.screens.SplashScreenActivity
import com.ethernom.helloworld.util.StateMachine

class InitializeState {

    val TAG = "InitializeState"

    @RequiresApi(Build.VERSION_CODES.O)
    fun goToInitialState(context: Context) {
        val trackerSharePreference = TrackerSharePreference.getConstant(context)

        // if card not yet register
        if (!trackerSharePreference.isCardRegistered){
            // card registered & ble and location are off
            if (!trackerSharePreference.isLocationStatus && !trackerSharePreference.isBLEStatus){
                Log.e("InitializeState", "not card registered location off , ble off")

                /*
                 - Launch BLE & Location Status Intent
                 - Display (Notify user to turn on BLE)
                 - Display (Notify user to turn on Location)
                 - change to 0000
                 */
                //trackerSharePreference.currentState = StateMachine.INITIAL.value
                // Push system notification for notify user
                //showBluetoothNotification(context)
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground Intent to Main Activity for Display Read label to notify user
                    val intent = Intent(context, DiscoverDeviceActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    SplashScreenActivity.TEMP_ONE_SHOT_FLAG = SplashScreenActivity.ONE_SHOT_FLAG
                }
                return
            }
            // if Bluetooth turn off
            else if (!trackerSharePreference.isBLEStatus){
                Log.e("InitializeState", "not card registered ble off")

                /*
                 - Launch BLE & Location Status Intent
                 - Display (Notify user to turn on BLE)
                 - change to 1000
                 */
                trackerSharePreference.currentState = StateMachine.CARD_DISCOVERY_BLE_LOCATION_OFF.value
                // Push system notification for notify user
                //showBluetoothNotification(context)

                if (MyApplication.isAppInForeground(context)){
                    // App in foreground Intent to Main Activity for Display Read label to notify user
                    val intent = Intent(context, DiscoverDeviceActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    SplashScreenActivity.TEMP_ONE_SHOT_FLAG = SplashScreenActivity.ONE_SHOT_FLAG

                }
                return
            }
            // if Location turn off
            else if (!trackerSharePreference.isLocationStatus){
                Log.e("InitializeState", "not card registered location off")
                /*
                 - Launch BLE & Location Status Intent
                 - Display (Notify user to turn on Location)
                 - change to 1000
                 */
                // Push system notification for notify user
                //showLocationNotification(context)
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground Intent to Main Activity for Display Read label to notify user
                    val intent = Intent(context, DiscoverDeviceActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                }
                return
            }
            // if Both Location & Bluetooth is turn on
            else if (trackerSharePreference.isLocationStatus && trackerSharePreference.isBLEStatus){
                Log.e("InitializeState", "not card registered location on , ble on")
                /*
                 - ble and location are true
                 - Display Empty List & state scan generate advertising package
                 - change to 1001
                 */
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground Intent to Main Activity for Display Empty List & state scan generate advertising package
                    val intent = Intent(context, DiscoverDeviceActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    SplashScreenActivity.TEMP_ONE_SHOT_FLAG = SplashScreenActivity.ONE_SHOT_FLAG
                }
            }
        }
        // if card registered
        else {
            // card registered & ble and location are off
            if (!trackerSharePreference.isLocationStatus && !trackerSharePreference.isBLEStatus){
                /*
                - Launch BLE & Location Status Intent
                - Display (Notify user to turn on BLE)
                - Display (Notify user to turn on Location)
                - Display Registered Device"
                - change to 2003
                */
                //showBluetoothNotification(context)
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground Intent to Main Activity for display card registered
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    SplashScreenActivity.TEMP_ONE_SHOT_FLAG = SplashScreenActivity.ONE_SHOT_FLAG
                }

                return
            }
            // if Bluetooth is turn off
            else if (!trackerSharePreference.isBLEStatus){
               /*
                  - Launch BLE & Location Status Intent
                  - Display (Notify user to turn on BLE)
                  - Display Registered Device"
                  - change to 2001
                  */
                //showBluetoothNotification(context)
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground Intent to Main Activity for display card registered
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    SplashScreenActivity.TEMP_ONE_SHOT_FLAG = SplashScreenActivity.ONE_SHOT_FLAG
                }
                return
            }
            // if Location is turn off
            else if (!trackerSharePreference.isLocationStatus){
                /* - Launch BLE & Location Status Intent
                   - Display (Notify user to turn on Location)
                   - Display Registered Device
                   - change to 2002 */
                //showLocationNotification(context)
                if (MyApplication.isAppInForeground(context)){
                    // App in foreground Intent to Main Activity for display card registered
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    SplashScreenActivity.TEMP_ONE_SHOT_FLAG = SplashScreenActivity.ONE_SHOT_FLAG
                }
                return
            }
            // if Both Location & Bluetooth turn on
            else if (trackerSharePreference.isLocationStatus && trackerSharePreference.isBLEStatus){
                /*
                - ble and location are true
                - Display Registered Device
                - change to 2000
                */

                if (MyApplication.isAppInForeground(context)){
                    // App in foreground Intent to Main Activity for display card registered
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    SplashScreenActivity.TEMP_ONE_SHOT_FLAG = SplashScreenActivity.ONE_SHOT_FLAG
                }
            }
        }

    }
}
