package com.ethernom.helloworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
