package com.ethernom.helloworld.receiver

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import androidx.annotation.RequiresApi
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.screens.MainActivity.Companion.DELAY_PERIOD
import com.ethernom.helloworld.workmanager.MyWorkManager
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

class BluetoothStateChangeReceiver : BroadcastReceiver() {

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action != null && action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    Log.d("BluetoothStateChanger", "STATE_OFF")
                    MyApplication.appendLog(MyApplication.getCurrentDate() + " : BluetoothToggle STATE_OFF\n")

                    if (TrackerSharePreference.getConstant(context).isCardExisted){
                        if (!TrackerSharePreference.getConstant(context).isAlreadyScan){
                            WorkManager.getInstance(context).cancelAllWorkByTag("WORK_MANAGER")
                            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Canceled WORK_MANAGER when BluetoothToggle STATE_OFF\n")
                        } else {
                            BleReceiver.stopScan()
                            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Stop scan when BluetoothToggle STATE_OFF\n")
                        }
                    }
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    Log.d("BluetoothStateChanger", "STATE_TURNING_OFF")
                    MyApplication.appendLog(MyApplication.getCurrentDate() + " : BluetoothToggle STATE_TURNING_OFF\n")
                }
                BluetoothAdapter.STATE_ON -> {
                    Log.d("BluetoothStateChanger", "STATE_ON")
                    MyApplication.appendLog(MyApplication.getCurrentDate() + " : BluetoothToggle STATE_ON\n")

                    if (TrackerSharePreference.getConstant(context).isCardExisted){
                        //OneTimeWorkRequest
                        val oneTimeRequest = OneTimeWorkRequest.Builder(MyWorkManager::class.java)
                            .addTag("WORK_MANAGER")
                            .build()
                        WorkManager.getInstance(context).enqueue(oneTimeRequest)
                        TrackerSharePreference.getConstant(context).isAlreadyCreateWorkerThread = true
                    }
                }
                BluetoothAdapter.STATE_TURNING_ON -> {
                    Log.d("BluetoothStateChanger", "STATE_TURNING_ON")
                    MyApplication.appendLog(MyApplication.getCurrentDate() + " : BluetoothToggle STATE_TURNING_ON\n")
                }
            }
        }
    }

}
