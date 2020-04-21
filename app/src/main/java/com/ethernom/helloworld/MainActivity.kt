package com.ethernom.helloworld

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
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import android.content.Intent



class MainActivity : AppCompatActivity() {

    private var TAG = "APP_MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var clickCount = 0
        button.setOnClickListener {
            clickCount++
            text_result.text = "${text_result.text} \nhello world $clickCount"
        }

        try{
            val isNotification = intent.getBooleanExtra("NOTIFICATION", false)
            if (isNotification){
                TrackerSharePreference.getConstant(this).isAlreadyCreateWorkerThread = false
                MyApplication.appendLog("${MyApplication.getCurrentDate()} : Notification was clicked and change isAlreadyCreateWorkerThread = false for rearming scan.\n")
            }

        }catch (e: Exception){
            MyApplication.appendLog("${MyApplication.getCurrentDate()} : Error "+ e.message + "\n")
        }

        Log.d(TAG, "onCreate called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onCreate stage is called\n")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onStart stage is called \n")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onResume stage is called\n")

        if (requestWriteExternalStorage() && requestBluetoothPermission() ){

            if (!TrackerSharePreference.getConstant(this).isAlreadyCreateWorkerThread){
                TrackerSharePreference.getConstant(this).isAlreadyCreateWorkerThread = true
                MyApplication.appendLog("${MyApplication.getCurrentDate()} : Enqueue MyWorkManager\n")
                //OneTimeWorkRequest
                val oneTimeRequestWork = OneTimeWorkRequest.Builder(MyWorkManager::class.java)
                    .addTag("WORK_MANAGER")
                    .build()
                WorkManager.getInstance(this).enqueue(oneTimeRequestWork)
            }


            if (!TrackerSharePreference.getConstant(this).isAlreadyCreateAlarmWorkerThread){

                TrackerSharePreference.getConstant(this).isAlreadyCreateAlarmWorkerThread = true
                MyApplication.appendLog("${MyApplication.getCurrentDate()} : Enqueue AlarmWorkManager\n")
                //OneTimeWorkRequest
                val alarmOneTimeRequestWork = OneTimeWorkRequest.Builder(AlarmWorkManager::class.java)
                    .addTag("ALARM_WORK_MANAGER")
                    .build()
                WorkManager.getInstance(this).enqueue(alarmOneTimeRequestWork)
            }





        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onPause stage is called\n")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onRestart stage is called\n")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onStop stage is called\n")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onDestroy stage is called\n")
    }

    private fun requestWriteExternalStorage(): Boolean {
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
    companion object{
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }
}
