package com.ethernom.helloworld;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import static android.app.NotificationManager.IMPORTANCE_HIGH;
import static android.content.Context.NOTIFICATION_SERVICE;
public class MyAppReceiver extends BroadcastReceiver {
    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        //Which App need to relaunch ?
        //What is mechanism to relaunch the App?
        Log.e("LifeCycle", "Need to relaunch App");
        /*PackageManager pm = context.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage("com.ethernom.helloworld");
        launchIntent.putExtra("some_data", "value");
        context.startActivity(launchIntent);*/
        //silentNotification(context, NOTIFICATION_ID);
        silentNotification(context);
    }
    private static final String CHANNEL_ID = "alarm_channel";

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
        builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText("Click to relaunch hello world");
        builder.setAutoCancel(true);
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        manager.notify(0, builder.build());
    }
}