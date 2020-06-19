package com.ethernom.helloworld.screens

import android.content.Intent
import android.os.Bundle
import com.ethernom.helloworld.R
import kotlinx.android.synthetic.main.activity_setting.*
import android.app.Activity
import com.ethernom.helloworld.application.SettingSharePreference
import com.ethernom.helloworld.util.Utils
import kotlinx.android.synthetic.main.activity_base.*

class SettingActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        init()
        showBackButtonToolbar()
    }


    private fun init(){
        tvToolbarDefaultBackPressTitle.text = resources.getString(R.string.settings)
        text_toolbar_title.text = resources.getString(R.string.settings)
        button_tracker.setOnClickListener {
            onBackPressed()
        }
        disPlayLengthDigit(SettingSharePreference.getConstant(this).pinLength)
        view_pin_length.setOnClickListener {
            Utils.preventDoubleClick(it)
            startActivityForResult(Intent(this, PinLengthSelectionActivity::class.java), 101)
            overridePendingTransition(R.anim.activity_slide_up, R.anim.activity_hold)
        }
        view_permission.setOnClickListener {
            Utils.preventDoubleClick(it)
            startActivity(Intent(this, LocationPermissionActivity::class.java))
        }
        view_about_info.setOnClickListener {
            Utils.preventDoubleClick(it)
            startActivity(Intent(this, AboutActivity::class.java))
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                val result = data!!.getIntExtra("length", 2)
                disPlayLengthDigit(result)
            }
        }
    }
    private fun disPlayLengthDigit(length: Int){
        text_digit_count.text = "$length Digits"
    }
}
