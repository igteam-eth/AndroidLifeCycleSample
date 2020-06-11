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
import com.ethernom.helloworld.dialog.DeleteDeviceBottomDialog
import com.ethernom.helloworld.dialog.ItemDeleteCallback
import com.ethernom.helloworld.model.BleClient
import com.ethernom.helloworld.util.Utils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), RegisteredDeviceAdapter.OnItemCallback, ItemDeleteCallback {

    private var registeredDeviceList: ArrayList<BleClient> = ArrayList()
    private var registeredDeviceAdapter: RegisteredDeviceAdapter? = null
    private lateinit var trackerSharePreference: TrackerSharePreference

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Start Initialize Card Registered RecyclerView Adapter
        initialRecyclerViewList()

        trackerSharePreference = TrackerSharePreference.getConstant(this)
        MyApplication.saveCurrentStateToLog(this)

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        if (requestWriteExternalStorage() && requestBluetoothPermission()){
            if (TrackerSharePreference.getConstant(this).isCardRegistered){
                displayRegisteredCard()
            }
        }
    }

    private fun initialRecyclerViewList() {
        registeredDeviceAdapter = RegisteredDeviceAdapter(registeredDeviceList, this)
        rv_registered_device.layoutManager =
            LinearLayoutManager(this)
        rv_registered_device.adapter = registeredDeviceAdapter
    }


    private fun requestWriteExternalStorage(): Boolean {
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun requestBluetoothPermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            Log.d(TAG, "Checking Bluetooth permissions")
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {

                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        PERMISSION_REQUEST_COARSE_LOCATION
                    )

                } else {

                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        PERMISSION_REQUEST_COARSE_LOCATION
                    )
                }
                return false
            } else {
                Log.d(TAG, "  Permission is granted")
                return true
            }
        } else return false
    }

    private fun displayRegisteredCard() {
        registeredDeviceList.clear()
        val bleClient: BleClient = TrackerSharePreference.getConstant(this).ethernomCard
        registeredDeviceList.add(bleClient)
        registeredDeviceAdapter!!.notifyDataSetChanged()
    }

    override fun ItemClickListener(position: Int) {
        Log.d(TAG, "Name: " + registeredDeviceList[position].devName)
        DeleteDeviceBottomDialog(registeredDeviceList[position].devName, this, this).show()
    }

    override fun onItemDeleteClicked() {
        trackerSharePreference.isCardRegistered = false
        registeredDeviceList.clear()
        registeredDeviceAdapter!!.notifyDataSetChanged()
        trackerSharePreference.clearAll()
        Utils.initBLE_Location(this)
        finish()
        startActivity(Intent(this, DiscoverDeviceActivity::class.java))
    }

    companion object {
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
        const val TAG = "APP_MainActivity"
    }

}
