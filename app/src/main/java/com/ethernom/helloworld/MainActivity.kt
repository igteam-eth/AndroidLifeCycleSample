package com.ethernom.helloworld

import android.bluetooth.BluetoothAdapter
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

        //Tracker state machine
        //Initialize state variable
        //Default value state machine = initial(0000)
        //Default value card register = false
        Log.e("EthernomHelloworld","Current State:"+ TrackerSharePreference.getConstant(this).currentState)
        //Start
        Log.e("EthernomHelloworld","Is Card Register: ${TrackerSharePreference.getConstant(this).isCardRegistered}")
        if(TrackerSharePreference.getConstant(this).isCardRegistered) {
            // Go to Beacon receive (2000) state
            TrackerSharePreference.getConstant(this).currentState = StateMachine.BEACON_RECEIVE.value
        } else {
            // Go to Card discover (1000) state
            TrackerSharePreference.getConstant(this).currentState = StateMachine.CARD_DIS_REG.value
        }
        Log.e("EthernomHelloworld","Current State:"+ TrackerSharePreference.getConstant(this).currentState)

        // Register for broadcasts on BluetoothAdapter state change
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(BluetoothStateChangeReceiver(), filter)

        //end of tracker state machine
        Log.e("LifeCycle", "OnCreate Called")
        var clickCount = 0
        button.setOnClickListener {
            clickCount++
            text_result.text  = "${text_result.text} \nhello world $clickCount"
        }

    }
    override fun onStart() {
        super.onStart()
        Log.e("LifeCycle", "OnStart Called")
    }

    override fun onResume() {
        super.onResume()
        Log.e("LifeCycle", "OnResume Called")
    }

    override fun onPause() {
        super.onPause()
        Log.e("LifeCycle", "OnPause Called")
    }

    override fun onRestart() {
        super.onRestart()
        Log.e("LifeCycle", "OnRestart Called")
    }

    override fun onStop() {
        super.onStop()
        Log.e("LifeCycle", "OnStop Called")
    }

    override fun onDestroy() {
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show()
        Log.e("LifeCycle", "OnDestroy Called")
        super.onDestroy()


    }
}
