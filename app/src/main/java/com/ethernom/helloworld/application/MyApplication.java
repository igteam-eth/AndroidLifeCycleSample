package com.ethernom.helloworld.application;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.work.Configuration;

import com.ethernom.helloworld.BuildConfig;
import com.ethernom.helloworld.R;
import com.ethernom.helloworld.receiver.BluetoothStateChangeReceiver;
import com.ethernom.helloworld.receiver.LocationStateChangeReceiver;
import com.ethernom.helloworld.screens.BaseActivity;
import com.ethernom.helloworld.screens.SplashScreenActivity;
import com.ethernom.helloworld.util.ForegroundCheckTask;
import com.ethernom.helloworld.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

public class MyApplication extends Application implements Configuration.Provider, LifecycleObserver {

    public static BluetoothStateChangeReceiver mBluetoothStateChangeReceiver;
    public static LocationStateChangeReceiver mLocationStateChangeReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        Log.d("MyApplication", "onCreate() called");

        // Register for broadcasts on Bluetooth state change
        mBluetoothStateChangeReceiver = new BluetoothStateChangeReceiver();
        IntentFilter btIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateChangeReceiver , btIntentFilter);

        // Register for broadcasts on Location state change
        mLocationStateChangeReceiver = new LocationStateChangeReceiver();
        IntentFilter filterLocation = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver( mLocationStateChangeReceiver, filterLocation);

        Log.d("MyApplication", Utils.isLocationEnabled(this)+"");

    }

    static public void appendLog(String logs) {
        /*try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File myFile = new File(path, "ethernom_log.txt");
            FileOutputStream fOut = new FileOutputStream(myFile, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(logs);
            myOutWriter.close();
            fOut.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }*/
    }

    static public void saveLogWithCurrentDate(String logs) {
        appendLog(getCurrentDate() + " : " + logs + "\n");
    }


    public static boolean isAppInForeground(Context context) throws ExecutionException, InterruptedException {
        return new ForegroundCheckTask().execute(context).get();
    }

    public static String getCurrentDate() {
        long milliSeconds = System.currentTimeMillis();
        String dateFormat = "dd/MM/yyyy HH:mm:ss.SSS";
        // Create a DateFormatter object for displaying date in specified format.
        DateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static void saveCurrentStateToLog(Context context) {
        String currentState = TrackerSharePreference.getConstant(context).getCurrentState();
        appendLog(getCurrentDate() + " : Current State " + currentState + " " + BaseActivity.Companion.getEnumNameByValue(currentState) + "\n");

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showRangNotification(Context context) {

        Log.d("MyApplication", "showRangNotification");
        final NotificationManager manager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        final NotificationChannel channel =
                new NotificationChannel(Utils.CHANNEL_RANG, "Ethernom", IMPORTANCE_HIGH);
        assert manager != null;
        manager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(context, Utils.CHANNEL_RANG);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        builder.setContentTitle("Ethernom Tracker");
        builder.setContentText("You rang your phone from your device");
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.putExtra("NOTIFICATION", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        manager.notify(0, builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showLocationNotification(Context context) {

        Log.d("MyApplication", "showLocationNotification");

        final NotificationManager manager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        final NotificationChannel channel =
                new NotificationChannel(Utils.CHANNEL_LOCATION_OFF, "Ethernom", IMPORTANCE_HIGH);
        assert manager != null;
        manager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(context, Utils.CHANNEL_LOCATION_OFF);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        builder.setContentTitle("Location is off");
        builder.setContentText("Turn on Location services for the Ethernom Tracker app to keep track of your items.");
        builder.setAutoCancel(true);
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        manager.notify(0, builder.build());

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showBluetoothNotification(Context context) {

        Log.d("MyApplication", "showBluetoothNotification");
        final NotificationManager manager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        final NotificationChannel channel =
                new NotificationChannel(Utils.CHANNEL_BLE_OFF, "Ethernom", IMPORTANCE_HIGH);
        assert manager != null;
        manager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(context, Utils.CHANNEL_BLE_OFF);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        builder.setContentTitle("Bluetooth is off");
        builder.setContentText("Turn on Bluetooth services for the Ethernom Tracker app to keep track of your items.");
        builder.setAutoCancel(true);
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_BLUETOOTH_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        manager.notify(0, builder.build());
    }


    // create notification to show user start app
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void requiredLaunchAppNotification(Context context) {
        Log.d("MyApplication", "requiredLaunchAppNotification");
        final NotificationManager manager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        final NotificationChannel channel =
                new NotificationChannel(Utils.CHANNEL_RANG, "Ethernom", IMPORTANCE_HIGH);
        assert manager != null;
        manager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(context, Utils.CHANNEL_RANG);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        builder.setContentTitle("Ethernom Tracker");
        builder.setContentText("Start Tracker App");
        builder.setAutoCancel(true);
        builder.setOngoing(true);
        Intent intent = new Intent(context, SplashScreenActivity.class);
        intent.putExtra("NOTIFICATION", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        manager.notify(0, builder.build());
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build();
    }
}
