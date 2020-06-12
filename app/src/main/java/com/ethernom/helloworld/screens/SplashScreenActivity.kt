package com.ethernom.helloworld.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ethernom.helloworld.BuildConfig
import com.ethernom.helloworld.R
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.receiver.BeaconReceiver
import com.ethernom.helloworld.statemachine.InitializeState
import com.ethernom.helloworld.util.StateMachine
import com.ethernom.helloworld.util.Utils
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreenActivity : BaseActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)

        if (TrackerSharePreference.getConstant(this).isRanging) {
            BeaconReceiver.stopSound()
        }

        val versionCode = BuildConfig.VERSION_CODE
        val versionName = BuildConfig.VERSION_NAME
        txt_version.text = "Version: $versionName.$versionCode"

        //initialize bluetooth and location
        Utils.initBLE_Location(this)

        MyApplication.saveCurrentStateToLog(this)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        if (requestWriteExternalStoragePermission() && requestLocationPermission()) {

            Handler().postDelayed({
                // go to initial state
                InitializeState().goToInitialState(this)

            }, 2000)


        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun requestLocationPermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            Log.d(MainActivity.TAG, "Checking Bluetooth permissions")
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
                        MainActivity.PERMISSION_REQUEST_COARSE_LOCATION
                    )

                } else {

                    requestPermissions(
                        arrayOf(
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ),
                        MainActivity.PERMISSION_REQUEST_COARSE_LOCATION
                    )
                }
                return false
            } else {
                Log.d(MainActivity.TAG, "  Permission is granted")
                return true
            }
        } else return false
    }

    private fun requestWriteExternalStoragePermission(): Boolean {
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


}
