package com.ethernom.helloworld;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class TrackerSharePreference {
    private static final String APP_SHARED_PREFS = "Tracker_Share_Preference";
    private SharedPreferences sharedPrefs;
    private static TrackerSharePreference mAppShareConstant;
    private Context mContext = null;

    public enum SharedPreKeyType {
        INDEX
    }

    private TrackerSharePreference(Context context) {
        mContext = context;
        this.sharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
    }

    public synchronized static TrackerSharePreference getConstant(Context context) {
        if (null == mAppShareConstant) {
            mAppShareConstant = new TrackerSharePreference(context);
        }
        return mAppShareConstant;
    }

    public void clearAll() {
        sharedPrefs.edit().clear().apply();
    }

    public void setCurrentIndex(int index) {
        sharedPrefs.edit().putInt(SharedPreKeyType.INDEX.toString(), index).apply();
    }

    public int getCurrentIndex() {
        return sharedPrefs.getInt(SharedPreKeyType.INDEX.toString(), 0);
    }

}
