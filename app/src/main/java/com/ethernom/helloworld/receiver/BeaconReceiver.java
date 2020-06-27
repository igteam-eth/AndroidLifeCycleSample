package com.ethernom.helloworld.receiver;

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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.R;
import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.model.BleClient;
import com.ethernom.helloworld.util.StateMachine;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.AUDIO_SERVICE;
import static com.ethernom.helloworld.application.MyApplication.showRangNotification;

@SuppressLint("MissingPermission")
@RequiresApi(api = Build.VERSION_CODES.O)
public class BeaconReceiver extends BroadcastReceiver {

    public static MediaPlayer mp = null;

    private static final String TAG = "BeaconReceiver";
    public static final String ACTION_SCANNER_FOUND_DEVICE = "com.ethernom.helloworld.ACTION_SCANNER_FOUND_DEVICE";

    public static PendingIntent mPendingIntent;

    private static Context mContext;

    private static int originalVolume = 0;
    private static AudioManager mAudioManager;

    /**
     * Constructor
     */
    public BeaconReceiver() {
        Log.v(TAG, "in Constructor");
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //if user delete the card
        if (!TrackerSharePreference.getConstant(context).isCardRegistered()) return;

        MyApplication.appendLog(MyApplication.getCurrentDate() + " : BeaconReceiver OnReceive callback\n");
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

                            TrackerSharePreference trackerSharePreference = TrackerSharePreference.getConstant(context);
                            trackerSharePreference.setAlreadyCreateWorkerThread(false);
                            trackerSharePreference.setRanging(true);
                            trackerSharePreference.setBeaconTimestamp(MyApplication.getCurrentDate());
                            trackerSharePreference.setCurrentState(StateMachine.RING_NOTIFICATION_STATE.getValue());
                            playSound(context.getApplicationContext());
                            showRangNotification(context.getApplicationContext());

                            Log.d("BleReceiver", "showNotification");

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

        BleClient bleClient = TrackerSharePreference.getConstant(mContext).getEthernomCard();
        byte[] majors;
        byte[] minors;
        String major = bleClient.getMajor();
        String minor = bleClient.getMinor();
        majors = hexStringToByteArray(major);
        minors = hexStringToByteArray(minor);


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
        manData.put(18, majors[0]); //major
        manData.put(19, majors[1]); //major
        manData.put(20, minors[0]); //minor
        manData.put(21, minors[1]); //minor
        manData.put(22, (byte) 0xc3);
        builder.setManufacturerData(0x004c, manData.array());

        return builder.build();
    }

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

        Intent intent = new Intent(mContext, BeaconReceiver.class); // explicit intent
        intent.setAction(BeaconReceiver.ACTION_SCANNER_FOUND_DEVICE);
        int id = 0;

        mPendingIntent = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_ONE_SHOT);

        // Now start the scanner
        try {
            Log.d(TAG, "Asking library to start scanning.");
            BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, mPendingIntent);
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Start scanning \n");
            TrackerSharePreference.getConstant(context).setCurrentState(StateMachine.WAITING_FOR_BEACON.getValue());
            TrackerSharePreference.getConstant(context).setAlreadyCreateWorkerThread(true);

        } catch (Exception e) {
            Log.e(TAG, "ERROR in startScan() " + e.getMessage());
        }
    }


    public static void stopScan() {

        Log.d(TAG, "Stop scan");

        if (mContext == null) {
            Log.d(TAG, "Can't stop: mContext null");
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Can't stop: mContext null\n");

            return;
        }

        if (mPendingIntent == null) {
            Log.d(TAG, "Can't stop: mPendingIntent null");
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Can't stop: mPendingIntent null\n");

            return;
        }

        try {
            Log.d(TAG, "Asking library to stop scanning.");
            BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            bluetoothAdapter.getBluetoothLeScanner().stopScan(mPendingIntent);
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : Stop scanning \n");

        } catch (Exception e) {
            Log.e(TAG, "ERROR in stopScan() " + e.getMessage());
            MyApplication.appendLog(MyApplication.getCurrentDate() + " : ERROR in stopScan() \n");

        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    //call to play  sound
    public static void playSound(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        assert mAudioManager != null;
        originalVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d(TAG, "originalVolume: "+ originalVolume);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
        mAudioManager.setMode(AudioManager.STREAM_MUSIC);
        mAudioManager.setSpeakerphoneOn(true);

        if (mp != null) {
            mp.stop();
        }

        mp = MediaPlayer.create(context, R.raw.ringingsound);
        mp.setLooping(true);
        mp.start();

    }

    // call to stop sound
    public static void stopSound() {
        if(mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
        }
        if (mp != null) {
            mp.stop();
            Log.d(TAG, "Stop Sound");
        }
    }


}
