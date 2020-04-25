package com.ethernom.helloworld.screens

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ethernom.helloworld.R
import com.ethernom.helloworld.adapter.BLEAdapter
import com.ethernom.helloworld.adapter.DeviceAdapter
import com.ethernom.helloworld.application.SettingSharePreference
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.model.BleClient
import com.ethernom.helloworld.model.CardInfo
import com.ethernom.helloworld.util.BLEScan
import kotlinx.android.synthetic.main.activity_discover_device.*
import kotlinx.android.synthetic.main.toolbar_default_backpress.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class DiscoverDeviceActivity : AppCompatActivity(), DeviceAdapter.OnItemCallback,
    BLEScan.DeviceDiscoveredCallBack, BLEAdapter.BLEAdapterCallback{

    private var mBTDevicesArrayList: ArrayList<BleClient>? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: DeviceAdapter? = null
    private var mBleScan: BLEScan? = null
    private var selectedPos: Int? = null
    private var mBLEAdapter: BLEAdapter? = null


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
        showProgressBar()
        Log.d("onItemClick", ""+position)
        selectedPos = position

        mBLEAdapter = BLEAdapter(this, this)
        mBleScan!!.stopScanning()
        val ci = CardInfo(
            mBTDevicesArrayList!![position].devName,
            mBTDevicesArrayList!![position].macAddress,
            ""
        )
        mBLEAdapter!!.ConnectCard(ci)
    }

    override fun DeviceDiscover(
        deviceName: String,
        uuid: String,
        macadd: String,
        rssi: String,
        SNDevice: String) {
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

    override fun onGetPinSucceeded(pin: String) {
        Log.e("pinCallback", pin)
        val intent = Intent(this, ConfirmPinActivity::class.java)
        intent.putExtra("pin", pin.substring(pin.length - SettingSharePreference.getConstant(this).pinLength, pin.length ))
        startActivityForResult(intent, 101)
    }

    override fun onGetMajorMinorSucceeded(data: String) {
        val mid = getMinLength(data)
        val parts = arrayOf(data.substring(0, mid), data.substring(mid))

        val majors = reversString(parts[0])
        val major = "${majors[1]}${majors[0]}"

        val minors = reversString(parts[1])
        val minor = "${minors[1]}${minors[0]}"

        Log.e("after reverse", major + minor)

        val bleClient =  BleClient()
        bleClient.devName = mBTDevicesArrayList!![selectedPos!!].devName
        bleClient.deviceSN = mBTDevicesArrayList!![selectedPos!!].deviceSN
        bleClient.major = major
        bleClient.minor = minor
        TrackerSharePreference.getConstant(this).ethernomCard = bleClient
        hideProgressBar()
        finish()
    }

    private fun reversString(data: String) =
        arrayOf(data.substring(0, getMinLength(data)), data.substring(getMinLength(data)))

    private fun getMinLength(data: String) = data.length.div(2)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101){
            if (resultCode == Activity.RESULT_OK){
                val result = data!!.getBooleanExtra("pinVerified", false)
                if (result){
                    mBLEAdapter!!.H2CRequestBLETrackerInit()
                }
            }else{
                hideProgressBar()
                mBLEAdapter!!.RequestAppSuspend(0x01.toByte())
            }
        }
    }

    private fun showProgressBar(){
        view_progressBar.visibility = View.VISIBLE
    }
    private fun hideProgressBar(){
        view_progressBar.visibility = View.GONE
    }
    override fun onResume() {
        super.onResume()
        mBleScan!!.startScanning()
    }

    override fun onPause() {
        super.onPause()
        mBleScan!!.stopScanning()
        hideProgressBar()
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
    }
}
