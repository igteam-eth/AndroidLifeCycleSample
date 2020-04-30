package com.ethernom.helloworld.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import com.ethernom.helloworld.R

class LoadingDialog(context: Context, private val defaultText: String = "") : Dialog(context) {

    private var txtDescription: TextView? = null

    init {
        init()
    }

    @SuppressLint("SetTextI18n")
    fun setLoadingDescription(description: String){
        if (txtDescription != null ){
            if (!TextUtils.isEmpty(defaultText)){
                txtDescription!!.text = "$defaultText : $description"
            }else{
                txtDescription!!.text = description
            }
        }
    }

    private fun init() {

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_loading)
        setCancelable(false)

        if (window != null) {
            window!!.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window!!.setDimAmount(0.5f)
        }

        txtDescription = findViewById(R.id.text_description)


        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


    }



}
