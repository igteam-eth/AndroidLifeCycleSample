package com.ethernom.helloworld.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TaskRemoveAlarm extends BroadcastReceiver {

    private String TAG = TaskRemoveAlarm.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive");

    }
}
