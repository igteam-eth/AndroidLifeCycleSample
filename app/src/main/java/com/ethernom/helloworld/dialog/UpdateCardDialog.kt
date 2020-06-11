package com.ethernom.helloworld.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.ethernom.helloworld.R

class UpdateCardDialog(context: Context, private val callback: Callback) : Dialog(context) {

    init {
        init()
    }

    private fun init() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_update_card)
        setCancelable(false)

        if (window != null) {
            window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window!!.attributes.windowAnimations = R.style.DialogAnimationScaleInOut
            window!!.setDimAmount(0.5f)
        }

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        findViewById<TextView>(R.id.button_disconnect).setOnClickListener {
            dismiss()
            callback.onDisconnectButtonClicked()
        }

        findViewById<TextView>(R.id.button_dont_update).setOnClickListener{
            dismiss()
            callback.onDoNotUpdateButtonClicked()
        }
        findViewById<TextView>(R.id.button_update).setOnClickListener{
            dismiss()
            callback.onUpdateButtonClicked()
        }

    }
    interface Callback{
        fun onDisconnectButtonClicked()
        fun onUpdateButtonClicked()
        fun onDoNotUpdateButtonClicked()
    }


}
