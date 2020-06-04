package com.ethernom.helloworld

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ethernom.helloworld.adapter.DeviceAdapter
import com.ethernom.helloworld.dialog.LoadingDialog
import com.ethernom.helloworld.model.BleClient
import com.ethernom.helloworld.util.BLEScan
import java.util.*

class DiscoverDeviceActivity : AppCompatActivity() , BLEScan.DeviceDiscoveredCallBack, DeviceAdapter.OnItemCallback{

    private var mBluetoothState: ((state: (Boolean)) -> Unit) = {}

    private var mBTDevicesArrayList: ArrayList<BleClient>? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: DeviceAdapter? = null
    private var mBleScan: BLEScan? = null
    private var selectedPos: Int? = null
    private lateinit var loadingDialog: LoadingDialog
    private  val TAG = "DiscoverDeviceActivity"

    private var isVerifyPinType = false

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_device)
        mBTDevicesArrayList = ArrayList()
        setUpList()

        checkBluetoothSate { isBTOn ->
            if (isBTOn){
                Log.d(TAG, "BT On")
                mBleScan = BLEScan(this, this)
                loadingDialog = LoadingDialog(this)
                mBleScan!!.startScanning()
            }else{
                Log.d(TAG, "BT off")
            }
        }
    }

    private fun setUpList() {
        recyclerView = findViewById(R.id.recyclerView)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView!!.layoutManager = linearLayoutManager
        adapter = DeviceAdapter(mBTDevicesArrayList, this)
        recyclerView!!.adapter = adapter
    }

    override fun DeviceDiscover(
        deviceName: String?,
        uuid: String?,
        macadd: String?,
        rssi: String?,
        SNDevice: String?
    ) {
        Log.d("SerialNumber", SNDevice!!)
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

    override fun ItemClickListener(position: Int) {
        Log.e(TAG, "ItemClickListener $position" )

    }

    private fun checkBluetoothSate(bluetoothState: ((state: (Boolean)) -> Unit)) {

        this.mBluetoothState = bluetoothState
        val bAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bAdapter != null) {
            if (!bAdapter.isEnabled) {
                val mIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(mIntent, REQUEST_BLUETOOTH_STATE)
            }else{
                mBluetoothState(true)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_BLUETOOTH_STATE) {  // Match the request code
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Bluetooth turn on")
                mBluetoothState(true)
            } else {   // RESULT_CANCELED
                Log.d(TAG, "Bluetooth deny turning on")
                mBluetoothState(false)
            }
        }
    }
    companion object{
        const val REQUEST_BLUETOOTH_STATE = 101
        const val TAG = "DiscoverDeviceActivity"
    }
}
