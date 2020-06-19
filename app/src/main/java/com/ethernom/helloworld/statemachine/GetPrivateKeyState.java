package com.ethernom.helloworld.statemachine;

import android.content.Context;
import android.util.Log;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.callback.StateMachineCallback;
import com.ethernom.helloworld.presenter.privatekey.GetAppKeyCallback;
import com.ethernom.helloworld.presenter.privatekey.GetPrivateKeyPresenter;
import com.ethernom.helloworld.screens.DiscoverDeviceActivity;
import com.ethernom.helloworld.util.StateMachine;

public class GetPrivateKeyState implements GetAppKeyCallback {

    private StateMachineCallback stateMachineCallback;
    private Context context;

    public GetPrivateKeyState(Context context, StateMachineCallback stateMachineCallback ){
        this.stateMachineCallback = stateMachineCallback;
        this.context = context;
    }

    private static final String TAG = GetPrivateKeyState.class.getSimpleName();

    public void get(String sn, String menuFac){
        Log.d(TAG, "check update");
        new GetPrivateKeyPresenter(this, sn, menuFac).getData();
    }

    @Override
    public void getSucceeded(String appKey) {
        // Get Private Key Success
        // change to 1005
        if(!DiscoverDeviceActivity.Companion.getActivityState().equals("onStop")) {
            TrackerSharePreference.getConstant(context).setCurrentState(StateMachine.CARD_REGISTER.getValue());
            CardRegisterState cardRegisterState = new CardRegisterState(context);
            cardRegisterState.H2CAuthentication((byte) 0x01, appKey);
            MyApplication.saveLogWithCurrentDate("Get Private Key succeeded");
        } else {
            MyApplication.saveLogWithCurrentDate("Get Private Key Error");
            ((DiscoverDeviceActivity) context).runOnUiThread(() ->
                    stateMachineCallback.showMessageErrorState("Make sure your device is powered on and authenticated. Please try again.")
            );
        }
    }

    @Override
    public void getFailed(String message) {
        // Get Private Key Failed
        stateMachineCallback.getPrivateKeyFailed(message);
        MyApplication.saveLogWithCurrentDate("Get Private Key Error");
    }
}
