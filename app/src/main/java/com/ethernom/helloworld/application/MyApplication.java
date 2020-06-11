package com.ethernom.helloworld.application;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.ethernom.helloworld.receiver.BluetoothStateChangeReceiver;
import com.ethernom.helloworld.receiver.LocationStateChangeReceiver;
import com.ethernom.helloworld.screens.BaseActivity;
import com.ethernom.helloworld.util.ForegroundCheckTask;
import com.ethernom.helloworld.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        // Register for broadcasts on Bluetooth state change
        IntentFilter btIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(new BluetoothStateChangeReceiver(), btIntentFilter);

        // Register for broadcasts on Location state change
        IntentFilter filterLocation = new IntentFilter(LocationManager.MODE_CHANGED_ACTION);
        registerReceiver(new  LocationStateChangeReceiver(), filterLocation);


        Log.d("MyApplication", Utils.isLocationEnabled(this)+"");

    }

    static public void appendLog(String logs) {

        try {
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
        }
    }
    static public void saveLogWithCurrentDate(String logs) {
        appendLog(getCurrentDate()+" : " +logs +"\n");
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


    public static void saveCurrentStateToLog(Context context){
        String currentState = TrackerSharePreference.getConstant(context).getCurrentState();

        appendLog(getCurrentDate()+ " : Current State "+ currentState  + " "+ BaseActivity.Companion.getEnumNameByValue(currentState)+ "\n");

    }

}
