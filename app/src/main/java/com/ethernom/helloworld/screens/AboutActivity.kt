package com.ethernom.helloworld.screens

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ethernom.helloworld.R
import kotlinx.android.synthetic.main.toolbar_default_backpress.*
import android.content.Intent
import android.net.Uri
import android.security.KeyChain
import android.text.method.LinkMovementMethod
import android.util.Log
import com.ethernom.helloworld.BuildConfig
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        init()
    }

    @SuppressLint("SetTextI18n")
    private fun init(){



        text_version.text = "Version: ${BuildConfig.VERSION_NAME}"
        tvToolbarDefaultBackPressTitle.text = resources.getString(R.string.about)
        text_terms_of_service.movementMethod = LinkMovementMethod.getInstance()
        text_terms_of_service.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = Uri.parse("https://ethernom.com/legal/tos.html")
            startActivity(browserIntent)
        }
        buttonBack.setOnClickListener {
            onBackPressed()
        }

        text_privacy_policy.movementMethod = LinkMovementMethod.getInstance()
        text_privacy_policy.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = Uri.parse("https://ethernom.com/legal/privacy_policy.html")
            startActivity(browserIntent)
        }
    }
}
