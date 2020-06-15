package com.ethernom.helloworld.screens

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
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
import com.ethernom.helloworld.adapter.DeviceAdapter
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.SettingSharePreference
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.callback.StateMachineCallback
import com.ethernom.helloworld.dialog.LoadingDialog
import com.ethernom.helloworld.dialog.UpdateCardDialog
import com.ethernom.helloworld.model.BleClient
import com.ethernom.helloworld.model.CardInfo
import com.ethernom.helloworld.statemachine.CardRegisterState
import com.ethernom.helloworld.statemachine.FirmwareInfoState
import com.ethernom.helloworld.statemachine.GetPrivateKeyState
import com.ethernom.helloworld.util.BLEScan
import com.ethernom.helloworld.util.CardConnection.*
import com.ethernom.helloworld.util.StateMachine
import java.util.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class DiscoverDeviceActivity : BaseActivity(), DeviceAdapter.OnItemCallback,
    BLEScan.DeviceDiscoveredCallBack, UpdateCardDialog.Callback, StateMachineCallback {

    private var mBTDevicesArrayList: ArrayList<BleClient>? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: DeviceAdapter? = null
    private var mBleScan: BLEScan? = null
    private var selectedPos: Int? = null
    private lateinit var loadingDialog: LoadingDialog

    private var cardInfo: CardInfo? = null
    private var isVerifyPinType = false
    private var firmwareInfoState: FirmwareInfoState? = null
    private lateinit var updateCardDialog: UpdateCardDialog
    private lateinit var alertDialog: AlertDialog
    private lateinit var alertDialogBuilder: AlertDialog.Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_device)

        checkLocationPermission()
        loadingDialog = LoadingDialog(this)
        updateCardDialog = UpdateCardDialog(this, this)
        alertDialogBuilder = AlertDialog.Builder(this)
        alertDialog = alertDialogBuilder.create()

        initBluetoothConnection(this, loadingDialog)

        firmwareInfoState = FirmwareInfoState(this)
        mBTDevicesArrayList = ArrayList()
        setUpList()
        MyApplication.saveCurrentStateToLog(this)

    }

    // User Select Card
    override fun ItemClickListener(position: Int) {

        checkBluetoothSate { isBTOn ->
            if (isBTOn) {
                showProgressBar()
                Log.d("onItemClick", "" + position)
                selectedPos = position

                cardInfo = CardInfo(
                    mBTDevicesArrayList!![position].devName,
                    mBTDevicesArrayList!![position].macAddress,
                    ""
                )

                //loadingDialog.setLoadingDescription("Loading: Connecting " + mBTDevicesArrayList!![position].devName + "...")
                loadingDialog.setLoadingDescription("Loading: Get Card ID...")


                TrackerSharePreference.getConstant(this).currentState =
                    StateMachine.GET_FIRMWARE_INFO.value

                firmwareInfoState!!.cardInfo = cardInfo
                // Establish Connection
                firmwareInfoState!!.establishBLEConnection()
            }

        }

    }

    // Card Advertising Packets
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
            TrackerSharePreference.getConstant(this).currentState =
                StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.value
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

    // Get Major and Minor Success
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

        TrackerSharePreference.getConstant(this).isCardRegistered = true
        TrackerSharePreference.getConstant(this).currentState =
            StateMachine.WAITING_FOR_BEACON.value

        // Display Card Registered
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
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
                    TrackerSharePreference.getConstant(this).currentState =
                        StateMachine.CARD_REGISTER.value
                    CardRegisterState(this).H2CRequestBLETrackerInit()

                    //loadingDialog.setLoadingDescription("Loading: Starting tracker...")
                    loadingDialog.show()

                }
            } else {
                firmwareInfoState!!.RequestAppSuspend(0x01.toByte())
                firmwareInfoState!!.DisconnectCard()
                //loadingDialog.setLoadingDescription("Loading: Canceling PIN authentication...")
                hideProgressBar()
                // Next state
                initState()

            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (TrackerSharePreference.getConstant(this).isBLEStatus) {
            if (!isVerifyPinType && gatt != null) {
                firmwareInfoState!!.RequestAppSuspend(0x01.toByte())
                firmwareInfoState!!.DisconnectCard()
                updateCardDialog.dismiss()
                hideDialog()
                mBleScan!!.stopScanning()
            }
        }
    }

    override fun hideProgressBarState() {
        hideProgressBar()
    }

    override fun showMessageErrorState(message: String) {
        hideProgressBar()
        showDialog(message)
    }

    override fun unknownEvent() {
        showDialog("Unknown event was called.")
    }

    private fun showProgressBar() {
        loadingDialog.show()
    }

    private fun hideProgressBar() {
        loadingDialog.dismiss()
    }

    //  StartScan General Advertising
    override fun onResume() {
        super.onResume()
        if (!isVerifyPinType) {
            if (TrackerSharePreference.getConstant(this).isBLEStatus) {
                try {
                    mBleScan = BLEScan(this, this)
                    mBleScan!!.startScanning()
                    TrackerSharePreference.getConstant(this).currentState =
                        StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.value
                }catch (e: java.lang.Exception){
                    e.printStackTrace()
                }

            }
        }
    }

    override fun readyToDiscoverDevice() {

        mBleScan = BLEScan(this, this)
        mBleScan!!.startScanning()
    }

    private fun hideDialog() {
        alertDialog.dismiss()
    }

    private fun showDialog(message: String?) {

        alertDialogBuilder.apply {
            setTitle("Error")
            setMessage(message)
            setPositiveButton(
                android.R.string.yes
            ) { dialog, _ ->
                // Next state
                initState()
                dialog.dismiss()
            }
            show()
        }
    }

    // Server Response Update Needed
    override fun appRequiredToUpdate() {
        hideProgressBar()
        if (isScreenPresent)
            updateCardDialog.show()

    }

    override fun checkUpdateFailed(message: String) {
        hideProgressBar()
        showDialog("Make sure your device is powered on and authenticated. Please try again.")

    }

    override fun getPinSucceeded(pin: String) {
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

    override fun getPrivateKeyFailed(message: String) {
        hideProgressBar()
        showDialog(message)
    }

    override fun onUpdateButtonClicked() {
        firmwareInfoState!!.DisconnectCard()
        firmwareInfoState!!.setAlreadyCallDisconnect(false)
        // Next state
        initState()
        goToDeviceManager()

    }

    override fun onDisconnectButtonClicked() {
        firmwareInfoState!!.DisconnectCard()
        // Next state
        initState()

    }

    override fun appMustBeUpdate() {

        hideProgressBar()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Updates required!")
            .setMessage("To update your device, please use Ethernom Device Manager.")
            .setCancelable(false)
            .setPositiveButton("Update") { dialog, _ ->
                //  Next state
                initState()
                goToDeviceManager()
                dialog.dismiss()
            }
            .show()
    }


    override fun onDoNotUpdateButtonClicked() {
        showProgressBar()
        if (cardInfo != null) {
            isUserRefuseUpdate = true

            if (TrackerSharePreference.getConstant(this).isBLEStatus) {
                mBleScan!!.stopScanning()
            }

            TrackerSharePreference.getConstant(this).currentState =
                StateMachine.GET_PRIVATE_KEY.value
            GetPrivateKeyState(this, stateMachineCallback).get(serialNumber, menuFac)
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
        if (TrackerSharePreference.getConstant(this).isBLEStatus) {
            try {
                mBleScan!!.stopScanning()
            }catch (e: Exception){
                e.printStackTrace()
            }

        }

        hideProgressBar()
    }

    private fun initState() {
        //Default value state machine = initial(0000)
        TrackerSharePreference.getConstant(this).currentState = StateMachine.INITIAL.value
        val intent = Intent(this, SplashScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        const val TAG: String = "DiscoverDeviceActivity"
    }
}
