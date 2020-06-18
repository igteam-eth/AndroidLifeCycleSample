package com.ethernom.helloworld.screens

import android.os.Build
import android.os.Bundle
import com.ethernom.helloworld.R
import kotlinx.android.synthetic.main.activity_location_permission.*
import android.content.Intent
import android.net.Uri
import com.ethernom.helloworld.util.Utils
import kotlinx.android.synthetic.main.activity_base.*


class LocationPermissionActivity : BaseActivity() {

    private val permissionDescription =
        "For Ethernom BLE Tracker to locate your device, Location access needs to be set to  \"Allow all the time\". Go to Location Setting to turn on access.  \n\n1. Press on Button SET LOCATION.  \n\n2. Tab Permissions.\n"
    private val v9 = "3. Select enable for allow your location at any time."
    private val v10 =
        "3. Choose an option 'Allow all the time' The app can use your location at any time."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_permission)

        showDefaultToolbar()
        tvToolbarDefaultBackPressTitle.text = resources.getString(R.string.location_permission)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            text_permission_description.text = "$permissionDescription \n$v10"
            image_permission.setImageDrawable(getDrawable(R.drawable.location_setting_android_v10))
        } else {
            text_permission_description.text = "$permissionDescription \n$v9"
            image_permission.setImageDrawable(getDrawable(R.drawable.location_setting_android_v9))
        }

        button_set_location_always.setOnClickListener {
            Utils.preventDoubleClick(it)
            showInstalledAppDetails()
        }
        showBackButtonToolbar()
    }

    private fun showInstalledAppDetails() {
        //redirect user to app Settings
        val i = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        i.addCategory(Intent.CATEGORY_DEFAULT)
        i.data = Uri.parse("package:$packageName")
        startActivity(i)
    }

}
