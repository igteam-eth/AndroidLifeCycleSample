package com.ethernom.helloworld.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import com.ethernom.helloworld.R
import com.ethernom.helloworld.application.SettingSharePreference
import com.ethernom.helloworld.application.TrackerSharePreference
import com.ethernom.helloworld.util.StateMachine
import kotlinx.android.synthetic.main.activity_confirm_pin.*
import kotlinx.android.synthetic.main.toolbar_default_backpress.*




class ConfirmPinActivity : BaseActivity() {

    private var digitCount = 2
    private var enterFailedCount = 0

    @SuppressLint("SetTextI18n", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_pin)
        tvToolbarDefaultBackPressTitle.text = resources.getString(R.string.EthernomTitle)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        val mKeyboardView = findViewById<KeyboardView>(R.id.keyboard)

        TrackerSharePreference.getConstant(this).currentState = StateMachine.VERIFY_PIN.value

        val returnIntent = Intent()
        val pinToConfirm = intent.getStringExtra("pin")
        Log.i("pin", pinToConfirm!!)
        digitCount = SettingSharePreference.getConstant(this).pinLength
        pin_view_confirm.itemCount = digitCount
        pin_view_confirm.setAnimationEnable(true)
        pin_view_confirm.clearFocus()
        pin_view_confirm.setRawInputType(InputType.TYPE_CLASS_TEXT)
        pin_view_confirm.setTextIsSelectable(true)
        val ic = pin_view_confirm.onCreateInputConnection(EditorInfo())
        mKeyboardView.setInputConnection(ic)

        pin_view_confirm.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                if (s!!.isNotEmpty()){
                    if (pinToConfirm.length == s.toString().length && pinToConfirm == s.toString()){
                        // Entering PIN correct
                        returnIntent.putExtra("pinVerified", true)
                        setResult(Activity.RESULT_OK, returnIntent)
                        onBackPressed()
                    }else{
                        // Entering PIN Incorrect
                        if(pinToConfirm.length == s.toString().length){
                            // Entering PIN Incorrect x3
                            if (enterFailedCount >= 2){
                                setResult(Activity.RESULT_CANCELED)
                                onBackPressed()
                            }
                            text_authentication_message.text =  Html.fromHtml("<font color=#F44336>Error! Wrong PIN.</font><font> Please enter the $digitCount digit PIN code that appears on your device screen.</font>")
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

    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)

    }
}

