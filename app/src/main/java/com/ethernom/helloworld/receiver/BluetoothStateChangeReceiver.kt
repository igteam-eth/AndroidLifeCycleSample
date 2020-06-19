package com.ethernom.helloworld.receiver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.util.Log

import androidx.annotation.RequiresApi
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.MyApplication.showBluetoothNotification
import com.ethernom.helloworld.application.MyApplication.showLocationNotification
import com.ethernom.helloworld.application.SettingSharePreference
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.screens.DiscoverDeviceActivity
import com.ethernom.helloworld.statemachine.BeaconRegistration
import com.ethernom.helloworld.statemachine.InitializeState
import com.ethernom.helloworld.util.StateMachine
import com.ethernom.helloworld.util.Utils

class BluetoothStateChangeReceiver : BroadcastReceiver() {

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val TAG = javaClass.simpleName

        if (action != null && action == BluetoothAdapter.ACTION_STATE_CHANGED) {

            val currentState = TrackerSharePreference.getConstant(context).currentState

            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {

                BluetoothAdapter.STATE_OFF -> {
                    Log.e(TAG, "currentState : $currentState")

                    Log.e(TAG, "BT STATE_OFF")

                    MyApplication.appendLog(MyApplication.getCurrentDate() + " : BT STATE_OFF \n")
                    TrackerSharePreference.getConstant(context).isBLEStatus = false
                    showBluetoothNotification(context)

                    when(currentState){
                        StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.value ->{
                            goToInitState(context)
                        }
                        StateMachine.GET_FIRMWARE_INFO.value ->{
                            goToInitState(context)
                        }
                        StateMachine.CHECKING_UPDATE_FIRMWARE.value ->{
                            goToInitState(context)
                        }
                        StateMachine.GET_PRIVATE_KEY.value ->{
                            goToInitState(context)
                        }
                        StateMachine.CARD_REGISTER.value ->{
                            goToInitState(context)
                        }
                        StateMachine.VERIFY_PIN.value ->{
                            goToInitState(context)
                        }
                        StateMachine.WAITING_FOR_BEACON.value ->{
                            //Stop Scan
                            BeaconReceiver.stopScan()
                            TrackerSharePreference.getConstant(context).isAlreadyCreateWorkerThread = false
                            TrackerSharePreference.getConstant(context).isAlreadyCreateAlarm = false
                            goToInitState(context)
                        }
                        StateMachine.WAITING_FOR_BEACON_LOCATION_OFF_STATE.value ->{
                            //go to start 2003 WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE
                            TrackerSharePreference.getConstant(context).currentState =
                                StateMachine.WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE.value
                        }
                    }
                }

                BluetoothAdapter.STATE_ON -> {
                    Log.e(TAG, "BT STATE_ON")
                    MyApplication.appendLog(MyApplication.getCurrentDate() + " : BT STATE_ON \n")
                    TrackerSharePreference.getConstant(context).isBLEStatus = true

                    Utils.removeNotificationByID(context, Utils.CHANNEL_BLE_OFF)
                    if (!SettingSharePreference.getConstant(context).isBeforeActivate){
                        return
                    }

                    when(currentState){
                        StateMachine.CARD_DISCOVERY_BLE_LOCATION_OFF.value ->{

                            TrackerSharePreference.getConstant(context).currentState = StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.value
                            if (TrackerSharePreference.getConstant(context).isLocationStatus && MyApplication.isAppInForeground(context)){
                                //StartScan General Advertising
                                //Display List(Empty)
                                val mIntent =  Intent(context, DiscoverDeviceActivity::class.java)
                                mIntent.flags = FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(mIntent)
                            }
                        }

                        StateMachine.WAITING_FOR_BEACON_BLE_OFF_STATE.value ->{
                            if (TrackerSharePreference.getConstant(context).isLocationStatus){
                                //Launch BLE Scan Intent
                                //TrackerSharePreference.getConstant(context).currentState = StateMachine.WAITING_FOR_BEACON.value
                                BeaconRegistration()
                                    .launchBLEScan(context)
                            }
                        }

                        StateMachine.WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE.value ->{
                            if (!TrackerSharePreference.getConstant(context).isLocationStatus){
                                //Notify User to turn on Location
                                TrackerSharePreference.getConstant(context).currentState = StateMachine.WAITING_FOR_BEACON_LOCATION_OFF_STATE.value
                                showLocationNotification(context)
                            }
                        }
                    }
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

}
