package com.ethernom.helloworld.screens


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle

import androidx.annotation.RequiresApi
import com.ethernom.helloworld.dialog.NotifyRangCallback
import com.ethernom.helloworld.dialog.NotifyRangDialog


class ForegroundNotifyRangActivity : Activity(), NotifyRangCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotifyRangDialog(this, this).show()
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onButtonClicked() {
        /*BeaconReceiver.stopSound()
        MyApplication.saveLogWithCurrentDate("User clicked ringing notification")
        finish()*/
        val intent = Intent(this, SplashScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finishAffinity()
    }
}
