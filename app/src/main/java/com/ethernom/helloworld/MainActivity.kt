package com.ethernom.helloworld

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var clickCount = 0
        button.setOnClickListener {
            clickCount++
            text_result.text  = "${text_result.text} \nhello world $clickCount"
        }
    }

    private fun requestWriteExternalStorage(): Boolean {
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
        }else{
            return true
        }
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == 0){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Log.d(MainActivity::class.java.simpleName, "requestWriteExternalStorage was granted")
            }else{
                Log.d(MainActivity::class.java.simpleName, "requestWriteExternalStorage was deny")
            }
        }
    }



    override fun onStart() {
        super.onStart()
        Log.e("LifeCycle", "OnStart Called")
    }

    override fun onResume() {
        super.onResume()
        if (requestWriteExternalStorage()){


            MyApplication.appendLog("MainActivity   "+ MyApplication.getCurrentDate() + "\n")
            //OneTimeWorkRequest
            val oneTimeRequest = OneTimeWorkRequest.Builder(MyWorkManager::class.java)
                .addTag("WORK_MANAGER")
                .build()
            WorkManager.getInstance(this).enqueueUniqueWork("WORK_MANAGER", ExistingWorkPolicy.REPLACE, oneTimeRequest)

        }

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
