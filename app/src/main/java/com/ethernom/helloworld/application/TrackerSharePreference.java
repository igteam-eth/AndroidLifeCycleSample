package com.ethernom.helloworld.application;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.ethernom.helloworld.model.BleClient;
import com.ethernom.helloworld.util.StateMachine;
import com.google.gson.Gson;

public class TrackerSharePreference {
    private static final String APP_SHARED_PREFS = "Tracker_Share_Preference";
    private SharedPreferences sharedPrefs;
    private static TrackerSharePreference mAppShareConstant;
    private Context mContext;

    public enum SharedPreKeyType {
        CURRENT_STATE,
        CARD_REGISTERED,
        BLE_STATUS,
        ETHERNOM_CARD,
        LOCATION_STATUS
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
        MyApplication.saveCurrentStateToLog(mContext);
    }
    public String getCurrentState() {
        return sharedPrefs.getString(SharedPreKeyType.CURRENT_STATE.toString(), StateMachine.INITIAL.getValue());
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
    public void setLocationStatus(Boolean isOn) {
        sharedPrefs.edit().putBoolean(SharedPreKeyType.LOCATION_STATUS.toString(), isOn).apply();
    }

    public Boolean isLocationStatus() {
        return sharedPrefs.getBoolean(SharedPreKeyType.LOCATION_STATUS.toString(), false);
    }
    public void setEthernomCard(BleClient bleClient) {
        Gson gson = new Gson();
        String json = gson.toJson(bleClient);
        sharedPrefs.edit().putString(SharedPreKeyType.ETHERNOM_CARD.toString(), json).apply();
    }

    public BleClient getEthernomCard() {
        Gson gson = new Gson();
        String json = sharedPrefs.getString(SharedPreKeyType.ETHERNOM_CARD.toString(), null);
        return gson.fromJson(json, BleClient.class);
    }

}
