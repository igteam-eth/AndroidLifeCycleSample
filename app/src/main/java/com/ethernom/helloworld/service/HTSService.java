package com.ethernom.helloworld.service;

/**
 * See here on services: https://developer.android.com/guide/components/services
 *
 * Note that for Android 8 (26) you cannot start a service while the app is in background. See here:
 * https://stackoverflow.com/questions/51587863/bad-notification-for-start-foreground-invalid-channel-for-service-notification
 * https://stackoverflow.com/questions/46445265/android-8-0-java-lang-illegalstateexception-not-allowed-to-start-service-inten
 */

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ethernom.helloworld.R;
import com.ethernom.helloworld.screens.SecondActivity;
import com.ethernom.helloworld.receiver.BleReceiver;

import no.nordicsemi.android.log.ILogSession;
import no.nordicsemi.android.log.Logger;

@SuppressLint("NewApi")
public class HTSService extends Service  {

    private static final String TAG = "APP_HTSService";
    public static final String STARTUP_SOURCE = "com.eth.STARTUP_SOURCE";
    private static final String NOTIFICATION_CHANNEL_ID = "com.ethernom.helloworld.startup_CHANNEL";
    private static final String NOTIFICATIONS_CHANNEL_NAME = "com.ethernom.helloworld.startup_NOTIFICATIONS_CHANNEL_NAME";


    @Deprecated
    public static final String BROADCAST_BATTERY_LEVEL = "com.ethernom.helloworld.BROADCAST_BATTERY_LEVEL";
    @Deprecated
    public static final String EXTRA_BATTERY_LEVEL = "com.ethernom.helloworld.EXTRA_BATTERY_LEVEL";

    private final IBinder mBinder = new StartServiceBinder();

    private static String startupReason;

    private static BluetoothDevice sBleDevice;

    private static String sDeviceName;

    private static String sUuid;

    // This is the BroadcastReceiver in the BleReceiver class
    BroadcastReceiver mBleBroadcastReceiver;


    private ILogSession mLogSession;


    public HTSService() {
        Log.d(TAG, "in Constructor");
    }

    /**
     * "If the startService(intent) method is called and the service is not yet running,
     * the service object is created and the onCreate() method of the service is called."
     * <p>
     * see this for foreground services:
     * https://stackoverflow.com/questions/51587863/bad-notification-for-start-foreground-invalid-channel-for-service-notification
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // O is 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "onCreate() - registering BleReceiver (Version O or greater)");
            startNotificationChannel();
        }
        else {
            Log.d(TAG, "onCreate() - registering BleReceiver (less than Version O)");
        }

        startForegroundWithNotification();

        //This sets up the receiver that listens for BLE devices advertising
        // NOTE: actions must be registered in AndroidManifest.xml
        mBleBroadcastReceiver = new BleReceiver();    // My Receiver class, extends BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBleBroadcastReceiver, intentFilter);      // Now .ACTION_STATE_CHANGED events arrive on onReceive()

        // This receives messages for purposes of connecting to the device and getting its data
        LocalBroadcastManager.getInstance(this).registerReceiver(mLocalBroadcastReceiver, makeIntentFilter());
    }

    /**
     *
     * Starting in Android 8.0 (API level 26), all notifications must be assigned to a channel.
     *  See: https://developer.android.com/training/notify-user/channels
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void startNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATIONS_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        //notificationChannel.setLightColor(R.color.colorBlue);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(notificationChannel);
    }

    /**
     * set up an activity to be run if the user clicks on the notification
     * See https://developer.android.com/training/notify-user/navigation
     */
    private void startForegroundWithNotification() {

        // Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(this, SecondActivity.class);
        // Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(resultIntent);
        // Get the PendingIntent containing the entire back stack
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("HTS Scanner app is running in background")
                .setContentText("(Detects events even when the device is asleep)")
                .setContentInfo("Info about HTS Scanner app is provided here.")
                .setContentIntent(pendingIntent)
                .setChannelId(NOTIFICATION_CHANNEL_ID)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(3, notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy()");

        unregisterReceiver(mBleBroadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mLocalBroadcastReceiver);
    }

    @Override
    /**
     * Called by the OS: - can be called several times
     * "Called by the system every time a client explicitly starts the service by calling startService()"
     * "Once the service is started, the onStartCommand(intent) method in the service is called.
     *  It passes in the Intent object from the startService(intent) call."
     */
    public int onStartCommand(final Intent intent, final int flags, final int startId) {

        if (intent == null) {
            // I seems to have had this, for some reason... doc says this:
            // "This may be null if the service is being restarted after
            //   its process has gone away, and it had previously returned anything
            //    except {@link #START_STICKY_COMPATIBILITY}.
            Log.e(TAG, "onStartCommand() WITH NULL INTENT! id = " + startId + ", received " + startupReason);
        }
        else {

            startupReason = intent.getStringExtra(STARTUP_SOURCE);
            Log.d(TAG, "onStartCommand() id = " + startId + ", startup reason: '" + startupReason + "'");
        }

        // Ask the BleReceiver to restore its state
        // TODO - should this be called only after a reboot?
        BleReceiver.initialiseAfterReboot();

        //return START_REDELIVER_INTENT;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class StartServiceBinder extends Binder {

        public HTSService getService() {
            return HTSService.this;
        }
    }

    public String getStartupReason() {
        return startupReason;
    }


    /*********************************************************************************************
     *
     * Broadcast receiver
     *
     *********************************************************************************************/

    // Used by the activity to identify the broadcast items it wants to subscribe to
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleReceiver.DEVICE_FOUND);    // BleReceiver sends this when teh scan has found a device
        return intentFilter;
    }

    /**
     * This processes incoming broadcast messages
     */
    private final BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();

            switch (action) {

                // Message from BleReceiver when a matching device is found.
                // We connect to the device.
                case BleReceiver.DEVICE_FOUND: {
                    // here when the BleReceiver detects that a device has been found
                    sBleDevice = intent.getParcelableExtra(BleReceiver.BLE_DEVICE);
                    sDeviceName = intent.getStringExtra(BleReceiver.BLE_DEVICE_NAME);
                    sUuid = intent.getStringExtra(BleReceiver.BLE_SERVICE_UUID);
                    int rssi = intent.getIntExtra(BleReceiver.BLE_DEVICE_RSSI, 0);
                    Log.d(TAG, "Device found: " + sDeviceName + ", " + sBleDevice.getAddress() + " " + rssi + "dBm");

                    BleReceiver.setProcessingDevice(true);  // inhibit further device found events

                    // start a logger session
                    mLogSession = Logger.newSession(getApplicationContext(), getString(R.string.app_name), sBleDevice.getAddress(), sDeviceName);
                    Logger.d(mLogSession, "Started log session");

                    break;
                }

            }
        }
    };


}
