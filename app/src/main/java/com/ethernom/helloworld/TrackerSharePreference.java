package com.ethernom.helloworld;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class TrackerSharePreference {
    private static final String APP_SHARED_PREFS = "Tracker_Share_Preference";
    private SharedPreferences sharedPrefs;
    private static TrackerSharePreference mAppShareConstant;
    private Context mContext;

    public enum SharedPreKeyType {
        CURRENT_STATE,
        CARD_REGISTERED,
        BLE_STATUS
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

    public void setCurrentState(String currentState) {
        sharedPrefs.edit().putString(SharedPreKeyType.CURRENT_STATE.toString(), currentState).apply();
    }
    public String getCurrentState() {
        return sharedPrefs.getString(SharedPreKeyType.CURRENT_STATE.toString(), MainActivity.StateMachine.INITIAL.getValue());
    }

    public void setCardRegistered(Boolean isRegister) {
        sharedPrefs.edit().putBoolean(SharedPreKeyType.CARD_REGISTERED.toString(), isRegister).apply();
    }

    public Boolean isCardRegistered() {
        return sharedPrefs.getBoolean(SharedPreKeyType.CARD_REGISTERED.toString(), false);
    }

    public void setBLEStatus(Boolean isBLE) {
        sharedPrefs.edit().putBoolean(SharedPreKeyType.BLE_STATUS.toString(), isBLE).apply();
    }

    public Boolean isBLEStatus() {
        return sharedPrefs.getBoolean(SharedPreKeyType.BLE_STATUS.toString(), false);
    }

}
