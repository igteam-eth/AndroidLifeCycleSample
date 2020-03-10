package com.ethernom.helloworld.screens

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
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
import com.ethernom.helloworld.service.HTSService


class TrackerActivity : AppCompatActivity(), RegisteredDeviceAdapter.OnItemCallback, ItemDeleteCallback {

    private var registeredDeviceList: ArrayList<BleClient> = ArrayList()
    private var registeredDeviceAdapter: RegisteredDeviceAdapter? = null
    private lateinit var trackerSharePreference: TrackerSharePreference
    private lateinit var serviceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)
        init()
    }

    private fun init(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            BleReceiver.stop()
        }
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
    override fun onItemDeleteClicked() {
        registeredDeviceList.clear()
        registeredDeviceAdapter!!.notifyDataSetChanged()
        trackerSharePreference.ethernomCard = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            BleReceiver.stop()
            val service = Intent(this, HTSService::class.java)
            Log.d(TrackerActivity::class.java.simpleName, "Binding service")
            bindService(service, mServiceConnection, 0)
            startHTSService("Activity created")
            BleReceiver.startScanning(this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        if (trackerSharePreference.ethernomCard != null && registeredDeviceList.isEmpty()) {
            val service = Intent(this, HTSService::class.java)
            // We pass 0 as a flag so the service will not be created if not exists.
            Log.d(TrackerActivity::class.java.simpleName, "Binding service")
            bindService(service, mServiceConnection, 0)
            startHTSService("Activity created")
            BleReceiver.startScanning(this)
            val bleClient : BleClient=  trackerSharePreference.ethernomCard
            registeredDeviceList.add(bleClient)
            registeredDeviceAdapter!!.notifyDataSetChanged()
        }
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


}
