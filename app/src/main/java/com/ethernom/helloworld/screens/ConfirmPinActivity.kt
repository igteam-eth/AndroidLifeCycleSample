package com.ethernom.helloworld.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import com.ethernom.helloworld.R
import com.ethernom.helloworld.application.SettingSharePreference
import com.ethernom.helloworld.application.TrackerSharePreference
import kotlinx.android.synthetic.main.activity_confirm_pin.*
import kotlinx.android.synthetic.main.toolbar_default_backpress.*

class ConfirmPinActivity : BaseActivity() {

    private var digitCount = 2
    private val messageError = "<font color=#F44336>Error! Wrong PIN.</font><font> Please enter the $digitCount digit PIN code that appears on your device screen.</font>"
    private var enterFailedCount = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_pin)
        tvToolbarDefaultBackPressTitle.text = resources.getString(R.string.EthernomTitle)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        val returnIntent = Intent()
        val pinToConfirm = intent.getStringExtra("pin")
        Log.i("pin", pinToConfirm!!)
        digitCount = SettingSharePreference.getConstant(this).pinLength
        pin_view_confirm.itemCount = digitCount
        pin_view_confirm.setAnimationEnable(true)
        pin_view_confirm.requestFocus()
        pin_view_confirm.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty()){
                    if (pinToConfirm.length == s.toString().length && pinToConfirm == s.toString()){
                        returnIntent.putExtra("pinVerified", true)
                        setResult(Activity.RESULT_OK, returnIntent)
                        onBackPressed()
                    }else{
                        if(pinToConfirm.length == s.toString().length){
                            if (enterFailedCount >= 2){
                                setResult(Activity.RESULT_CANCELED)
                                onBackPressed()
                            }
                            text_authentication_message.text =  Html.fromHtml(messageError)
                            s.clear()
                            enterFailedCount++
                        }


                    }
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
