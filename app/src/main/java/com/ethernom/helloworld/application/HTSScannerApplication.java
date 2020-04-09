package com.ethernom.helloworld.application;

/** 
 * This file exists primarily so we can have a context to use before MainActivity starts.
 * 
 * See here for getting the application sContext:
 * https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android
 */

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Set;


public class HTSScannerApplication extends Application implements Configuration.Provider {

    private static final String TAG = "APP_HTSScannerApp";
    public static final String SETTINGS_APP_FIRST_TIME = "com.ethernom.helloworld.SETTINGS_APP_FIRST_TIME";
    public static final String SETTINGS_SCANNING = "com.ethernom.helloworld.SETTINGS_SCANNING";

    private static SharedPreferences sSharedPreferences;

    private static Context sContext;

    /** Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
    */
    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        Log.d(TAG, " ");
        Log.d(TAG, "*** onCreate()");
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(HTSScannerApplication.getAppContext());
    }

    public static Context getAppContext() {
        return sContext;
    }

    /*********************************************************************************************
     *
     * MANAGE SHAREDPREFERENCES
     *
     * This allows information to be saved across reboots, so the state before the reboot can be restored afterwards
     *
     *********************************************************************************************/

    public static void setupSharedPreferences() {

        // Check for for run. I am not doing anything here, but I wanted to test the concept. See refs above.
        if(sSharedPreferences.getBoolean(SETTINGS_APP_FIRST_TIME, true)) {
            // run your one time code
            Log.d(TAG, "This application is being run for the first time");
            SharedPreferences.Editor editor = sSharedPreferences.edit();
            editor.putBoolean(SETTINGS_APP_FIRST_TIME, false);
            editor.commit();
        };

        // debug: print all the values
        Log.d (TAG, printAllPreferences());
    }

    /**
     * Print all of the shared preferences_database, for debugging
     *
     * @return a string that can be printed or displayed
     */
    public static String printAllPreferences() {
        Map<String, ?> allPrefs = sSharedPreferences.getAll(); //your sharedPreference
        Set<String> set = allPrefs.keySet();
        String msg = "Shared Preferences:";
        // iterate through each saved setting
        for(String s : set){
            msg += s + " <" + allPrefs.get(s).getClass().getSimpleName() +"> =  "
                    + allPrefs.get(s).toString() + "\r\n";
        }
        return msg;
    }



    public static void saveScanning(Boolean scanning) {
        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putBoolean(SETTINGS_SCANNING, scanning);
        editor.commit();
    }


    public static Boolean getScanning() {
        return  sSharedPreferences.getBoolean(SETTINGS_SCANNING, false);
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
          return new  Configuration.Builder()
                        .setMinimumLoggingLevel(Log.DEBUG)
                        .build();
    }
    static public void appendLog(String logs) {

        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File myFile = new File(path, "ble_locate.txt");
            FileOutputStream fOut = new FileOutputStream(myFile,true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(logs);
            myOutWriter.close();
            fOut.close();
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
