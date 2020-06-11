package com.ethernom.helloworld.screens;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ethernom.helloworld.application.MyApplication;

public abstract class CoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Activity Lifecycle", getClass().getSimpleName() + "=======> onCreate");
        saveToLog("onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Activity Lifecycle", getClass().getSimpleName() + "=======> onResume");
        saveToLog("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Activity Lifecycle", getClass().getSimpleName() + "=======> onPause");
        saveToLog("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Activity Lifecycle", getClass().getSimpleName() + "=======> onStop");
        saveToLog("onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("Activity Lifecycle", getClass().getSimpleName() + "=======> onStart");
        saveToLog("onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i("Activity Lifecycle", getClass().getSimpleName() + "=======> onRestart");
        saveToLog("onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Activity Lifecycle", getClass().getSimpleName() + "=======> onDestroy");
        saveToLog("onDestroy");
    }

    private void saveToLog(String state){
        MyApplication.appendLog(MyApplication.getCurrentDate() +" : " + getClass().getSimpleName() + " =======> "+ state + "\n");
    }

}
