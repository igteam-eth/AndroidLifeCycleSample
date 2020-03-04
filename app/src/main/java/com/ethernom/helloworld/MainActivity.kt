package com.ethernom.helloworld

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e("LifeCycle", "OnCreate Called")
        var clickCount = 0
        button_update_content.setOnClickListener {
            clickCount++
            text_result.text  = "${text_result.text} \nhello world $clickCount"

        }

        // navigate to transparent screen to track only onPause() and onResume()
        button_go_to_second_screen.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
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
        Handler().postDelayed({
            Log.e("LifeCycle","5 seconds interval called")

        }, 5000)
    }

    override fun onDestroy() {

        Log.e("LifeCycle", "OnDestroy Called")
        super.onDestroy()


    }
}
