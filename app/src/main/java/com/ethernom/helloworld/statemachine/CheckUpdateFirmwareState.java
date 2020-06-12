package com.ethernom.helloworld.statemachine;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.callback.StateMachineCallback;
import com.ethernom.helloworld.model.FwInfo;
import com.ethernom.helloworld.presenter.checkupdate.CheckUpdateCallback;
import com.ethernom.helloworld.presenter.checkupdate.CheckUpdatePresenter;
import com.ethernom.helloworld.util.CardConnection;
import com.ethernom.helloworld.util.StateMachine;
import com.ethernom.helloworld.util.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static androidx.core.content.ContextCompat.getMainExecutor;
import static com.ethernom.helloworld.util.CardConnection.menuFac;
import static com.ethernom.helloworld.util.CardConnection.serialNumber;


public class CheckUpdateFirmwareState implements CheckUpdateCallback {

    private Context context;
    private StateMachineCallback stateMachineCallback;


    CheckUpdateFirmwareState(Context context) {
        stateMachineCallback = CardConnection.stateMachineCallback;
        this.context = context;
    }

    private static final String TAG = CheckUpdateFirmwareState.class.getSimpleName();

    public void check(ArrayList<FwInfo> fwInfoList, String sn, String menuFac) {
        Log.d(TAG, "check update");
        new CheckUpdatePresenter().checkUpdate(this, fwInfoList, sn, menuFac);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void checkUpdateSuccess(boolean require) {
        Log.d(TAG, "check update response success");
        if (require) {
            // Server Response Update Needed
            stateMachineCallback.appRequiredToUpdate();
            Log.d(TAG, "check update require to update");

        } else {
            // Server Response Update Not Needed
            //go to state 1004
            if (Utils.haveNetworkConnection(context)) {
                TrackerSharePreference.getConstant(context).setCurrentState(StateMachine.GET_PRIVATE_KEY.getValue());
                new GetPrivateKeyState(context, stateMachineCallback).get(serialNumber, menuFac);
                Log.d(TAG, "check update not require to update");
            } else {
                getMainExecutor(context).execute(() ->
                        new AlertDialog.Builder(context)
                                .setTitle("Error")
                                .setMessage("Please check your internet connection and try again.")
                                .setCancelable(false)
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    dialog.dismiss();
                                   new  InitializeState().goToInitialState(context);

                                }).show());
            }

        }
    }

    @Override
    public void checkUpdatedFailed(@NotNull String message) {
        Log.d(TAG, "check update response failed");
        stateMachineCallback.checkUpdateFailed(message);
    }
}
