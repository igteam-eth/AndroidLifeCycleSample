package com.ethernom.helloworld.screens


import android.app.Activity
import android.os.Build
import android.os.Bundle

import androidx.annotation.RequiresApi
import com.ethernom.helloworld.application.MyApplication
import com.ethernom.helloworld.dialog.NotifyRangCallback
import com.ethernom.helloworld.dialog.NotifyRangDialog
import com.ethernom.helloworld.receiver.BeaconReceiver


class ForegroundNotifyRangActivity : Activity(), NotifyRangCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotifyRangDialog(this, this).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onButtonClicked() {
        BeaconReceiver.stopSound()
        MyApplication.appendLog("${MyApplication.getCurrentDate()} : User was click notification to open the app: isAlreadyCreateWorkerThread = false\n")
        finish()
    }
}
