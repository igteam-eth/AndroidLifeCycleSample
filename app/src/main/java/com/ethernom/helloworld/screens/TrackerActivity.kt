package com.ethernom.helloworld.screens

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.ethernom.helloworld.R
import com.ethernom.helloworld.adapter.RegisteredDeviceAdapter
import com.ethernom.helloworld.model.BleClient
import kotlinx.android.synthetic.main.activity_tracker.*
import kotlinx.android.synthetic.main.toolbar_default.*
import android.view.animation.AlphaAnimation
import androidx.annotation.RequiresApi
import com.ethernom.helloworld.application.TrackerSharePreference
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ethernom.helloworld.BLEWorkerManager
import com.ethernom.helloworld.receiver.BleReceiver
import com.ethernom.helloworld.screens.MainActivity.Companion.PERMISSION_REQUEST_COARSE_LOCATION
import kotlin.system.exitProcess


class TrackerActivity : AppCompatActivity(), RegisteredDeviceAdapter.OnItemCallback, ItemDeleteCallback {

    private var registeredDeviceList: ArrayList<BleClient> = ArrayList()
    private var registeredDeviceAdapter: RegisteredDeviceAdapter? = null
    private lateinit var trackerSharePreference: TrackerSharePreference
    private val TAG = "Tracker"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            onCreate()
        }else{
            AlertDialog.Builder(this)
                .setTitle("Information")
                .setMessage("This app is supported from version 8 or later")
                .setPositiveButton(android.R.string.yes
                ) { dialog, _ ->
                    dialog.dismiss()
                    exitProcess(1)
                }
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onCreate(){

        trackerSharePreference = TrackerSharePreference.getConstant(this)
        registeredDeviceAdapter =  RegisteredDeviceAdapter(registeredDeviceList, this)

        button_add.setOnClickListener {
            val animation = AlphaAnimation(1f, 0.8f)
            it.startAnimation(animation)
            if (registeredDeviceList.isEmpty()) {
                startActivity(Intent(this, DiscoverDeviceActivity::class.java))
            }else{
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Please delete your current device before registering a new one.")
                    .setPositiveButton(android.R.string.yes
                    ) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }

        button_setting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
        rv_registered_device.layoutManager = LinearLayoutManager(this)
        rv_registered_device.adapter =registeredDeviceAdapter
    }
    override fun ItemClickListener(position: Int) {
        DeleteDeviceBottomDialog(this, this ).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemDeleteClicked() {
        registeredDeviceList.clear()
        registeredDeviceAdapter!!.notifyDataSetChanged()
        trackerSharePreference.ethernomCard = null
        stopWorkerManager()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (trackerSharePreference.ethernomCard != null && registeredDeviceList.isEmpty()) {
                val bleClient : BleClient=  trackerSharePreference.ethernomCard
                registeredDeviceList.add(bleClient)
                registeredDeviceAdapter!!.notifyDataSetChanged()
                startWorkManager()
            }else if (registeredDeviceList.isNotEmpty()){
                startWorkManager()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startWorkManager() {
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(BLEWorkerManager::class.java)
            .addTag("BLE_WORK_MANAGER")
            .build()
        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest)
        BleReceiver.startScanning(this)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun stopWorkerManager() {
        WorkManager.getInstance(this).cancelAllWorkByTag("BLE_WORK_MANAGER")
        BleReceiver.stopSound()
        BleReceiver.stopScanning()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun requestBluetoothPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            Log.d(TAG, "Checking Bluetooth permissions")
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED ) {
                Log.d(TAG, "  Permission is not granted")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Permission Required for BLE Device Detection")
                builder.setMessage("Bluetooth operation requires 'location' access.\nPlease grant this so the app can detect BLE devices")
                //builder.setIcon(R.drawable.cross);
                builder.setPositiveButton(android.R.string.ok, null)

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) !== PackageManager.PERMISSION_GRANTED){
                        builder.setOnDismissListener {
                            // User replies then there is a call to onRequestPermissionsResult() below
                            requestPermissions(
                                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                PERMISSION_REQUEST_COARSE_LOCATION
                            )
                        }
                    }
                }else{
                    builder.setOnDismissListener {
                        // User replies then there is a call to onRequestPermissionsResult() below
                        requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                            PERMISSION_REQUEST_COARSE_LOCATION
                        )
                    }
                }

                builder.show()
            } else {
                Log.d(TAG, "  Permission is granted")
            }
        }
    }

    /* This is called when the user responds to the request permission dialog */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        val TAG = "Tracker"
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Coarse location permission granted")
                } else {
                    Log.d(TAG, "Coarse location permission refused.")
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("This App will not Work as Intended")
                    builder.setMessage("Android requires you to grant access to device\'s location in order to scan for Bluetooth devices.")
                    //builder.setIcon(R.drawable.cross);
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requestBluetoothPermission()
            requestWriteExternalStorage()
        }
    }

    private fun requestWriteExternalStorage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0
            )
        }
    }
}
