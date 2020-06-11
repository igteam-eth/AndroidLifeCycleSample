package com.ethernom.helloworld.screens

import android.os.Bundle

class LocationBLENotifyUserActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val BLELocation = intent.getStringExtra("BLELocation")
        if (BLELocation == "ble") {
            checkBluetoothSate {
                if(it) {
                    finish()
                }
            }
        } else {
            checkLocationState {
                if (it) {
                    finish()
                }
            }
        }

    }
}