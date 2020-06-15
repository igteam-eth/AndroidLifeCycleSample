package com.ethernom.helloworld.util;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.screens.SplashScreenActivity;

public class Utils {


    public static int REQUEST_BLUETOOTH_STATE = 200;
    public static Byte BEACON_DELAY_PERIOD = 12;
    public static String CHANNEL_RANG = "CHANNEL_RANG_ID";
    public static String CHANNEL_BLE_OFF = "CHANNEL_BLE_OFF_ID";
    public static String CHANNEL_LOCATION_OFF = "CHANNEL_LOCATION_OFF_ID";


    public static void preventDoubleClick(final View view){
        view.setEnabled(false);
        view.postDelayed(() -> view.setEnabled(true), 500);
    }


    public static boolean haveNetworkConnection(Context context) {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = new NetworkInfo[0];
        if (cm != null) {
            netInfo = cm.getAllNetworkInfo();
        }
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
    /* For Construct Header */
    public static byte[] makeTransportHeader(byte srcport, byte destprt, byte control, byte interfaces, int payloadLength, byte protocol) {
        byte[] packet = new byte[8];
        packet[0] = srcport;
        packet[1] = destprt;
        packet[2] = control;
        packet[3] = interfaces;
        // length bytes, length is 2 bytes
        int Value0 = payloadLength & 0x00ff;
        int Value1 = payloadLength >> 8;
        packet[4] = (byte) Value0;
        packet[5] = (byte) Value1;
        packet[6] = (byte) 0;
        packet[7] = (byte) 0;
        int xorValue = packet[0];
        // xor the packet header for checksum
        for (int j = 1; j != 7; j++) {
            int c = packet[j];
            xorValue = xorValue ^ c;
        }
        packet[7] = (byte) xorValue;
        return packet;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void removeNotificationByID(Context context, String chanelID){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.deleteNotificationChannel(chanelID);
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode;

        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }
    public static boolean isBluetoothEnable(){
        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bAdapter != null) {
            return bAdapter.isEnabled();
        }else return false;
    }
    public static void initBLE_Location(Context context){
        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bAdapter != null) {
            TrackerSharePreference.getConstant(context).setBLEStatus(bAdapter.isEnabled());
        }
        TrackerSharePreference.getConstant(context).setLocationStatus(Utils.isLocationEnabled(context));

        boolean isLocation = TrackerSharePreference.getConstant(context).isLocationStatus();
        boolean isBLEStatus = TrackerSharePreference.getConstant(context).isBLEStatus();

        if (!TrackerSharePreference.getConstant(context).isCardRegistered()){
            //when start up if our location or ble is off when go to 1000 state "CARD_DISCOVERY_BLE_LOCATION_OFF"
            if (!isLocation | !isBLEStatus){
                TrackerSharePreference.getConstant(context).setCurrentState(StateMachine.CARD_DISCOVERY_BLE_LOCATION_OFF.getValue());
            }
        }

    }

    /*
    * open fun checkBluetoothSate(bluetoothState: ((state: (Boolean)) -> Unit)) {

        mBluetoothState = bluetoothState
        if (TrackerSharePreference.getConstant(this).isBLEStatus) {
            mBluetoothState(true)
        } else {
            val mIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(mIntent, Utils.REQUEST_BLUETOOTH_STATE)
        }
    }

    open fun checkLocationState(locationState: ((state: (Boolean)) -> Unit)){
        Log.d("MyApplication check", "${TrackerSharePreference.getConstant(this).isLocationStatus}")
        mLocationState = locationState
        if (TrackerSharePreference.getConstant(this).isLocationStatus){
            mLocationState(true)
        }else{
            AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Please enable location for registration the card.")
                .setCancelable(false)
                .setPositiveButton(
                    android.R.string.yes
                ) { dialog, _ ->
                    dialog.dismiss()
                    val mIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(mIntent)
                    isRequestEnableLocation = true

                }
                .show()
        }
    }*/

    /*
    * override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Utils.REQUEST_BLUETOOTH_STATE) {  // Match the request code
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Bluetooth turn on")
                mBluetoothState(true)
            } else {   // RESULT_CANCELED
                Log.d(TAG, "Bluetooth deny turning on")
                mBluetoothState(false)
            }
        }
        if (requestCode == Utils.REQUEST_LOCATION_STATE) {  // Match the request code
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "GPS turn on")
                mLocationState(true)
            } else {   // RESULT_CANCELED
                Log.d(TAG, "GPS deny turning on")
                mLocationState(false)
            }
        }
    }*/
}
