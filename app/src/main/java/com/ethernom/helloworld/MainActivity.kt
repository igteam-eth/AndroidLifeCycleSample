package com.ethernom.helloworld

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var TAG = "APP_MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onCreate called\n")
        var clickCount = 0
        button.setOnClickListener {
            clickCount++
            text_result.text = "${text_result.text} \nhello world $clickCount"
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onStart called \n")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onResume called\n")

        if (requestWriteExternalStorage()){

            MyApplication.appendLog("${MyApplication.getCurrentDate()} : Enqueue WorkManager\n")
            //OneTimeWorkRequest
            val oneTimeRequest = OneTimeWorkRequest.Builder(MyWorkManager::class.java)
                .addTag("WORK_MANAGER")
                .build()
            WorkManager.getInstance(this).enqueue(oneTimeRequest)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onPause called\n")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onRestart called\n")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onStop called\n")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called \n")
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : onDestroy called\n")
    }

    private fun requestWriteExternalStorage(): Boolean {
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : requestWriteExternalStorage called\n")
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
        } else {
            return true
        }
    }
}
