package com.ethernom.helloworld.screens

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ethernom.helloworld.R
import com.ethernom.helloworld.adapter.BLEAdapter
import com.ethernom.helloworld.adapter.DeviceAdapter
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.SettingSharePreference
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.dialog.LoadingDialog
import com.ethernom.helloworld.dialog.UpdateCardDialog
import com.ethernom.helloworld.model.BleClient
import com.ethernom.helloworld.model.CardInfo
import com.ethernom.helloworld.util.BLEScan
import kotlinx.android.synthetic.main.activity_discover_device.*
import kotlinx.android.synthetic.main.toolbar_default_backpress.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class DiscoverDeviceActivity : BaseActivity(), DeviceAdapter.OnItemCallback,
    BLEScan.DeviceDiscoveredCallBack, BLEAdapter.BLEAdapterCallback, UpdateCardDialog.Callback {

    private var mBTDevicesArrayList: ArrayList<BleClient>? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: DeviceAdapter? = null
    private var mBleScan: BLEScan? = null
    private var selectedPos: Int? = null
    private var mBLEAdapter: BLEAdapter? = null
    private lateinit var loadingDialog: LoadingDialog

    private var cardInfo: CardInfo? = null
    private var isVerifyPinType = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_device)

        checkLocationPermission()
        mBleScan = BLEScan(this, this)
        loadingDialog = LoadingDialog(this)
        mBLEAdapter = BLEAdapter(this, this, loadingDialog)


        mBTDevicesArrayList = ArrayList()
        setUpList()
        buttonBack.setOnClickListener {
            onBackPressed()
        }

        button_question_mark.setOnClickListener {
            MyApplication.showAlertDialog(
                this,
                "Unable to see your device?",
                "Make sure your device is powered on and authenticated."
            )
        }

    }

    override fun ItemClickListener(position: Int) {
        if (haveNetworkConnection()) {
            showProgressBar()
            Log.d("onItemClick", "" + position)
            selectedPos = position

           if(checkBlueToothAdapter()) {
               mBleScan!!.stopScanning()
           }

            cardInfo = CardInfo(
                mBTDevicesArrayList!![position].devName,
                mBTDevicesArrayList!![position].macAddress,
                ""
            )
            mBLEAdapter!!.ConnectCard(cardInfo)
            loadingDialog.setLoadingDescription("Loading: Connecting " + mBTDevicesArrayList!![position].devName + "...")
        } else {
            showDialog("Please enable internet connection!")
        }
    }

    private fun haveNetworkConnection(): Boolean {
        var haveConnectedWifi = false
        var haveConnectedMobile = false

        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.allNetworkInfo
        for (ni in netInfo) {
            if (ni.typeName.equals("WIFI", ignoreCase = true))
                if (ni.isConnected)
                    haveConnectedWifi = true
            if (ni.typeName.equals("MOBILE", ignoreCase = true))
                if (ni.isConnected)
                    haveConnectedMobile = true
        }
        return haveConnectedWifi || haveConnectedMobile
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

    override fun onGetPinSucceeded(pin: String) {
        Log.e("pinCallback", pin)

        isVerifyPinType = true
        val intent = Intent(this, ConfirmPinActivity::class.java)
        intent.putExtra(
            "pin",
            pin.substring(
                pin.length - SettingSharePreference.getConstant(this).pinLength,
                pin.length
            )
        )
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

        val bleClient = BleClient()
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
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getBooleanExtra("pinVerified", false)
                if (result) {
                    mBLEAdapter!!.H2CRequestBLETrackerInit()
                    loadingDialog.setLoadingDescription("Loading: Starting tracker...")
                    loadingDialog.show()
                }
            } else {
                mBLEAdapter!!.RequestAppSuspend(0x01.toByte())
                mBLEAdapter!!.DisconnectCard()
                loadingDialog.setLoadingDescription("Loading: Canceling PIN authentication...")
                hideProgressBar()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (checkBlueToothAdapter()) {
            if (!isVerifyPinType && mBLEAdapter != null ) {
                mBLEAdapter!!.RequestAppSuspend(0x01.toByte())
                mBLEAdapter!!.DisconnectCard()
                if(checkBlueToothAdapter()) {
                    mBleScan!!.stopScanning()
                }

            }
        }

    }

    override fun onStart() {
        super.onStart()
        if (!isVerifyPinType) {
            if(checkBlueToothAdapter()) {
                mBleScan!!.startScanning()

            }
        }
    }

    override fun getSecureServerFailed(message: String?) {
        hideProgressBar()
        showDialog(message)
    }

    override fun showMessageError(message: String?) {
        hideProgressBar()
        showDialog(message)
    }

    private fun showProgressBar() {
        loadingDialog.show()
    }

    private fun hideProgressBar() {
        loadingDialog.dismiss()
    }

    override fun onResume() {
        super.onResume()
        if(checkBlueToothAdapter()) {
            mBleScan!!.startScanning()
        }

    }

    private fun showDialog(message: String?) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton(
                android.R.string.yes
            ) { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    override fun appRequiredToUpdate() {
        hideProgressBar()
        UpdateCardDialog(this, this).show()
    }

    override fun onUpdateButtonClicked() {
        mBLEAdapter!!.DisconnectCard()
        goToDeviceManager()
    }

    override fun onDisconnectButtonClicked() {
        mBLEAdapter!!.DisconnectCard()
    }

    override fun appMustBeUpdate() {

        hideProgressBar()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Updates required!")
            .setMessage("To update your device, please use Ethernom Device Manager.")
            .setCancelable(false)
            .setPositiveButton("Update") { dialog, _ ->
                goToDeviceManager()
                dialog.dismiss() }
            .show()
    }


    override fun onDoNotUpdateButtonClicked() {
        showProgressBar()
        mBLEAdapter!!.DisconnectCard()
        if (cardInfo != null) {
            mBLEAdapter!!.setUserRefuseUpdate(true)
            if(checkBlueToothAdapter()) {
                mBleScan!!.stopScanning()
            }
            mBLEAdapter!!.ConnectCard(cardInfo)
        }
    }

    private fun goToDeviceManager() {
        val packageName = "com.ethernom.dm.mobile"
        var intent = packageManager.getLaunchIntentForPackage(packageName)
        if (intent == null) {
            // Bring user to the market or let them choose an app?
            intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=$packageName")
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        if(checkBlueToothAdapter()) {
            mBleScan!!.stopScanning()
        }

        hideProgressBar()
    }

    private fun checkBlueToothAdapter(): Boolean {

        val bAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bAdapter != null) {
            return if (!bAdapter.isEnabled) {
//                val mIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                startActivityForResult(mIntent, 2)
                false

            } else true

        }
        return false
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val TAG: String = "DiscoverDeviceActivity"
    }
}
