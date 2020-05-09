package com.ethernom.helloworld.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ethernom.helloworld.application.MyApplication

import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.receiver.BleReceiver
import com.ethernom.helloworld.workmanager.MyWorkManager

class KillTrackerService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("KillTrackerService", "onStartCommand")
        return super.onStartCommand(intent, flags, startId)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        Log.d("KillTrackerService", "onDestroy isRaning = ${TrackerSharePreference.getConstant(applicationContext).isRanging}")

        MyApplication.appendLog(MyApplication.getCurrentDate() + " : KillTrackerService  isRanging = ${TrackerSharePreference.getConstant(applicationContext).isRanging}  " + "\n")

        //if the app is ranging but user don't click the notification or acknowledge so we need to rearm scan
        if (TrackerSharePreference.getConstant(applicationContext).isRanging && TrackerSharePreference.getConstant(applicationContext).isCardExisted) {

            MyApplication.appendLog(MyApplication.getCurrentDate() + " : KillTrackerService  onDestroy  " + "\n")

            //OneTimeWorkRequest
            val oneTimeRequest = OneTimeWorkRequest.Builder(MyWorkManager::class.java)
                .addTag("WORK_MANAGER")
                .build()
            WorkManager.getInstance(applicationContext).enqueue(oneTimeRequest)

            TrackerSharePreference.getConstant(applicationContext).isAlreadyCreateWorkerThread = true
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : KillTrackerService  isAlreadyCreateWorkerThread = true   " + "\n")
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : KillTrackerService  Launch MyWorkManager  " + "\n")
            Log.d("KillTrackerService", "onDestroy Start scan")

        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
       stopSelf()
    }
}
