package com.ethernom.helloworld;

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
import static android.content.Context.NOTIFICATION_SERVICE;
public class MyAppReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        //Which App need to relaunch ?
        //What is mechanism to relaunch the App?
        Log.e("LifeCycle", "Need to relaunch App");
        /*PackageManager pm = context.getPackageManager();
        Intent launchIntent = pm.getLaunchIntentForPackage("com.ethernom.helloworld");
        launchIntent.putExtra("some_data", "value");
        context.startActivity(launchIntent);*/
        silentNotification(context, NOTIFICATION_ID);
    }
    private static final String CHANNEL_ID = "alarm_channel";
    private static final int NOTIFICATION_ID = 12345678;
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void createNotificationChannelSilent(Context ctx) {
        final NotificationManager mgr = ctx.getSystemService(NotificationManager.class);
        if(mgr == null) return;
        final String name = ctx.getString(R.string.channel_name);
        if(mgr.getNotificationChannel(name) == null) {
            final NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setBypassDnd(true);
            mgr.createNotificationChannel(channel);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void silentNotification(Context context, int mId) {
        final int id = mId;
        final NotificationManager manager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannelSilent(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        builder.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText("Click to relaunch hello world");
        builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setWhen(System.currentTimeMillis());
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        manager.notify(id, builder.build());
    }
}