package com.ethernom.helloworld

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    enum class StateMachine(val value: String){
        INITIAL("0000"),
        CARD_DIS_REG("1000"),
        CARD_REG("1001"),
        BEACON_RECEIVE("2000"),
        BT_STATE_OFF("2001"),
        WAITING_FOR_BEACON("2002")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("LifeCycle", "OnCreate Called")
        saveToLog("onCreate")

        //save log to file request
        requestWriteExternalStorage()

        //Tracker state machine
        //Initialize state variable
        //Default value state machine = initial(0000)
        //Default value card register = false
        Log.e("EthernomHelloworld","Current State:"+ TrackerSharePreference.getConstant(this).currentState)
        MyApplication.appendLog(MyApplication.getCurrentDate() + " : Current State : " + TrackerSharePreference.getConstant(this).currentState + " \n")

                //Start
        Log.e("EthernomHelloworld","Is Card Register: ${TrackerSharePreference.getConstant(this).isCardRegistered}")
        MyApplication.appendLog(MyApplication.getCurrentDate() +" : Is Card Register: ${TrackerSharePreference.getConstant(this).isCardRegistered}  \n")


        if(TrackerSharePreference.getConstant(this).isCardRegistered) {
            // Go to Beacon receive (2000) state
            TrackerSharePreference.getConstant(this).currentState = StateMachine.BEACON_RECEIVE.value
        } else {
            // Go to Card discover (1000) state
            TrackerSharePreference.getConstant(this).currentState = StateMachine.CARD_DIS_REG.value
            startActivity(Intent(this, DiscoverDeviceActivity::class.java))
        }
        Log.e("EthernomHelloworld","Current State:"+ TrackerSharePreference.getConstant(this).currentState)
        MyApplication.appendLog(MyApplication.getCurrentDate() + " : Current State : " + TrackerSharePreference.getConstant(this).currentState + " \n")


        // Register for broadcasts on BluetoothAdapter state change
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(BluetoothStateChangeReceiver(), filter)

        //end of tracker state machine

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

    override fun onStart() {
        super.onStart()
        Log.e("LifeCycle", "OnStart Called")
        saveToLog("onStart")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        Log.e("LifeCycle", "OnResume Called")
        saveToLog("onResume")

       requestBluetoothPermission()
    }

    override fun onPause() {
        super.onPause()
        Log.e("LifeCycle", "OnPause Called")
        saveToLog("onPause")
    }

    override fun onRestart() {
        super.onRestart()
        Log.e("LifeCycle", "OnRestart Called")
        saveToLog("onRestart")
    }

    override fun onStop() {
        super.onStop()
        Log.e("LifeCycle", "OnStop Called")
        saveToLog("onStop")
    }

    override fun onDestroy() {
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show()
        Log.e("LifeCycle", "OnDestroy Called")
        super.onDestroy()
        saveToLog("onDestroy")
    }

    private fun saveToLog(state: String) {
        MyApplication.appendLog(MyApplication.getCurrentDate() + " : " + javaClass.simpleName + " =======> " + state + "\n")
    }

    companion object {
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
        const val TAG = "APP_MainActivity"
    }

}
