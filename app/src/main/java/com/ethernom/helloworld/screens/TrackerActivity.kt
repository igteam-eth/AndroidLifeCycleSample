package com.ethernom.helloworld.screens

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.ethernom.helloworld.R
import com.ethernom.helloworld.adapter.RegisteredDeviceAdapter
import com.ethernom.helloworld.model.BleClient
import kotlinx.android.synthetic.main.activity_tracker.*
import kotlinx.android.synthetic.main.toolbar_default.*
import android.view.animation.AlphaAnimation

class TrackerActivity : AppCompatActivity(), RegisteredDeviceAdapter.OnItemCallback, ItemDeleteCallback {

    private var registeredDeviceList: ArrayList<BleClient> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracker)
        init()
    }

    private fun init(){
        button_add.setOnClickListener {
            val animation = AlphaAnimation(1f, 0.8f)
            it.startAnimation(animation)
            startActivity(Intent(this, DiscoverDeviceActivity::class.java))
        }

        button_setting.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        rv_registered_device.layoutManager = LinearLayoutManager(this)
        rv_registered_device.adapter = RegisteredDeviceAdapter(registeredDeviceList, this)
    }
    override fun ItemClickListener(position: Int) {
        DeleteDeviceBottomDialog(this, this ).show()
    }
    override fun onItemDeleteClicked() {
        Log.i("Item", "Deleted")
    }
}
