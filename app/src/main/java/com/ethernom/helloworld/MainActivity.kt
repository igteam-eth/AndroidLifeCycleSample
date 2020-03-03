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

        var clickCount = 0
        button.setOnClickListener {
            clickCount++
            text_result.text  = "${text_result.text} \nhello world $clickCount"
        }
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
