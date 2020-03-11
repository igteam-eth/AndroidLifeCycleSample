package com.ethernom.helloworld.screens

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
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
import com.ethernom.helloworld.receiver.BleReceiver
import com.ethernom.helloworld.screens.MainActivity.Companion.PERMISSION_REQUEST_COARSE_LOCATION
import com.ethernom.helloworld.service.HTSService
import kotlin.system.exitProcess


class TrackerActivity : AppCompatActivity(), RegisteredDeviceAdapter.OnItemCallback, ItemDeleteCallback {

    private var registeredDeviceList: ArrayList<BleClient> = ArrayList()
    private var registeredDeviceAdapter: RegisteredDeviceAdapter? = null
    private lateinit var trackerSharePreference: TrackerSharePreference
    private lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            init()
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
    private fun init(){
        BleReceiver.stop()
        serviceIntent = Intent(applicationContext, HTSService::class.java)
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
        BleReceiver.stop()
        BleReceiver.stopScanning()
        //startService()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (trackerSharePreference.ethernomCard != null && registeredDeviceList.isEmpty()) {
                startService()
                val bleClient : BleClient=  trackerSharePreference.ethernomCard
                registeredDeviceList.add(bleClient)
                registeredDeviceAdapter!!.notifyDataSetChanged()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startService() {
        val service = Intent(this, HTSService::class.java)
        // We pass 0 as a flag so the service will not be created if not exists.
        Log.d(TrackerActivity::class.java.simpleName, "Binding service")
        bindService(service, mServiceConnection, 0)
        startHTSService("Activity created")
        BleReceiver.startScanning(this)
    }

    private fun startHTSService(reason: String) {
        serviceIntent.putExtra(HTSService.STARTUP_SOURCE, reason)
        applicationContext.startService(serviceIntent)
        bindService(serviceIntent, mServiceConnection, 0)
    }

    /*********************************************************************************************
     *
     * Service Connection: allows bi-directional communication with the service.
     *
     */

    private val mServiceConnection = object : ServiceConnection {
        // Interface for monitoring the state of an application service

        override// We get here when the StartService service has connected to this activity.
        fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val b = binder as HTSService.StartServiceBinder
            val mService = b.service

            // Now
            val reason = mService.startupReason
            val msg = "Activity connected to the service. Reason: '$reason'"
            Log.d(MainActivity.TAG, msg)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            // Note: this method is called only when the service is killed by the system,
            // not when it stops itself or is stopped by the activity.
            // It will be called only when there is critically low memory, in practice never
            // when the activity is in foreground.
            val msg = "Activity disconnected from the service"
            Log.d(MainActivity.TAG, msg)
        }
    }
    /**
     * Called when screen is rotated!
     */
    override fun onDestroy() {
        Log.d(MainActivity.TAG, "onDestroy()")
        //unbindService(mServiceConnection)
        super.onDestroy()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun requestBluetoothPermission() {
        val TAG = "Tracker"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            Log.d(TAG, "Checking Bluetooth permissions")
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "  Permission is not granted")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Permission Required for BLE Device Detection")
                builder.setMessage("Bluetooth operation requires 'location' access.\nPlease grant this so the app can detect BLE devices")
                //builder.setIcon(R.drawable.cross);
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    // User replies then there is a call to onRequestPermissionsResult() below
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        PERMISSION_REQUEST_COARSE_LOCATION
                    )
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
        }

    }


}
