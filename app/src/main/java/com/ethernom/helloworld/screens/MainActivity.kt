package com.ethernom.helloworld.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.lang.Exception
import android.content.Intent
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.LinearLayoutManager
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.workmanager.MyWorkManager
import com.ethernom.helloworld.R
import com.ethernom.helloworld.adapter.RegisteredDeviceAdapter
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.dialog.DeleteDeviceBottomDialog
import com.ethernom.helloworld.dialog.ItemDeleteCallback
import com.ethernom.helloworld.model.BleClient
import com.ethernom.helloworld.receiver.AlarmReceiver
import com.ethernom.helloworld.receiver.BleReceiver
import com.ethernom.helloworld.service.KillTrackerService
import com.ethernom.helloworld.util.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_tracker.button_add
import kotlinx.android.synthetic.main.activity_tracker.rv_registered_device
import kotlinx.android.synthetic.main.toolbar_default.*
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : BaseActivity(), RegisteredDeviceAdapter.OnItemCallback,
    ItemDeleteCallback {

    private var registeredDeviceList: ArrayList<BleClient> = ArrayList()
    private var registeredDeviceAdapter: RegisteredDeviceAdapter? = null
    private lateinit var trackerSharePreference: TrackerSharePreference


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onCreate called\n")

        TrackerSharePreference.getConstant(this).isRanging = false

        trackerSharePreference = TrackerSharePreference.getConstant(this)
        registeredDeviceAdapter = RegisteredDeviceAdapter(registeredDeviceList, this)
        rv_registered_device.layoutManager =
            LinearLayoutManager(this)
        rv_registered_device.adapter = registeredDeviceAdapter

        button_add.setOnClickListener {

            Utils.preventDoubleClick(it)

            val animation = AlphaAnimation(1f, 0.8f)
            it.startAnimation(animation)
            if (registeredDeviceList.isEmpty()) {
                if (checkBlueToothAdapter())
                    startActivity(Intent(this, DiscoverDeviceActivity::class.java))
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Please delete your current device before registering a new one.")
                    .setPositiveButton(
                        android.R.string.yes
                    ) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
        button_setting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
            Utils.preventDoubleClick(it)
        }

        button_question_mark.setOnClickListener {
            Utils.preventDoubleClick(it)
            MyApplication.showAlertDialog(
                this,
                "Unable to see your device?",
                "Please make sure to add your device by using \"Add device\" button"
            )
        }

        try {
            val isNotification = intent.getBooleanExtra("NOTIFICATION", false)
            if (isNotification) {
                TrackerSharePreference.getConstant(this).isAlreadyCreateWorkerThread = false
                MyApplication.appendLog("${MyApplication.getCurrentDate()} : User was click notification to open the app: isAlreadyCreateWorkerThread = false\n")
            }
            BleReceiver.stopSound()

        } catch (e: Exception) {
            MyApplication.appendLog("${MyApplication.getCurrentDate()} : Error " + e.message + "\n")
        }

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called \n")

        startService(Intent(this, KillTrackerService::class.java))

        MyApplication.appendLog("${MyApplication.getCurrentDate()} : startService KillTrackerService in onStart\n")
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onResume called\n")

        if (checkBlueToothAdapter() && requestWriteExternalStorage() && requestBluetoothPermission()) {

            if (trackerSharePreference.isCardExisted) {

                displayEthernomCard()

                if (!TrackerSharePreference.getConstant(this).isAlreadyCreateWorkerThread) {

                    var numDelay = 0
                    TrackerSharePreference.getConstant(this).isAlreadyCreateWorkerThread = true
                    MyApplication.appendLog("${MyApplication.getCurrentDate()} : Enqueue WorkManager\n")

                    if (TrackerSharePreference.getConstant(this).isBeaconTimeStamp != "") {
                        val diffInMs =
                            SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS").parse(MyApplication.getCurrentDate()).time - SimpleDateFormat(
                                "dd/MM/yyyy HH:mm:ss.SSS"
                            ).parse(TrackerSharePreference.getConstant(this).isBeaconTimeStamp).time
                        val diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMs)

                        Log.d(TAG, "Seconds: $diffInSec")


                        if (diffInSec >= DELAY_PERIOD) {
                            numDelay = 0
                        } else {
                            numDelay = (DELAY_PERIOD - diffInSec).toInt()
                        }
                    }

                    Log.d(TAG, "Delay Seconds: $numDelay")
                    TrackerSharePreference.getConstant(this).setIsAlreadyScan(false)
                    //OneTimeWorkRequest
                    val oneTimeRequest = OneTimeWorkRequest.Builder(MyWorkManager::class.java)
                        .addTag("WORK_MANAGER")
                        .setInitialDelay(numDelay.toLong(), TimeUnit.SECONDS)
                        .build()
                    WorkManager.getInstance(this).enqueue(oneTimeRequest)
                    Log.d(TAG, "Enqueue WORK_MANAGER")
                }

                MyApplication.appendLog("${MyApplication.getCurrentDate()} : Host brand " + Build.BRAND + "\n")

                if (Build.BRAND.equals("samsung", ignoreCase = true)) {
                    MyApplication.appendLog("${MyApplication.getCurrentDate()} : Alarm Enabled \n")

                    if (!TrackerSharePreference.getConstant(this).isAlreadyCreateAlarm) {
                        TrackerSharePreference.getConstant(this).isAlreadyCreateAlarm = true
                        val startIntent = Intent(this, AlarmReceiver::class.java)
                        sendBroadcast(startIntent)
                    }
                }
            }


        }
    }

    private fun displayEthernomCard() {
        registeredDeviceList.clear()
        val bleClient: BleClient = trackerSharePreference.ethernomCard
        registeredDeviceList.add(bleClient)
        registeredDeviceAdapter!!.notifyDataSetChanged()
    }

    override fun ItemClickListener(position: Int) {
        Log.d(TAG, "Name: " + registeredDeviceList[position].devName)
        DeleteDeviceBottomDialog(registeredDeviceList[position].devName, this, this).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemDeleteClicked() {
        registeredDeviceList.clear()
        registeredDeviceAdapter!!.notifyDataSetChanged()
        trackerSharePreference.clearAll()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onPause called\n")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onRestart called\n")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onStop called\n")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onDestroy called\n")
    }

    private fun requestWriteExternalStorage(): Boolean {
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : requestWriteExternalStorage called\n")
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0
            )
            return false
        } else {
            return true
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

    private fun checkBlueToothAdapter(): Boolean {

        val bAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bAdapter != null) {
            return if (!bAdapter.isEnabled) {
                val mIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(mIntent, 2)
                false

            } else true

        }
        return false
    }


    companion object {
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
        const val TAG = "APP_MainActivity"
        const val DELAY_PERIOD = 12
    }

}
