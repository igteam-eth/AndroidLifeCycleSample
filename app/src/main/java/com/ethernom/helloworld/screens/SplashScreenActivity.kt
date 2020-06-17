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
import com.ethernom.helloworld.application.SettingSharePreference
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

        // Every startup app change current state to Initialize state it also handle app terminate state too
        TrackerSharePreference.getConstant(this).currentState = StateMachine.INITIAL.value

        if (TrackerSharePreference.getConstant(this).isRanging) {
            BeaconReceiver.stopSound()
        }

        val versionName = BuildConfig.VERSION_NAME
        txt_version.text = "Version: $versionName"

        //initialize bluetooth and location
        Utils.initBLE_Location(this)

        MyApplication.saveCurrentStateToLog(this)
        Utils.removeNotificationByID(this, Utils.CHANNEL_RANG)

        checkWriteExternalStoragePermission{
            if (SettingSharePreference.getConstant(this).isBeforeActivate) {
                checkLocationPermission {
                    // For User experience at SplashScreen Just alive 2 or 3 Seconds after that intent to screen follow by Initialize state of state table
                    Handler().postDelayed({
                        // go to initial state
                        // In Initial State class we study with input event , state variable and action function for intent to next state
                        InitializeState().goToInitialState(this)

                    }, 2000)
                    // Check for display before Activation screen easy user to allow app permission required

                }

            } else {
                startActivity(Intent(this, BeforeActivateActivity::class.java))
                finish()
            }
        }

    }



    companion object {
        var TAG = SplashScreenActivity::class.java.simpleName;
    }

}
