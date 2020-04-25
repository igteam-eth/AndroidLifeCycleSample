package com.ethernom.helloworld.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
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
import com.ethernom.helloworld.model.BleClient
import com.ethernom.helloworld.model.DataModel
import com.ethernom.helloworld.model.DataResponseModel
import com.ethernom.helloworld.model.HostModel
import com.ethernom.helloworld.receiver.AlarmReceiver
import com.ethernom.helloworld.receiver.BleReceiver
import com.ethernom.helloworld.webservice.ApiClient
import kotlinx.android.synthetic.main.activity_tracker.*
import kotlinx.android.synthetic.main.toolbar_default.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), RegisteredDeviceAdapter.OnItemCallback, ItemDeleteCallback  {

    private var registeredDeviceList: ArrayList<BleClient> = ArrayList()
    private var registeredDeviceAdapter: RegisteredDeviceAdapter? = null
    private lateinit var trackerSharePreference: TrackerSharePreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onCreate called\n")


        trackerSharePreference = TrackerSharePreference.getConstant(this)
        registeredDeviceAdapter =  RegisteredDeviceAdapter(registeredDeviceList, this)
        rv_registered_device.layoutManager = LinearLayoutManager(this)
        rv_registered_device.adapter =registeredDeviceAdapter

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

        try{
            val isNotification = intent.getBooleanExtra("NOTIFICATION", false)
            if (isNotification){
                TrackerSharePreference.getConstant(this).isAlreadyCreateWorkerThread = false
                MyApplication.appendLog("${MyApplication.getCurrentDate()} : User was click notification to open the app: isAlreadyCreateWorkerThread = false\n")
            }
        }catch (e: Exception){
            MyApplication.appendLog("${MyApplication.getCurrentDate()} : Error " + e.message + "\n")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onStart called \n")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onResume called\n")

        if (requestWriteExternalStorage() && requestBluetoothPermission() ){

            if (trackerSharePreference.isCardExisted) {

                displayEthernomCard()

                if (!TrackerSharePreference.getConstant(this).isAlreadyCreateWorkerThread){
                    TrackerSharePreference.getConstant(this).isAlreadyCreateWorkerThread = true
                    MyApplication.appendLog("${MyApplication.getCurrentDate()} : Enqueue WorkManager\n")
                    //OneTimeWorkRequest
                    val oneTimeRequest = OneTimeWorkRequest.Builder(MyWorkManager::class.java)
                        .addTag("WORK_MANAGER")
                        .build()
                    WorkManager.getInstance(this).enqueue(oneTimeRequest)
                }

                MyApplication.appendLog("${MyApplication.getCurrentDate()} : Host brand " + Build.BRAND + "\n")

                if (Build.BRAND.equals("samsung", ignoreCase = true)) {
                    MyApplication.appendLog("${MyApplication.getCurrentDate()} : Alarm Enabled \n")

                    if (!TrackerSharePreference.getConstant(this).isAlreadyCreateAlarm){
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
        DeleteDeviceBottomDialog(this, this ).show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemDeleteClicked() {
        BleReceiver.stopSound()
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
    fun requestBluetoothPermission() : Boolean {

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
                return false
            } else {
                Log.d(TAG, "  Permission is granted")
                return true
            }
        }else return false
    }


    private fun getData() {
        val token = "6$9a(_@|A3nr+p3y-wL$1@8*VFKW,Qt.m@O:fnqo<8#_4wG}pwJvIB*pxKb.sL3r"
        val auth = "Bearer $token"

        var mHost = HostModel("Ethernom, Inc.", "BLE Tracker", "com.ethernom.ble.tracker", "android","1.0.13" )

        var mData =  DataModel("00010002000000b0", "A19100503844", mHost)
        val call: Call<DataResponseModel> = ApiClient.getClient.getAppKey(auth, mData)
        call.enqueue(object : Callback<DataResponseModel> {

            override fun onResponse(call: Call<DataResponseModel>?, response: Response<DataResponseModel>?) {
                Log.d(TAG, "onResponse "+response!!.body().toString())
            }

            override fun onFailure(call: Call<DataResponseModel>?, t: Throwable?) {
                Log.d(TAG, "onFailure "+t!!.message.toString())
            }

        })
    }
    companion object{
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
        const val TAG = "APP_MainActivity"
    }
}
