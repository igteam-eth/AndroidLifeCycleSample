package com.ethernom.helloworld.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import com.ethernom.helloworld.R
import com.ethernom.helloworld.application.SettingSharePreference
import com.ethernom.helloworld.application.TrackerSharePreference
import kotlinx.android.synthetic.main.activity_confirm_pin.*
import kotlinx.android.synthetic.main.toolbar_default_backpress.*

class ConfirmPinActivity : AppCompatActivity() {

    private var digitCount = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_pin)
        init()
    }
    @SuppressLint("SetTextI18n")
    private fun init(){
        tvToolbarDefaultBackPressTitle.text = resources.getString(R.string.EthernomTitle)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val pinToConfirm = intent.getStringExtra("pin")
        Log.i("pin", pinToConfirm!!)
        digitCount = SettingSharePreference.getConstant(this).pinLength
        pin_view_confirm.itemCount = digitCount
        pin_view_confirm.setAnimationEnable(true)
        pin_view_confirm.requestFocus()
        pin_view_confirm.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (pinToConfirm == s.toString()){
                    val returnIntent = Intent()
                    returnIntent.putExtra("pinVerified", true)
                    setResult(Activity.RESULT_OK, returnIntent)
                    onBackPressed()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        text_authentication_message.text = "Please enter the $digitCount digit PIN code that appears on your device screen."

        buttonBack.setOnClickListener {
            onBackPressed()
        }
    }
}
