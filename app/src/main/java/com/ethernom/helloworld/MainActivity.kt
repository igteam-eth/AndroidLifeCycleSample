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
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set counter to 0
        TrackerSharePreference.getConstant(this).scanCounter = 0

        var clickCount = 0
        button.setOnClickListener {
            clickCount++
            text_result.text  = "${text_result.text} \nhello world $clickCount"
        }
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
        }else{
            return true
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == 0){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d(MainActivity::class.java.simpleName, "requestWriteExternalStorage was granted")
            }else{
                Log.d(MainActivity::class.java.simpleName, "requestWriteExternalStorage was deny")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (requestWriteExternalStorage() && requestBluetoothPermission()){

            if (!TrackerSharePreference.getConstant(this).isAlreadyScan){
                TrackerSharePreference.getConstant(this).isAlreadyScan = true
                Log.d(TAG, "Not Already Scan")
                MyApplication.appendLog("Enqueue WorkManager   "+ MyApplication.getCurrentDate() + "\n")
                //OneTimeWorkRequest
                val oneTimeRequest = OneTimeWorkRequest.Builder(MyWorkManager::class.java)
                    .addTag("WORK_MANAGER")
                    .build()
                WorkManager.getInstance(this).enqueue(oneTimeRequest)

            }else{
                Log.d(TAG, "Already Scan")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStart() {

        super.onStart()
        requestBluetoothPermission()
        BleReceiver.stopSound()
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
        val TAG = MainActivity::class.java.simpleName
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }

}
