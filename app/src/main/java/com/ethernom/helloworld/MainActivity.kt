package com.ethernom.helloworld

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess

@RequiresApi(Build.VERSION_CODES.O)
open class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MyMP", "" + BleReceiver.mp)
       BleReceiver.stop()


        val serviceIntent = Intent(this@MainActivity, onClearFromRecentService::class.java)
        startService(serviceIntent)

        Log.e("LifeCycle", "OnCreate Called")
        var clickCount = 0
        button_update_content.setOnClickListener {
            clickCount++
            text_result.text = "${text_result.text} \nhello world $clickCount"

        }
        // navigate to transparent screen to track only onPause() and onResume()
        button_go_to_second_screen.setOnClickListener {
            startActivity(Intent(this, SecondActivity::class.java))
        }
        //toggle enable timer
        switch_alarm.isChecked = getSharedPreferences("ALARM", Context.MODE_PRIVATE).getBoolean("alarm", false)
        switch_alarm.setOnCheckedChangeListener { _, isChecked ->
            getSharedPreferences("ALARM", Context.MODE_PRIVATE).edit().putBoolean("alarm", isChecked).apply()
        }
        init()
    }

    private fun init(){
        // If not already started, start .
        // This will read preferences from SharedPreferences and initialise BleReceiver so it scans properly
        startHTSService("Activity created")
        // This receives messages for purposes of painting the screen etc
    }

    /**
     * Called when screen is rotated!
     */
    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart()")

        /* Check access to BLE is granted */
        verifyBluetooth()
        requestBluetoothPermission()

        // If the service has not been started before, the following lines will not start it.
        // However, if it's running, the Activity will bind to it and notified via mServiceConnection.

        val service = Intent(this, HTSService::class.java)
        // We pass 0 as a flag so the service will not be created if not exists.
        Log.d(TAG, "Binding service")
        bindService(service, mServiceConnection, 0)
    }

    /**
     * Starts the HTSService
     * Called from onCreate() in case it has not been auto-started already.
     * Can also be called from StartupReceiover following a reboot.
     */
    private fun startHTSService(reason: String) {
        val serviceIntent = Intent(applicationContext, HTSService::class.java)
        serviceIntent.putExtra(HTSService.STARTUP_SOURCE, reason)
        applicationContext.startService(serviceIntent)
        bindService(serviceIntent, mServiceConnection, 0)
    }

    /*********************************************************************************************
     *
     * BLUETOOTH PERMISSIONS
     *
     *********************************************************************************************/

    /**
     * Request permission to use Bluetooth:
     * pops up a dialog explaining things then gets the OS to request permission
     * Note - if this is not done the the scanner will fail, often silently!
     */
    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            Log.d(TAG, "Checking Bluetooth permissions")
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "  Permission is not granted")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Permission Required for BLE Device Detection")
                builder.setMessage("Bluetooth operation requires 'location' access.\nPlease grant this so the app can detect BLE devices")
                //builder.setIcon(R.drawable.cross);
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    // User replies then there is a call to onRequestPermissionsResult() below
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                        PERMISSION_REQUEST_COARSE_LOCATION
                    )
                }
                builder.show()
            } else {
                Log.d(TAG, "  Permission is granted")
            }
        }
    }

    /**
     * Check BLE is enabled, and pop up a dialog if not
     */
    private fun verifyBluetooth() {

        try {
            if (!checkAvailability()) {
                Log.d(TAG, "BLE not available.")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Bluetooth is Not Enabled")
                builder.setMessage("Bluetooth must be on for this app to work.\nPlease allow the app to turn on Bluetooth when asked, or the app will be terminated.")
                //builder.setIcon(R.drawable.cross);
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    // Ask for permission to turn on BLE
                    askToTurnOnBLE()
                }
                builder.show()
            } else {
                Log.d(TAG, "BLE is available.")
            }
        } catch (e: RuntimeException) {
            Log.d(TAG, "BLE not supported.")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Bluetooth Low Energy not available")
            builder.setMessage("Sorry, this device does not support Bluetooth Low Energy.")
            //builder.setIcon(R.drawable.cross);
            builder.setPositiveButton(android.R.string.ok, null)
            builder.setOnDismissListener {
                // kill the app
                finish()
                exitProcess(0)
            }
            builder.show()
        }

    }
    /**
     * Asks user's permission to turn on the Bluetooth
     */
    private fun askToTurnOnBLE() {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT)
    }


    /**
     * Check if Bluetooth LE is supported by this Android device, and if so, make sure it is enabled.
     *
     * @return false if it is supported and not enabled
     * @throws RuntimeException if Bluetooth LE is not supported.  (Note: The Android emulator will do this)
     */
    @Throws(RuntimeException::class)
    fun checkAvailability(): Boolean {
        if (!isBleAvailable()) {
            throw RuntimeException("Bluetooth LE not supported by this device")
        }
        return (this.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled
    }
    /**
     * Checks if the device supports BLE
     * @return true if it does
     */
    private fun isBleAvailable(): Boolean {
        var available = false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.w(TAG, "Bluetooth LE not supported prior to API 18.")
        } else if (!this.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "This device does not support bluetooth LE.")
        } else {
            available = true
        }
        return available
    }
    /*********************************************************************************************
     *
     * Service Connection: allows bi-directional communication with the service.
     *
     */

    private val mServiceConnection = object : ServiceConnection {
        // Interface for monitoring the state of an application service

        override// We get here when the StartService service has connected to this activity.
        fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val b = binder as HTSService.StartServiceBinder
            val mService = b.service

            // Now
            val reason = mService.startupReason
            val msg = "Activity connected to the service. Reason: '$reason'"
            Log.d(TAG, msg)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            // Note: this method is called only when the service is killed by the system,
            // not when it stops itself or is stopped by the activity.
            // It will be called only when there is critically low memory, in practice never
            // when the activity is in foreground.
            val msg = "Activity disconnected from the service"
            Log.d(TAG, msg)

        }
    }



    override fun onResume() {
        super.onResume()
        BleReceiver.startScanning(this)
    }

    override fun onPause() {
        super.onPause()
        Log.e("LifeCycle", "OnPause Called")
    }

    override fun onRestart() {
        super.onRestart()
        Log.e("LifeCycle", "OnRestart Called")
    }

    override fun onStop() {
        super.onStop()
        Log.e("LifeCycle", "OnStop Called")
    }

    companion object{
        val TAG = MainActivity::class.java.simpleName
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
        const val REQUEST_ENABLE_BT = 2
    }
}
