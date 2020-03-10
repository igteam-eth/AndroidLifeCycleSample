package com.ethernom.helloworld.application;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.ethernom.helloworld.model.BleClient;
import com.google.gson.Gson;

public class TrackerSharePreference {
    private static final String APP_SHARED_PREFS = "Tracker_Share_Preference";
    private SharedPreferences sharedPrefs;
    private static TrackerSharePreference mAppShareConstant;
    private Context mContext = null;

    public enum SharedPreKeyType {
        PIN_LENGTH,
        PRI_KEY,
        PUB_KEY,
        ETHERNOM_CARD,
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

    public void setPinLength(int length) {
        sharedPrefs.edit().putInt(SharedPreKeyType.PIN_LENGTH.toString(), length).apply();
    }

    public int getPinLength() {
        return sharedPrefs.getInt(SharedPreKeyType.PIN_LENGTH.toString(), 2);
    }

    public String getPrivateKey() {
        return sharedPrefs.getString(SharedPreKeyType.PRI_KEY.toString(), "dd4eff4bc96940bcab8d077bf565107d0d9b78a842e23bc808c9e715a126474b");
    }

    public void setPrivateKey(String privateKey) {
        sharedPrefs.edit().putString(SharedPreKeyType.PRI_KEY.toString(), privateKey).apply();
    }

    public String getPublicKey() {
        return sharedPrefs.getString(SharedPreKeyType.PUB_KEY.toString(), "31325b200ffdfc07985f68bf40aed5bec69adaf2a270c6d8b3602948e608ddfc289b6dd740d5260a62eb05da6d313d2cb6a9f86f3bb1a2f3f11205a1a715d979");
    }

    public void setPublicKey(String publicKey) {
        sharedPrefs.edit().putString(SharedPreKeyType.PUB_KEY.toString(), publicKey).apply();
    }

    public void setEthernomCard(BleClient bleClient) {
        Gson gson = new Gson();
        String json = gson.toJson(bleClient);
        sharedPrefs.edit().putString(SharedPreKeyType.ETHERNOM_CARD.toString(), json).apply();
    }

    public BleClient getEthernomCard() {
        Gson gson = new Gson();
        //default object
        BleClient bleClient = new BleClient();
        String defaultObject = gson.toJson(bleClient);
        String json = sharedPrefs.getString(SharedPreKeyType.ETHERNOM_CARD.toString(), null);
        return gson.fromJson(json, BleClient.class);
    }
}
