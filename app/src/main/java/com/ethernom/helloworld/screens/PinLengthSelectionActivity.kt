package com.ethernom.helloworld.screens

import android.os.Bundle
import android.view.View
import com.ethernom.helloworld.R
import kotlinx.android.synthetic.main.activity_pin_lenth_selection.*
import android.app.Activity
import android.content.Intent
import com.ethernom.helloworld.application.SettingSharePreference
import kotlinx.android.synthetic.main.activity_base.*


class PinLengthSelectionActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_lenth_selection)

        setPinLength(SettingSharePreference.getConstant(this).pinLength)

        view_two_digits.setOnClickListener {
            setPinLength(2)
            setIntent(2)

        }
        view_four_digits.setOnClickListener {
            setPinLength(4)
            setIntent(4)
        }
        view_six_digits.setOnClickListener {
            setPinLength(6)
            setIntent(6)
        }
        text_back.setOnClickListener {
            onBackPressed()
        }
        hideAllToolbar()

    }

    override fun onResume() {
        super.onResume()
        view_turn_on_location.visibility = View.GONE
        view_turn_on_bluetooth.visibility = View.GONE
    }
    private fun setPinLength(length: Int){
        when(length){
            2 -> {
                image_check_two_digits.visibility = View.VISIBLE
                image_check_four_digits.visibility = View.GONE
                image_check_six_digits.visibility = View.GONE
            }
            4 -> {
                image_check_two_digits.visibility = View.GONE
                image_check_four_digits.visibility = View.VISIBLE
                image_check_six_digits.visibility = View.GONE
            }
            6 -> {
                image_check_two_digits.visibility = View.GONE
                image_check_four_digits.visibility = View.GONE
                image_check_six_digits.visibility = View.VISIBLE
            }
        }
        SettingSharePreference.getConstant(this).pinLength = length
    }
    private fun setIntent(length: Int){
        val returnIntent = Intent()
        returnIntent.putExtra("length", length)
        setResult(Activity.RESULT_OK, returnIntent)
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.activity_hold, R.anim.activity_slide_down)
    }
}
