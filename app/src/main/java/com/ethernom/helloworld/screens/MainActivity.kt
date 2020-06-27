package com.ethernom.helloworld.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ethernom.helloworld.R
import com.ethernom.helloworld.adapter.RegisteredDeviceAdapter
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.application.TrackerSharePreference.getConstant
import com.ethernom.helloworld.dialog.DeleteDeviceBottomDialog
import com.ethernom.helloworld.dialog.ItemDeleteCallback
import com.ethernom.helloworld.model.BleClient
import com.ethernom.helloworld.receiver.AlarmReceiver
import com.ethernom.helloworld.receiver.BeaconReceiver
import com.ethernom.helloworld.servcie.AppSwipeEventService
import com.ethernom.helloworld.statemachine.BeaconRegistration
import com.ethernom.helloworld.util.Utils
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_main.*


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : BaseActivity(), RegisteredDeviceAdapter.OnItemCallback, ItemDeleteCallback {

    private var registeredDeviceList: ArrayList<BleClient> = ArrayList()
    private var registeredDeviceAdapter: RegisteredDeviceAdapter? = null
    private lateinit var trackerSharePreference: TrackerSharePreference
    private lateinit var appSwipeIntentService: Intent

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start Initialize Card Registered RecyclerView Adapter
        initialRecyclerViewList()

        // Initialize share preference
        trackerSharePreference = getConstant(this)
        appSwipeIntentService = Intent(this, AppSwipeEventService::class.java)

        // Stop ring when app is ringing by user interact with notification
        if (!getConstant(this).isAlreadyCreateWorkerThread) {
            getConstant(this).isRanging = false
            BeaconReceiver.stopSound()
        }
        button_setting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
            Utils.preventDoubleClick(it)
        }

        displayCardAndLaunchWorkerThread()
    }

    private fun displayCardAndLaunchWorkerThread() {
        if (getConstant(this).isCardRegistered) {
            // Call display registered device to the list
            displayRegisteredCard()
        }
        if (
        // check bluetooth is turn on
            Utils.isBluetoothEnable()
            &&
            // check location is turn on
            Utils.isLocationEnabled(this)
        ) {
            /*
           if both location & bluetooth are turn on : Launch BLE Scan Intent for detect Beacon signal
           For WaitingForBeaconState we study with input event , state variable and action function for intent to next state
           */
            BeaconRegistration().launchBLEScan(this)

            // Host model is
            MyApplication.saveLogWithCurrentDate("BRAND: ${Build.BRAND}")

            // Host model is SAMSUNG  start alarm manager
            if (Build.BRAND.equals("samsung", ignoreCase = true)) {
                // check if not Already Create Alarm
                if (!trackerSharePreference.isAlreadyCreateAlarm) {
                    MyApplication.saveLogWithCurrentDate("Periodic Alarm for Samsung created")
                    trackerSharePreference.isAlreadyCreateAlarm = true
                    val startIntent = Intent(this.applicationContext, AlarmReceiver::class.java)
                    this.applicationContext.sendBroadcast(startIntent)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        /*if (getConstant(this).isCardRegistered) {
            // Call display registered device to the list
            displayRegisteredCard()
        }
        if (
        // check if storage permission turn on
            requestWriteExternalStoragePermission()

        ) {
            if (
            // check bluetooth is turn on
                Utils.isBluetoothEnable()
                &&
                // check location is turn on
                Utils.isLocationEnabled(this)
            ) {
                *//*
               if both location & bluetooth are turn on : Launch BLE Scan Intent for detect Beacon signal
               For WaitingForBeaconState we study with input event , state variable and action function for intent to next state
               *//*
                BeaconRegistration().launchBLEScan(this)

                // Host model is
                MyApplication.saveLogWithCurrentDate("BRAND: ${Build.BRAND}")

                // Host model is SAMSUNG  start alarm manager
                if (Build.BRAND.equals("samsung", ignoreCase = true)) {
                    // check if not Already Create Alarm
                    if (!trackerSharePreference.isAlreadyCreateAlarm) {
                        MyApplication.saveLogWithCurrentDate("Periodic Alarm for Samsung created")
                        trackerSharePreference.isAlreadyCreateAlarm = true
                        val startIntent = Intent(this.applicationContext, AlarmReceiver::class.java)
                        this.applicationContext.sendBroadcast(startIntent)
                    }
                }
            }
        }*/
    }

    // Initialize RecyclerView for display list of card registered
    private fun initialRecyclerViewList() {
        registeredDeviceAdapter = RegisteredDeviceAdapter(registeredDeviceList, this)
        rv_registered_device.layoutManager =
            LinearLayoutManager(this)
        rv_registered_device.adapter = registeredDeviceAdapter
    }

    private fun requestWriteExternalStoragePermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0
            )
            false
        } else {
            true
        }
    }

    // Display Registered Device to Recycler View List
    private fun displayRegisteredCard() {
        registeredDeviceList.clear()
        val bleClient: BleClient = getConstant(this).ethernomCard
        registeredDeviceList.add(bleClient)
        registeredDeviceAdapter!!.notifyDataSetChanged()
    }

    // Callback event when user click device item to delete
    override fun ItemClickListener(position: Int) {
        Log.d(TAG, "Name: " + registeredDeviceList[position].devName)
        DeleteDeviceBottomDialog(registeredDeviceList[position].devName, this, this).show()
    }

    // Callback event when user click delete item & auto intent to discover devices screen
    override fun onItemDeleteClicked() {
        MyApplication.saveLogWithCurrentDate("Card have been deleted.")
        registeredDeviceList.clear()
        registeredDeviceAdapter!!.notifyDataSetChanged()
        trackerSharePreference.clearAll()
        Utils.initBLE_Location(this)
        val intent = Intent(this, SplashScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }

    override fun onDestroy() {
        super.onDestroy()

        /*val mIntent = Intent(this, StartScanAlarmReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(
//            this, 0,
//            mIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )

        if (getConstant(this).isRanging) {
            BeaconReceiver.stopSound()
            getConstant(this).isRanging = false
            Utils.removeNotificationByID(this, Utils.CHANNEL_RANG)
        }
        applicationContext.sendBroadcast(mIntent)*/

    }

    override fun onStart() {
        super.onStart()
        startService(appSwipeIntentService)
    }

    companion object {
        const val PERMISSION_REQUEST_COARSE_LOCATION = 112
        const val TAG = "APP_MainActivity"
    }
}
