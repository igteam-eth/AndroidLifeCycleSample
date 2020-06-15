package com.ethernom.helloworld.screens

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes

import com.ethernom.helloworld.R
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.util.StateMachine
import com.ethernom.helloworld.util.Utils
import kotlinx.android.synthetic.main.activity_base.*
import android.content.IntentFilter
import android.location.LocationManager
import android.provider.Settings




abstract class BaseActivity : CoreActivity() {

    private var mBluetoothState: ((state: (Boolean)) -> Unit) = {}
    open  var isScreenPresent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base)

        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        view_turn_on_location.setOnClickListener {
            val mIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(mIntent)
        }
        view_turn_on_bluetooth.setOnClickListener {
            val intentOpenBluetoothSettings = Intent()
            intentOpenBluetoothSettings.action = Settings.ACTION_BLUETOOTH_SETTINGS
            startActivity(intentOpenBluetoothSettings)
        }
        button_close.setOnClickListener {
            view_turn_on_location.visibility = View.GONE
        }
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        val view = layoutInflater.inflate(layoutResID, null, false)
        content_view!!.addView(view)
        //set up toolbar
        setUpToolbar()
    }

    private fun setUpToolbar(){

        if (this !is SplashScreenActivity){
            if (TrackerSharePreference.getConstant(this).isCardRegistered){
                toolbar_after_registered.visibility = View.VISIBLE
            }else{
                toolbar_before_registered.visibility = View.VISIBLE
            }
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

    override fun onResume() {
        super.onResume()
        isScreenPresent = true

        if (this !is SplashScreenActivity){
            displayBLEAndLocationEnable()
        }
    }

    private fun displayBLEAndLocationEnable() {
        if (Utils.isBluetoothEnable()) {
            view_turn_on_bluetooth.visibility = View.GONE
        } else {
            view_turn_on_bluetooth.visibility = View.VISIBLE
            view_turn_on_location.visibility = View.GONE
            return
        }
        if (Utils.isLocationEnabled(this)) {
            view_turn_on_location.visibility = View.GONE
        } else {
            view_turn_on_location.visibility = View.VISIBLE
            return
        }
    }

    open fun showDefaultToolbar(){
        toolbar_after_registered.visibility = View.GONE
        toolbar_before_registered.visibility = View.VISIBLE
    }

    open fun hideAllToolbar(){
        toolbar_after_registered.visibility = View.GONE
        toolbar_before_registered.visibility = View.GONE
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
    }
    private val  mBluetoothStateChange =  object: BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val  action = intent!!.action

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                displayBLEAndLocationEnable()
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {

                    BluetoothAdapter.STATE_ON -> {
                        if(Utils.isLocationEnabled(context)){
                            readyToDiscoverDevice()
                        }
                    }
                }

            }
        }
    }

    private val  mLocationStateChange =  object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val  action = intent!!.action

            if (action.equals(LocationManager.MODE_CHANGED_ACTION)) {
                displayBLEAndLocationEnable()
                if (Utils.isLocationEnabled(context) && Utils.isBluetoothEnable()) {//true
                    readyToDiscoverDevice()
                }

            }
        }
    }
    open fun readyToDiscoverDevice(){

    }

    override fun onStart() {
        super.onStart()
        val filter1 = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(mBluetoothStateChange, filter1)

        val filter2 = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        registerReceiver(mLocationStateChange, filter2)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(mBluetoothStateChange)
        unregisterReceiver(mLocationStateChange)
    }


    companion object{
        var TAG = BaseActivity::class.java.simpleName
        fun getEnumNameByValue(value: String): String{
            return "${StateMachine.values().find { it.value == value }}"
        }
    }



}
