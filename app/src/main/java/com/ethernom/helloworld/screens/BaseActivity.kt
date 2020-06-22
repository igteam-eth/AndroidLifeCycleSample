package com.ethernom.helloworld.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.annotation.LayoutRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ethernom.helloworld.R
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.util.StateMachine
import com.ethernom.helloworld.util.Utils
import kotlinx.android.synthetic.main.activity_base.*

abstract class BaseActivity : CoreActivity() {

    private var mBluetoothState: ((state: (Boolean)) -> Unit) = {}
    private var mLocationPermission: ((status: (Boolean)) -> Unit) = {}
    private var mWriteStoragePermission: ((status: (Boolean)) -> Unit) = {}
    open var isScreenPresent = false
    private var isShowDialogDenyPermanently = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_base)

        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        view_turn_on_location.setOnClickListener {
            if (!Utils.isLocationEnabled(this)) {
                val mIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(mIntent)
            } else {
                openSettingScreen()
            }
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

    private fun setUpToolbar() {

        if (this !is SplashScreenActivity) {
            if (TrackerSharePreference.getConstant(this).isCardRegistered) {
                toolbar_after_registered.visibility = View.VISIBLE
            } else {
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

        if (this !is SplashScreenActivity) {
            displayBLEAndLocationEnable()
            // next state
            if(!TrackerSharePreference.getConstant(this).isCardRegistered){
                if(!Utils.isBluetoothEnable() || !Utils.isLocationEnabled(this)) {
                    //when start up if our location or ble is off when go to 1000 state "CARD_DISCOVERY_BLE_LOCATION_OFF"
                    TrackerSharePreference.getConstant(this).currentState = StateMachine.CARD_DISCOVERY_BLE_LOCATION_OFF.value
                }
            } else {
                if(!Utils.isBluetoothEnable() && !Utils.isLocationEnabled(this)) {
                    //when start up if our location or ble is off when go to 2003 state "WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE"
                    TrackerSharePreference.getConstant(this).currentState = StateMachine.WAITING_FOR_BEACON_BLE_AND_LOCATION_OFF_STATE.value
                } else if (!Utils.isBluetoothEnable()) {
                    //when start up if our location or ble is off when go to 2001 state "WAITING_FOR_BEACON_BLE_OFF_STATE"
                    TrackerSharePreference.getConstant(this).currentState = StateMachine.WAITING_FOR_BEACON_BLE_OFF_STATE.value
                } else if (!Utils.isLocationEnabled(this)){
                    //when start up if our location or ble is off when go to 2002 state "WAITING_FOR_BEACON_LOCATION_OFF_STATE"
                    TrackerSharePreference.getConstant(this).currentState = StateMachine.WAITING_FOR_BEACON_LOCATION_OFF_STATE.value
                }
            }
        }

        if (isShowDialogDenyPermanently){
            mLocationPermission(true)
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
        val isAllowAllTheTime: Boolean = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }
        if (Utils.isLocationEnabled(this) && isAllowAllTheTime) {
            view_turn_on_location.visibility = View.GONE
        } else {
            view_turn_on_location.visibility = View.VISIBLE
            return
        }
    }

    open fun showDefaultToolbar() {
        toolbar_after_registered.visibility = View.GONE
        toolbar_before_registered.visibility = View.VISIBLE
    }

    open fun hideAllToolbar() {
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

    private val mBluetoothStateChange = object : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                displayBLEAndLocationEnable()
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {

                    BluetoothAdapter.STATE_ON -> {
                        if (Utils.isLocationEnabled(context)) {
                            readyToDiscoverDevice()
                        }
                    }
                }
            }
        }
    }

    private val mLocationStateChange = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action

            if (action.equals(LocationManager.MODE_CHANGED_ACTION)) {
                displayBLEAndLocationEnable()
                if (Utils.isLocationEnabled(context) && Utils.isBluetoothEnable()) {//true
                    readyToDiscoverDevice()
                }

            }
        }
    }


    /**
     * Case 1: User doesn't have permission
     * Case 2: User has permission
     *
     * Case 3: User has never seen the permission Dialog
     * Case 4: User has denied permission once but he din't clicked on "Never Show again" check box
     * Case 5: User denied the permission and also clicked on the "Never Show again" check box.
     * Case 6: User has allowed the permission
     *
     */
    open fun checkLocationPermission(locationPermission: ((state: (Boolean)) -> Unit)){
        this.mLocationPermission = locationPermission

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // This is Case 1. Now we need to check further if permission was shown before or not
            Log.d(
                TAG,
                "This is Case 1. Now we need to check further if permission was shown before or not"
            )
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {
                // This is Case 4.
                Log.d(TAG, "This is Case 4.")
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    PERMISSION_REQUEST_COARSE_LOCATION
                )
            } else {
                // This is Case 3. Request for permission here
                Log.d(TAG, "This is Case 3. Request for permission here")
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ),
                    PERMISSION_REQUEST_COARSE_LOCATION
                )
            }
        } else {
            // This is Case 2. You have permission now you can do anything related to it
            Log.d(TAG, "This is Case 2. You have permission now you can do anything related to it")
            mLocationPermission(true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // This is Case 2 (Permission is now granted)
                Log.d(TAG, "This is Case 2 (Permission is now granted)")
                mLocationPermission(true)
            } else {
                // This is Case 1 again as Permission is not granted by user
                Log.d(TAG, "This is Case 1 again as Permission is not granted by user")


                //Now further we check if used denied permanently or not
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                ) {
                    // case 4 User has denied permission but not permanently
                    Log.d(TAG, "case 4 User has denied permission but not permanently")
                    mLocationPermission(false)
                } else {
                    // case 5. Permission denied permanently.
                    Log.d(TAG, "case 5. Permission denied permanently")
                    // You can open Permission setting's page from here now.
                    showDialogDenyPermanently()
                }
            }
        }else if(requestCode == PERMISSION_REQUEST_WRITE_STORAGE){
            mWriteStoragePermission(true)
        }
    }

    private fun showDialogDenyPermanently() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permission deny")
            .setMessage("Please go to setting and allow permission \"Allow all the time\" ")
            .setCancelable(false)
            .setPositiveButton("Setting") { dialog, _ ->
                openSettingScreen()
                isShowDialogDenyPermanently = true
                dialog.dismiss()
            }
            .show()
    }


    private fun openSettingScreen() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    open fun readyToDiscoverDevice() {}

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
    open fun checkWriteExternalStoragePermission( writeStoragePermission: ((state: (Boolean)) -> Unit)) {
        mWriteStoragePermission = writeStoragePermission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSION_REQUEST_WRITE_STORAGE
            )
        } else {
            mWriteStoragePermission(true)
        }
    }
    fun showBackButtonToolbar(){
        text_back_press.visibility = View.VISIBLE
        text_back_press.setOnClickListener {
            onBackPressed()
        }
    }
    fun showSettingButtonToolbar(){
        button_setting_from_discover.visibility = View.VISIBLE
        button_setting_from_discover.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }


    companion object {
        var TAG = BaseActivity::class.java.simpleName
        const val PERMISSION_REQUEST_COARSE_LOCATION = 112
        const val PERMISSION_REQUEST_WRITE_STORAGE = 113
        fun getEnumNameByValue(value: String): String {
            return "${StateMachine.values().find { it.value == value }}"
        }
    }


}
