package com.ethernom.helloworld.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ethernom.helloworld.application.SettingSharePreference;
import com.ethernom.helloworld.receiver.BLEAndLocationRegisterManager;

import static com.ethernom.helloworld.application.MyApplication.mBluetoothStateChangeReceiver;
import static com.ethernom.helloworld.application.MyApplication.mLocationStateChangeReceiver;

public class BLEAndLocationRegistrationService extends Service {

    private String TAG = BLEAndLocationRegistrationService.class.getSimpleName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        Log.d(TAG, "onTaskRemoved");

        if (!SettingSharePreference.getConstant(getApplicationContext()).IsAlreadyRemove()) {
            getApplicationContext().unregisterReceiver(mBluetoothStateChangeReceiver);
            getApplicationContext().unregisterReceiver(mLocationStateChangeReceiver);
            getApplicationContext().sendBroadcast(
                    new Intent(
                            getApplicationContext(),
                            BLEAndLocationRegisterManager.class
                    ));
            SettingSharePreference.getConstant(getApplicationContext())
                    .setIsAlreadyRemove(true);
        }
    }
}
