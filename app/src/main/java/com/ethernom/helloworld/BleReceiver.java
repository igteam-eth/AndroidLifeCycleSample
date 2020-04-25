package com.ethernom.helloworld;

/*
The code here manages the scanner library. In particular, it enables or disables the scanning.
It also listens for incoming PendingIntents when a matching BLE device is found.

It also listens for events associated with enabling and disabling the device'ss Bluetooth.
*/

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.ethernom.helloworld.MyApplication.showSilentNotification;

@SuppressLint("MissingPermission")
@RequiresApi(api = Build.VERSION_CODES.O)
public class BleReceiver extends BroadcastReceiver {

    public static MediaPlayer mp = null;

    private static final String TAG = "APP_BleReceiver";
    public static final String ACTION_SCANNER_FOUND_DEVICE = "com.ethernom.helloworld.ACTION_SCANNER_FOUND_DEVICE";

    public static PendingIntent mPendingIntent;

    private static Context mContext;

    /**
     * Constructor
     */
    public BleReceiver() {
        Log.v(TAG, "in Constructor");
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        MyApplication.appendLog(MyApplication.getCurrentDate()+ " : BleReceiver OnReceive callback\n");
        mContext = context;

        Log.v(TAG, "onReceive() ");

        if (intent.getAction() == null) {
            Log.e(TAG, "ERROR: action is null");
        } else {
            // Look whether we find our device
            if (ACTION_SCANNER_FOUND_DEVICE.equals(intent.getAction())) {
                Bundle extras = intent.getExtras();

                if (extras != null) {
                    Object obj = extras.get(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT);
                    if (obj instanceof ArrayList) {
                        ArrayList<ScanResult> scanResults = (ArrayList<ScanResult>) obj;
                        Log.v(TAG, "There are " + scanResults.size() + " results");

                        for (ScanResult result : scanResults) {
                            if (result.getScanRecord() == null) {
                                Log.d(TAG, "getScanRecord is null");
                                continue;
                            }
                            showSilentNotification(context);
                            playSound(context);
                        }

                    } else {
                        // Received something, but not a list of scan results...
                        Log.d(TAG, "   no ArrayList but " + obj);
                    }
                } else {
                    Log.d(TAG, "no extras");
                }
            }
        }
    }

    private static ScanFilter getScanFilter() {

        ScanFilter.Builder builder = new ScanFilter.Builder();
        ByteBuffer manData = ByteBuffer.allocate(23);
        manData.put(0, (byte) 0x02);
        manData.put(1, (byte) 0x15);
        manData.put(2, (byte) 0x19);
        manData.put(3, (byte) 0x49);
        manData.put(4, (byte) 0x00);
        manData.put(5, (byte) 0x13);
        manData.put(6, (byte) 0x55);
        manData.put(7, (byte) 0x37);
        manData.put(8, (byte) 0x4f);
        manData.put(9, (byte) 0x5e);
        manData.put(10, (byte) 0x99);
        manData.put(11, (byte) 0xca);
        manData.put(12, (byte) 0x29);
        manData.put(13, (byte) 0x0f);
        manData.put(14, (byte) 0x4f);
        manData.put(15, (byte) 0xbf);
        manData.put(16, (byte) 0xf1);
        manData.put(17, (byte) 0x42);
        manData.put(18, (byte) 0x3F); //major
        manData.put(19, (byte) 0x58); //major
        manData.put(20, (byte) 0x91); //minor
        manData.put(21, (byte) 0x84); //minor
        manData.put(22, (byte) 0xc3);
        builder.setManufacturerData(0x004c, manData.array());
        return builder.build();
    }
        /* Doeurn
        manData.put(18, (byte) 0x16); //major
        manData.put(19, (byte) 0x9C); //major
        manData.put(20, (byte) 0x95); //minor
        manData.put(21, (byte) 0x2C); //minor */

        /* Sopheak
        manData.put(18, (byte) 0x23); //major
        manData.put(19, (byte) 0xD5); //major
        manData.put(20, (byte) 0x10); //minor
        manData.put(21, (byte) 0x14); //minor */

        /* Bunheng1
        manData.put(18, (byte) 0x61); //major
        manData.put(19, (byte) 0x7F); //major
        manData.put(20, (byte) 0xA9); //minor
        manData.put(21, (byte) 0x8D); //minor */

        /*Zophak
        manData.put(18, (byte) 0x81); //major
        manData.put(19, (byte) 0xcb); //major
        manData.put(20, (byte) 0xd8); //minor
        manData.put(21, (byte) 0x89); //minor */

        /*AABB
        manData.put(18, (byte) 0xDF); //major
        manData.put(19, (byte) 0xA0); //major
        manData.put(20, (byte) 0x00); //minor
        manData.put(21, (byte) 0x0C); //minor*/

        /*thoun
        manData.put(18, (byte) 0x3F); //major
        manData.put(19, (byte) 0x58); //major
        manData.put(20, (byte) 0x91); //minor
        manData.put(21, (byte) 0x84); //minor*/

    /**
     * Used internally only
     */
    public static void startScan(Context context) {

        mContext = context;

        Log.d(TAG, "Start scan");

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setReportDelay(0)
                .setLegacy(true)
                .build();

        List<ScanFilter> filters = new ArrayList<>();
        filters.clear();
        Log.d(TAG, "Starting to scan for: ");
        ScanFilter filter = getScanFilter();
        filters.add(filter);

        Intent intent = new Intent(mContext, BleReceiver.class); // explicit intent
        intent.setAction(BleReceiver.ACTION_SCANNER_FOUND_DEVICE);
        int id = 0;

        mPendingIntent = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_ONE_SHOT);

        // Now start the scanner
        try {
            Log.d(TAG, "Asking library to start scanning.");
            BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, mPendingIntent);
            MyApplication.appendLog(MyApplication.getCurrentDate()+ " : Start scanning \n");

        } catch (Exception e) {
            Log.e(TAG, "ERROR in startScan() " + e.getMessage());
        }
    }

    public static void stopScan() {

        Log.d(TAG, "Stop scan");

        if (mContext == null) {
            Log.d(TAG, "Can't stop: mContext null");
            MyApplication.appendLog(MyApplication.getCurrentDate()+ " : Can't stop: mContext null\n");

            return;
        }

        if (mPendingIntent == null) {
            Log.d(TAG, "Can't stop: mPendingIntent null");
            MyApplication.appendLog(MyApplication.getCurrentDate()+ " : Can't stop: mPendingIntent null\n");

            return;
        }

        try {
            Log.d(TAG, "Asking library to stop scanning.");
            BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothAdapter.getBluetoothLeScanner().stopScan(mPendingIntent);
            MyApplication.appendLog(MyApplication.getCurrentDate()+ " : Stop scanning \n");

        } catch (Exception e) {
            Log.e(TAG, "ERROR in stopScan() " + e.getMessage());
            MyApplication.appendLog(MyApplication.getCurrentDate()+ " : ERROR in stopScan() \n");

        }
    }

    //call to play  sound
    public static void playSound(Context context) {
        if (mp != null) {
            mp.stop();
        }

        mp = MediaPlayer.create(context, R.raw.ringingsound_short);
        //mp.setLooping(true);
        mp.start();

    }

    // call to stop sound
    public static void stopSound() {
        if (mp != null) {
            mp.stop();
        }
    }


}
