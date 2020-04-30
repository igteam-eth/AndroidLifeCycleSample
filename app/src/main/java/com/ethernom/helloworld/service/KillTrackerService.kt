package com.ethernom.helloworld.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi

import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.receiver.BleReceiver

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
        Log.d("KillTrackerService", "onDestroy")
        //if the app is ranging but user don't click the notification or acknowledge so we need to rearm scan
        if (TrackerSharePreference.getConstant(applicationContext).isRanging) {
            TrackerSharePreference.getConstant(this).isAlreadyCreateWorkerThread = false
            BleReceiver.startScan(applicationContext)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
       stopSelf()
    }
}
