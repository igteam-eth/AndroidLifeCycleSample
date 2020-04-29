package com.ethernom.helloworld.application;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.ethernom.helloworld.model.BleClient;
import com.google.gson.Gson;

public class SettingSharePreference {
    private static final String APP_SHARED_PREFS = "Setting_Share_Preference";
    private SharedPreferences sharedPrefs;
    private static SettingSharePreference mAppShareConstant;
    private Context mContext = null;

    public enum SharedPreKeyType {
        PIN_LENGTH,
    }

    private SettingSharePreference(Context context) {
        mContext = context;
        this.sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
    }

    public synchronized static SettingSharePreference getConstant(Context context) {
        if (null == mAppShareConstant) {
            mAppShareConstant = new SettingSharePreference(context);
        }
        return mAppShareConstant;
    }

    public void clearAll() {
        sharedPrefs.edit().clear().apply();
    }



    public void setPinLength(int length) {
        sharedPrefs.edit().putInt(SharedPreKeyType.PIN_LENGTH.toString(), length).apply();
    }

    public int getPinLength() {
        return sharedPrefs.getInt(SharedPreKeyType.PIN_LENGTH.toString(), 2);
    }

}