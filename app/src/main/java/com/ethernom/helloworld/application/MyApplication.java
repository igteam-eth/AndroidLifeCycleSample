package com.ethernom.helloworld.application;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.ethernom.helloworld.R;
import com.ethernom.helloworld.receiver.NotificationDismissedReceiver;
import com.ethernom.helloworld.screens.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.app.NotificationManager.IMPORTANCE_HIGH;


public class MyApplication extends Application implements LifecycleObserver {


    @Override
    public void onCreate() {
        super.onCreate();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onAppBackgrounded(){
        Log.d("MyApplication", "In Background");
        TrackerSharePreference.getConstant(this).setAppInForeground(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onAppForegrounded() {
        Log.d("MyApplication", "In Foreground");
        TrackerSharePreference.getConstant(this).setAppInForeground(true);

    }
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onAppDestroyed() {
        Log.d("MyApplication", "Destroy");
        //if the app is ranging but user don't click the notification or acknowledge so we need to rearm scan
        if (TrackerSharePreference.getConstant(this).isRanging()) {
            TrackerSharePreference.getConstant(this).setRanging(false);
            TrackerSharePreference.getConstant(this).setAlreadyCreateWorkerThread(false);
        }
    }


    static public void appendLog(String logs) {


        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File myFile = new File(path, "ethernom_log.txt");
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void showSilentNotification(Context context) {
        final String CHANNEL_ID = "ring_channel";
        final NotificationManager manager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        final NotificationChannel channel =
                new NotificationChannel(CHANNEL_ID, "Ethernom", IMPORTANCE_HIGH);
        assert manager != null;
        manager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        builder.setContentTitle("Ethernom Tracker");
        builder.setContentText("You rang your phone from your device");
        builder.setAutoCancel(true);
        builder.setDeleteIntent(createOnDismissedIntent(context));
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("NOTIFICATION", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        manager.notify(0, builder.build());
    }

    public static void showAlertDialog(Context context ,String title, String message){

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .show();
    }
    private static PendingIntent createOnDismissedIntent(Context context) {
        Intent intent = new Intent(context, NotificationDismissedReceiver.class);
        intent.putExtra("NOTIFICATION_DISMISS", true);

        return PendingIntent.getBroadcast(context.getApplicationContext(),
                0, intent, 0);
    }

}
