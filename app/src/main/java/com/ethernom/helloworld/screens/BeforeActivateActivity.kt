package com.ethernom.helloworld.screens

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.ethernom.helloworld.R
import com.ethernom.helloworld.statemachine.InitializeState
import com.ethernom.helloworld.util.Utils
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_before_activate.*

@Suppress("DEPRECATION")
class BeforeActivateActivity : BaseActivity() {
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    var btStatus = false
    var lcStatus = false
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_before_activate)


        BtEnlabel.setOnClickListener {
            Log.d(TAG, "BtEnlabel clicked")
            turnOnBluetooth()
            BtEnlabel.visibility = View.INVISIBLE
            BtEnImg.visibility = View.VISIBLE
            btStatus = true
            checkToChangeLayout()
        }
        toolbar_before_registered.visibility = View.GONE

        LcEnlabel.setOnClickListener {
            Log.d(TAG, "LcEnlabel clicked")

            if(!Utils.isLocationEnabled(this)) {
                val mIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(mIntent)
            } else {
                Log.d(TAG, "LcEnlabel requestLocationPermission()")
               if(requestLocationPermission()){
                   LcEnlabel.visibility = View.INVISIBLE
                   LcEnImg.visibility = View.VISIBLE
                   btStatus = true
               }
            }
        }

        btn_next.setOnClickListener {
            InitializeState().goToInitialState(this)
        }
    }

    override fun onResume() {
        super.onResume()
        checkToChangeLayout()
    }
    fun checkToChangeLayout() {
        Log.d(TAG, "Utils.isBluetoothEnable() ${Utils.isBluetoothEnable()}")
        Log.d(TAG, "Utils.isLocationEnabled(this) ${Utils.isLocationEnabled(this)}")

        if(Utils.isBluetoothEnable()) {
            BtEnlabel.visibility = View.INVISIBLE
            BtEnImg.visibility = View.VISIBLE
        } else {
            BtEnlabel.visibility = View.VISIBLE
            BtEnImg.visibility = View.INVISIBLE
        }

        if(Utils.isLocationEnabled(this) && checkLocationPermission()) {
            LcEnlabel.visibility = View.INVISIBLE
            LcEnImg.visibility = View.VISIBLE
        } else if(Utils.isLocationEnabled(this)) {
            LcEnlabel.visibility = View.VISIBLE
            LcEnlabel.text = "ALLOW"
            LcEnImg.visibility = View.INVISIBLE
        } else {
            LcEnlabel.visibility = View.VISIBLE
            LcEnlabel.text = "ENABLE"
            LcEnImg.visibility = View.INVISIBLE
        }

        if(checkLocationPermission() && Utils.isBluetoothEnable() && Utils.isLocationEnabled(this)) {
            btStatus = true
            lcStatus = true

        }

        if (btStatus && lcStatus) {
            btn_next.background = resources.getDrawable(R.drawable.button_shape)
            btn_next.isEnabled = true
        } else {
            btn_next.background = resources.getDrawable(R.drawable.black_background_button)
            btn_next.isEnabled = false

        }


    }
    fun turnOnBluetooth(): Boolean {
        val bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter()
        return bluetoothAdapter?.enable() ?: false
    }
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

    fun checkLocationPermission(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            Log.d(MainActivity.TAG, "Checking Bluetooth permissions")
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            } else {
                Log.d(MainActivity.TAG, "  Permission is granted")
                return true
            }
        } else return false
    }
}