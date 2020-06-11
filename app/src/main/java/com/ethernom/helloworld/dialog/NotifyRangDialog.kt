package com.ethernom.helloworld.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity.BOTTOM
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import com.ethernom.helloworld.R
import kotlinx.android.synthetic.main.dialog_notify_rang.view.*

class NotifyRangDialog(context: Context?, var notifyRangCallback: NotifyRangCallback) : Dialog(context!!) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_notify_rang, null)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(view)
        setCancelable(false)
        if (window != null) {
            window?.setDimAmount(0.5f)
            window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setGravity(BOTTOM)
            window?.attributes!!.windowAnimations = R.style.DialogAnimationSlideUpDown
        }

        view.button_acknowledge.setOnClickListener {
            val animation = AlphaAnimation(1f, 0.8f)
            it.startAnimation(animation)
            notifyRangCallback.onButtonClicked()
            dismiss()
        }
    }
}

interface NotifyRangCallback{
    fun onButtonClicked()
}