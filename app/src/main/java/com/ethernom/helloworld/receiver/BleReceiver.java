package com.ethernom.helloworld.receiver;

/*
The code here manages the scanner library. In particular, it enables or disables the scanning.
It also listens for incoming PendingIntents when a matching BLE device is found.

It also listens for events associated with enabling and disabling the device'ss Bluetooth.
 */

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ethernom.helloworld.application.HTSScannerApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.model.BleClient;
import com.ethernom.helloworld.screens.MainActivity;
import com.ethernom.helloworld.R;
import com.ethernom.helloworld.screens.TrackerActivity;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.content.Context.NOTIFICATION_SERVICE;


@SuppressLint("MissingPermission")
@RequiresApi(api = Build.VERSION_CODES.O)
public class BleReceiver extends BroadcastReceiver {

    public static MediaPlayer mp = null;
    private static final String CHANNEL_ID = "ring_channel";

    private static final String TAG = "APP_BleReceiver";

    public static final String ACTION_SCANNER_FOUND_DEVICE = "com.ethernom.helloworld.ACTION_SCANNER_FOUND_DEVICE";

    public static final String DEVICE_FOUND = "com.ethernom.helloworld.DEVICE_FOUND";
    public static final String BLE_DEVICE = "com.ethernom.helloworld.BLE_DEVICE";
    public static final String BLE_DEVICE_NAME = "com.ethernom.helloworld.BLE_DEVICE_NAME";
    public static final String BLE_DEVICE_RSSI = "com.ethernom.helloworld.BLE_DEVICE_RSSI";
    public static final String BLE_SERVICE_UUID = "com.ethernom.helloworld.BLE_SERVICE_UUID";

    public static final String SCANNING_STATE = "com.ethernom.helloworld.SCANNING_STATE";
    public static final String EXTRA_SCANNING_STATE = "com.ethernom.helloworld.EXTRA_SCANNING_STATE";

    private static PendingIntent mPendingIntent;

    private static Boolean mScanning = false;
    private static Boolean mShouldScan = false;

    private static Context mContext;

    // Set true when we are processing a device and false when this is finished.
    private static Boolean mProcessingDevice = false;


    /**
     * Constructor
     */
    public BleReceiver() {
        Log.v(TAG, "in Constructor");
    }
    private boolean status = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Log.v(TAG, "  ");
        Log.v(TAG, "onReceive() ");

        if (intent.getAction() == null) {
            Log.e(TAG, "ERROR: action is null");
            return;
        } else {
            Log.v(TAG, "DEBUG: action is " + intent.getAction());
        }

        //NOTE: actions must be registered in AndroidManifest.xml
        switch (intent.getAction()) {

            // Look whether we find our device
            case ACTION_SCANNER_FOUND_DEVICE: {
                Bundle extras = intent.getExtras();

                if (extras != null) {
                    Object o = extras.get(BluetoothLeScanner.EXTRA_LIST_SCAN_RESULT);
                    if (o instanceof ArrayList) {
                        ArrayList<ScanResult> scanResults = (ArrayList<ScanResult>) o;
                        Log.v(TAG, "There are " + scanResults.size() + " results");

                        if (!mShouldScan) {
                            Log.d(TAG, "*** Unexpected device found: not scanning");
                        }

                        for (ScanResult result : scanResults) {

                            if (result.getScanRecord() == null) {
                                Log.d(TAG, "getScanRecord is null");
                                continue;
                            }

                            BluetoothDevice device = result.getDevice();
                            ScanRecord scanRecord = result.getScanRecord();
                            String scanName = scanRecord.getDeviceName();
                            String deviceName = device.getName();
                            int rssi = result.getRssi();
                            stopScan();
                            //mHeader.setText("Single device found: " + device.getName() + " RSSI: " + result.getRssi() + "dBm");
                            Log.i(TAG, "Found: " + device.getAddress()
                                    + " scan name: " + scanName
                                    + " device name: " + deviceName
                                    + " RSSI: " + rssi + "dBm");

                            // Sometimes the same device is found again, even though we have stopped scanning as soon as it was found.
                            // Discard these events.
                            if (mProcessingDevice) {
                                Log.d(TAG, "Ignoring " + scanName + " (already processing).");
                                return;
                            }


                            if (status){
                                silentNotification(context);
                                play(context);
                                status = false;
                            }else{
                                final Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        silentNotification(context);
                                        play(context);
                                    }
                                }, 10000);
                            }
                        }

                    } else {
                        // Received something, but not a list of scan results...
                        Log.d(TAG, "   no ArrayList but " + o);
                    }
                } else {
                    Log.d(TAG, "no extras");
                }

                break;
            }

            // Look at BLE adapter state
            case BluetoothAdapter.ACTION_STATE_CHANGED: {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "BLE off");
                        // Need to take some action or app will fail...
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "BLE turning off");
                        stopScan();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "BLE on");
                        startScan();    // restart scanning (provided the activity wants this to happen)
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "BLE turning on");
                        break;
                }
                break;
            }

            default:
                // should not happen
                Log.d(TAG, "Received unexpected action " + intent.getAction());
        }

    }

    /**
     * After reboot we need to get saved state from SharedPreferences
     * This is called when HTSService starts after reboot.
     */
    public static void initialiseAfterReboot() {

        Log.d(TAG, "Initialising state saved from SharedPreferences");
        HTSScannerApplication.setupSharedPreferences();

        mContext = HTSScannerApplication.getAppContext();

        mShouldScan = HTSScannerApplication.getScanning();

        if (mShouldScan) {
            Log.d(TAG, "Looks like we were scanning before reboot, so we will start again");
            startScan();
        } else {
            Log.d(TAG, "Looks like we were not scanning before reboot.");
        }
    }

    /**
     * Called externally only
     *
     * @param context
     * @param //uuid  Calling code defines the 128-bit UUID
     */
    public static void startScanning(Context context) {
        mContext = context;
        //mUuid = uuid;
        mShouldScan = true;

        // Save these in SharedPreferences, so they are available after reboot
        //HTSScannerApplication.saveUuid(uuid);
        //HTSScannerApplication.saveScanning(true);

        startScan();
    }

    /*
    manData.put(18, (byte)0xB8);
    manData.put(19, (byte)0x56);
    manData.put(20, (byte)0x6E);
    manData.put(21, (byte)0x77);*/

    private static ScanFilter getScanFilter() {

        BleClient bleClient = TrackerSharePreference.getConstant(mContext).getEthernomCard();
        byte[] majors;
        byte[] minors;
        if (bleClient != null){
            String major = bleClient.getMajor();
            String minor = bleClient.getMinor();
            majors = hexStringToByteArray(major);
            minors = hexStringToByteArray(minor);
        }else{
            majors = hexStringToByteArray("0000");
            minors = hexStringToByteArray("0000");
        }



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
        manData.put(18,  majors[0]); //major
        manData.put(19, majors[1]); //major
        manData.put(20, minors[0]); //minor
        manData.put(21, minors[1]); //minor
        manData.put(22, (byte) 0xc3);
        builder.setManufacturerData(0x004c, manData.array()); //Is this id correct?
        return builder.build();
    }

    /**
     * Used internally only
     */

    private static void startScan() {

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setReportDelay(0)
                .setLegacy(true)
                .build();


        List<ScanFilter> filters = new ArrayList<>();
        filters.clear();
        Log.d(TAG, "Starting to scan");
        ScanFilter filter = getScanFilter();
        filters.add(filter);

        Intent intent = new Intent(mContext, BleReceiver.class); // explicit intent
        intent.setAction(BleReceiver.ACTION_SCANNER_FOUND_DEVICE);
        int id = 0;     // "Private request code for the sender"

        mPendingIntent = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Now start the scanner

        try {
            Log.d(TAG, "Asking library to start scanning.");
            //BluetoothLeScannerCompat.getScanner().startScan(filters, settings, mContext, mPendingIntent);
            BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, mPendingIntent);
            mScanning = true;
            notifyScanState();
        } catch (Exception e) {
            Log.e(TAG, "ERROR in startScan() " + e.getMessage());
        }
    }

    /**
     * Called externally only
     */
    public static void stopScanning() {
        mProcessingDevice = false;
        mShouldScan = false;
        stopScan();
    }

    /**
     * Used internally only
     */

    private static void stopScan() {
        Log.d(TAG, "Stop scanning");

        if (mContext == null) {
            Log.d(TAG, "Can't stop: mContext null");
            return;
        }

        if (mPendingIntent == null) {
            Log.d(TAG, "Can't stop: mPendingIntent null");
            return;
        }

        try {
            Log.d(TAG, "Asking library to stop scanning.");
            BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothAdapter.getBluetoothLeScanner().stopScan(mPendingIntent);
            mScanning = false;

            notifyScanState();
        } catch (Exception e) {
            Log.e(TAG, "ERROR in stopScan() " + e.getMessage());
        }
    }
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


    /**
     * Inform the MainActivity what we are doing in terms of scanning
     */
    private static void notifyScanState() {
        Intent intent = new Intent(SCANNING_STATE);
        intent.putExtra(EXTRA_SCANNING_STATE, mScanning);
        LocalBroadcastManager.getInstance(HTSScannerApplication.getAppContext()).sendBroadcast(intent);
    }


    /**
     * Inhibits processing of spurious ACTION_SCANNER_FOUND_DEVICE messages once we have decided to connect to a HTS device.
     * Called by HTSService twice:
     * - when it gets a DEVICE_FOUND message from BleReceiver
     * - when disconnected from the HTS device.
     *
     * @param state
     */
    public static void setProcessingDevice(Boolean state) {
        mProcessingDevice = state;

        // if scanning has been temporarily suspended while we process one device, restart scanning
        if (state == false) {
            startScan();
        }
    }

    //call to play  sound
    public static void play(Context context) {
        if (mp != null) {
            mp.setLooping(false);
            mp.stop();
        }
        mp = MediaPlayer.create(context, R.raw.ovending);
        mp.setLooping(true);
        mp.start();

    }

    // call to stop sound
    public static void stop() {
        if (mp != null) {
            mp.stop();
        }
    }

    //show notification when beacon come up
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void silentNotification(Context context) {

        final NotificationManager manager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        final NotificationChannel channel =
                new NotificationChannel(CHANNEL_ID, "Ethernom", IMPORTANCE_HIGH);
        assert manager != null;
        manager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setColor(ContextCompat.getColor(context, R.color.colorWhite));
        builder.setContentTitle("Ethernom Tracker");
        builder.setContentText("You rang your phone from your device");
        builder.setAutoCancel(false);
        Intent intent = new Intent(context, TrackerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        manager.notify(0, builder.build());
    }
}
