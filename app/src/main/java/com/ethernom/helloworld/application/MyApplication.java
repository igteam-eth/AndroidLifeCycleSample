package com.ethernom.helloworld.application;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;


import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import com.ethernom.helloworld.R;
import com.ethernom.helloworld.screens.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.app.NotificationManager.IMPORTANCE_HIGH;


public class MyApplication extends Application  {


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
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("NOTIFICATION", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        manager.notify(0, builder.build());
    }

}
