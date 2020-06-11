package com.ethernom.helloworld.screens

import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log

import com.ethernom.helloworld.R
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.util.StateMachine
import com.ethernom.helloworld.util.Utils

abstract class BaseActivity : CoreActivity() {

    private var mBluetoothState: ((state: (Boolean)) -> Unit) = {}
    private var mLocationState: ((state: (Boolean)) -> Unit) = {}
    open var isRequestEnableLocation = false
    open  var isScreenPresent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    open fun checkBluetoothSate(bluetoothState: ((state: (Boolean)) -> Unit)) {

        mBluetoothState = bluetoothState
        if (TrackerSharePreference.getConstant(this).isBLEStatus) {
            mBluetoothState(true)
        } else {
            val mIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(mIntent, Utils.REQUEST_BLUETOOTH_STATE)
        }
    }

    open fun checkLocationState(locationState: ((state: (Boolean)) -> Unit)){
        Log.d("MyApplication check", "${TrackerSharePreference.getConstant(this).isLocationStatus}")
        mLocationState = locationState
        if (TrackerSharePreference.getConstant(this).isLocationStatus){
            mLocationState(true)
        }else{
            AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Please enable location for registration the card.")
                .setCancelable(false)
                .setPositiveButton(
                    android.R.string.yes
                ) { dialog, _ ->
                    dialog.dismiss()
                    val mIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(mIntent)
                    isRequestEnableLocation = true

                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        isScreenPresent = true
        if (isRequestEnableLocation){
            if (TrackerSharePreference.getConstant(this).isLocationStatus){
                mLocationState(true)
            }else{
                mLocationState(false)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        isScreenPresent = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Utils.REQUEST_BLUETOOTH_STATE) {  // Match the request code
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Bluetooth turn on")
                mBluetoothState(true)
            } else {   // RESULT_CANCELED
                Log.d(TAG, "Bluetooth deny turning on")
                mBluetoothState(false)
            }
        }
        if (requestCode == Utils.REQUEST_LOCATION_STATE) {  // Match the request code
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "GPS turn on")
                mLocationState(true)
            } else {   // RESULT_CANCELED
                Log.d(TAG, "GPS deny turning on")
                mLocationState(false)
            }
        }
    }
    companion object{
        var TAG = BaseActivity::class.java.simpleName
        fun getEnumNameByValue(value: String): String{
            return "${StateMachine.values().find { it.value == value }}"
        }
    }

}
