package com.ethernom.helloworld.screens

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity.BOTTOM
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.ethernom.helloworld.R
import kotlinx.android.synthetic.main.dialog_device_delete.view.*

class DeleteDeviceBottomDialog(context: Context?, var itemDeleteCallback: ItemDeleteCallback) : Dialog(context!!) {

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_device_delete, null)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(view)
        setCancelable(true)
        if (window != null) {
            window?.setDimAmount(0.5f)
            window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setGravity(BOTTOM)
            window?.attributes!!.windowAnimations = R.style.DialogAnimationSlideUpDown
        }

        view.view_delete.setOnClickListener {
            itemDeleteCallback.onItemDeleteClicked()
            dismiss()
        }
    }
}

interface ItemDeleteCallback{
    fun onItemDeleteClicked()
}