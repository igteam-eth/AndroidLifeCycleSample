package com.ethernom.helloworld

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

import androidx.annotation.RequiresApi
class BluetoothStateChangeReceiver : BroadcastReceiver() {

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val TAG = javaClass.simpleName

        if (action != null && action == BluetoothAdapter.ACTION_STATE_CHANGED) {

            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_OFF -> {
                  Log.e("EthernomHelloworld", "BT STATE_OFF")
                    TrackerSharePreference.getConstant(context).isBLEStatus = false
                }

                BluetoothAdapter.STATE_TURNING_OFF -> {
                    Log.e("EthernomHelloworld", "BT STATE_TURNING_OFF")
                }

                BluetoothAdapter.STATE_ON -> {
                    Log.e("EthernomHelloworld", "BT STATE_ON")
                    TrackerSharePreference.getConstant(context).isBLEStatus = true
                }

                BluetoothAdapter.STATE_TURNING_ON -> {
                    Log.e("EthernomHelloworld", "BT STATE_TURNING_ON")

                }
            }
        }
    }

}
