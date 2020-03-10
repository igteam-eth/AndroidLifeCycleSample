package com.ethernom.helloworld.screens

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ethernom.helloworld.R
import com.ethernom.helloworld.adapter.DeviceAdapter
import com.ethernom.helloworld.model.BleClient
import com.ethernom.helloworld.service.BLEScan
import kotlinx.android.synthetic.main.toolbar_default_backpress.*
import java.util.*

class DiscoverDeviceActivity : AppCompatActivity(), DeviceAdapter.OnItemCallback,
    BLEScan.DeviceDiscoveredCallBack {

    private var mBTDevicesArrayList: ArrayList<BleClient>? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: DeviceAdapter? = null
    private var mBleScan: BLEScan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_device)

        checkLocationPermission()
        mBleScan = BLEScan(this, this)
        mBTDevicesArrayList = ArrayList()
        setUpList()
        buttonBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun ItemClickListener(position: Int) {
        Log.d("onItemClick", "" + position)
        startActivity(Intent(this, ConfirmPinActivity::class.java))
    }

    override fun DeviceDiscover(
        deviceName: String,
        uuid: String,
        macadd: String,
        rssi: String,
        SNDevice: String
    ) {
        Log.d("SerialNumber", SNDevice)
        var stat = false
        for (ble in mBTDevicesArrayList!!) {
            if (macadd == ble.macAddress) {
                stat = true
                break
            }
        }
        if (!stat) {
            val ble = BleClient(
                UUID.randomUUID().toString(),
                uuid,
                macadd,
                deviceName,
                rssi,
                "active",
                SNDevice
            )
            mBTDevicesArrayList!!.add(ble)
            adapter!!.notifyDataSetChanged()
        }
    }

    private fun setUpList() {
        recyclerView = findViewById(R.id.recyclerView)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView!!.layoutManager = linearLayoutManager
        adapter = DeviceAdapter(mBTDevicesArrayList, this)
        recyclerView!!.adapter = adapter
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this).setTitle(R.string.title_location_permission)
                    .setMessage(R.string.text_location_permission).setPositiveButton(
                        R.string.ok
                    ) { _, _ ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }
                    .create().show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_LOCATION
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mBleScan!!.startScanning()
    }

    override fun onPause() {
        super.onPause()
        mBleScan!!.stopScanning()
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}
