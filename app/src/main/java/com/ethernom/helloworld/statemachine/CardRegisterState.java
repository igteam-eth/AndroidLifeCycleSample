package com.ethernom.helloworld.statemachine;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.SettingSharePreference;
import com.ethernom.helloworld.screens.DiscoverDeviceActivity;
import com.ethernom.helloworld.util.*;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.ethernom.helloworld.util.CardConnection._host_name;
import static com.ethernom.helloworld.util.CardConnection.stateMachineCallback;
import static com.ethernom.helloworld.util.Utils.makeTransportHeader;
import static com.ethernom.helloworld.util.CardConnection.gatt;
import static com.ethernom.helloworld.util.CardConnection.mBufferCallBack;
import static com.ethernom.helloworld.util.CardConnection.ethCharacteristic;
import static com.ethernom.helloworld.util.CardConnection.mLoadingDialog;

public class CardRegisterState {

    private byte[] challenge = new byte[32];
    private Context context;
    private String privateKey;
    private String major_minor;
    private String new_PIN;

    public enum InputEvent{
        GET_CHALLENGE,
        AUTHENTICATION_WITH_CARD,
        GET_SESSION_PIN,
        GET_MAJOR_MINOR,
        APP_LAUNCH
    }

    private String TAG = CardConnection.class.getSimpleName();

    public CardRegisterState(Context context) {
        this.context = context;
    }

    // every Input event & Action function inside
    public void cardRegisterDispatcher(InputEvent event, Boolean result){
        switch (event){
            case GET_CHALLENGE:{
                if (result){
                    saveToLog("GET_CHALLENGE", true);
                    // Get Challenge Success
                    generate_auth_rsp(challenge, privateKey);
                }else{
                    saveToLog("GET_CHALLENGE", false);
                    // Get Challenge Failed
                    tryAgainDialog();
                }
                break;
            }
            case AUTHENTICATION_WITH_CARD:{
                if (result){
                    saveToLog("AUTHENTICATION_WITH_CARD", true);
                    // Authenticate Success
                    H2CAppLaunch(android.os.Build.MODEL, (byte) 0x01);
                    //setDescriptionOnLoading("Loading: Launching...");
                }else{
                    saveToLog("AUTHENTICATION_WITH_CARD", false);
                    // Authenticate Failed
                    RequestAppSuspend((byte) 0x01);
                    ((DiscoverDeviceActivity) context).runOnUiThread(() ->
                            stateMachineCallback.appMustBeUpdate()
                    );
                }
                break;
            }
            case APP_LAUNCH:{
                if (result){
                    saveToLog("APP_LAUNCH", true);
                    // Launch Success
                    H2CRequestSessionPIN();
                }else{
                    saveToLog("APP_LAUNCH", false);
                    // Launch Success
                    tryAgainDialog();
                }
                break;
            }
            case GET_SESSION_PIN:{
                if (result){
                    saveToLog("GET_SESSION_PIN", true);
                    // SESSION Success
                    stateMachineCallback.getPinSucceeded(new_PIN);
                }else{
                    saveToLog("GET_SESSION_PIN", false);
                    // SESSION Failed
                    tryAgainDialog();
                }
                break;
            }
            case GET_MAJOR_MINOR:{
                if (result){
                    saveToLog("GET_MAJOR_MINOR", true);
                    // MAJOR_MINOR Success
                    stateMachineCallback.onGetMajorMinorSucceeded(major_minor);
                    DisconnectCard();
                }else{
                    saveToLog("GET_MAJOR_MINOR", false);
                    // MAJOR_MINOR Failed
                    tryAgainDialog();

                }
                break;
            }

        }
    }

    // Call for authenticate card
    public void H2CAuthentication(byte appID, String privateKey) {
        this.privateKey = privateKey;
        byte[] payload = new byte[5];
        payload[0] = EthernomConstKt.getCM_INIT_APP_PERM();
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = 0;
        payload[4] = appID;
        byte[] mHeader = Utils.makeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte) 0x02, payload.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> {
            if (buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_AUTHENTICATE()) {
                for (int i = 0; i < 32; i++) challenge[i] = buffer[i + 12];
                cardRegisterDispatcher(InputEvent.GET_CHALLENGE, true);

            } else {
                cardRegisterDispatcher(InputEvent.GET_CHALLENGE, false);

            }
        });
    }
    private void InitWriteToCard(byte[] data, FirmwareInfoState.BufferCallback bufferCallback) {
        mBufferCallBack = bufferCallback;
        WriteToCard(data);
    }
    // call suspend app
    public void RequestAppSuspend(byte appID) {
        byte[] payload = new byte[5];
        payload[0] = EthernomConstKt.getCM_SUSPEND_APP();
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = 0;
        payload[4] = appID;
        byte[] mHeader = makeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte) 0x02, payload.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> {
            Log.i(TAG, "SUCCESS SUSPEND" + Conversion.bytesToHex(buffer));
        });
    }
    // call initial ble tracker to get major and minor
    public void H2CRequestBLETrackerInit() {
        _host_name = android.os.Build.MODEL;
        Log.i(TAG, "requestBLETrkInit");
        byte[] payload = Conversion.convertToByte((byte) 0x81, new String[]{_host_name});
        byte[] mHeader = makeTransportHeader((byte) 0x93, (byte) 0x13, (byte) 0x00, (byte) 0x02, payload.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> {
            String mBuffer = Conversion.bytesToHex(buffer);
            String result = mBuffer.substring(mBuffer.length() - 8);
            Log.i(TAG, result);
            major_minor = result;
            cardRegisterDispatcher(InputEvent.GET_MAJOR_MINOR, true);

        });
    }

    //Generate sign with challenge and private key
    private void generate_auth_rsp(byte[] challenge, String privateKey) {
        ECDSA_P256 mSign = new ECDSA_P256();
        List<Byte> responseBytes = mSign.generate_auth_rsp(challenge, privateKey);
        byte[] temp = new byte[responseBytes.size()];
        for (int i = 0; i < responseBytes.size(); i++) temp[i] = responseBytes.get(i);
        byte[] mHeader = makeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte) 0x02, temp.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, temp);
        InitWriteToCard(data, buffer -> {
            if (buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_RSP()) {
                if (buffer[12] == EthernomConstKt.getCM_ERR_SUCCESS()) {
                    Log.i(TAG, "Auth success");
                    cardRegisterDispatcher(InputEvent.AUTHENTICATION_WITH_CARD, true);
                } else {
                    Log.i(TAG, "Auth fails");
                    cardRegisterDispatcher(InputEvent.AUTHENTICATION_WITH_CARD, true);
                }

            } else {
                Log.i(TAG, "Invalid command");
                RequestAppSuspend((byte) 0x01);
                tryAgainDialog();
            }
        });
    }
    // call launch ble tracker
    public void H2CAppLaunch(String host_name, byte appID) {
        _host_name = host_name;
        byte[] payload = new byte[5];
        payload[0] = EthernomConstKt.getCM_LAUNCH_APP();
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = 0;
        payload[4] = appID;
        byte[] mHeader = makeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte) 0x02, payload.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);

        InitWriteToCard(data, buffer -> {
            if (buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_RSP()) {
                if (buffer[12] == EthernomConstKt.getCM_ERR_SUCCESS()) {
                    Log.i(TAG, "App launched success");
                    cardRegisterDispatcher(InputEvent.APP_LAUNCH, true);

                } else if (buffer[12] == EthernomConstKt.getCM_ERR_APP_BUSY()) {
                    Log.i(TAG, "App launched busy");
                    cardRegisterDispatcher(InputEvent.APP_LAUNCH, false);
                } else {
                    Log.i(TAG, "App launched fails");
                    cardRegisterDispatcher(InputEvent.APP_LAUNCH, false);
                }
            } else {
                Log.i(TAG, "Invalid command: Launch app state");
                cardRegisterDispatcher(InputEvent.APP_LAUNCH, false);
            }
        });
    }

    // call request session pin
    public void H2CRequestSessionPIN() {
        Log.i(TAG, "requestSessionPIN");
        List<Byte> payload = new ArrayList<Byte>();
        JSONObject session_pin = null;
        Integer pin_len = SettingSharePreference.getConstant(context).getPinLength();
        byte[] hname = Conversion.convertToByte(_host_name, 15);
        payload.add(EthernomConstKt.getCM_SESSION_REQUEST());
        payload.add((byte) 0x00);
        payload.add((byte) 19);
        payload.add((byte) 0x00);
        payload.add((byte) 0x01);
        for (byte b : hname) payload.add(b);
        payload.add((byte) 0x00);
        payload.add(pin_len.byteValue());
        payload.add((byte) 0x00);

        byte[] payloadTemp = new byte[payload.size()];
        for (int i = 0; i < payload.size(); i++) payloadTemp[i] = payload.get(i);
        byte[] mHeader = makeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte) 0x02, payloadTemp.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, payloadTemp);

        InitWriteToCard(data, buffer -> {
            if (buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_SESSION_RSP()) {
                byte[] slice = Arrays.copyOfRange(buffer, 12, buffer.length);
                String new_PIN = Conversion.convertToString(slice);
                Log.i(TAG, "Success" + new_PIN);
                this.new_PIN = new_PIN;
                cardRegisterDispatcher(InputEvent.GET_SESSION_PIN, true);

            } else {
                cardRegisterDispatcher(InputEvent.GET_SESSION_PIN, false);
            }
        });
    }
    private void saveToLog(String value, Boolean result){
        if (result){
            MyApplication.saveLogWithCurrentDate(value+" succeeded");
        }else{
            MyApplication.saveLogWithCurrentDate(value+" failed");
        }
    }

    private void tryAgainDialog() {
        // call suspend when app in ble tracker screen
        RequestAppSuspend((byte) 0x01);
        // call disconnect from host & card
        DisconnectCard();
        // Back to discover screen and intent to initial state
        ((DiscoverDeviceActivity) context ).runOnUiThread(() ->
                stateMachineCallback.showMessageErrorState("Make sure your device is powered on and authenticated. Please try again.")
        );
        // Next state

    }
    /* FOor disconnect card */
    public void DisconnectCard() {
        if (gatt != null) {
            gatt.close();
        }
    }

    // a way to write byte buffers
    private void WriteToCard(byte[] data) {

        int maxByteSize = 244;
        if (data.length > maxByteSize) {
            ethCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            int dataLength = data.length;
            int count = 0;
            byte[] firstMessage = null;
            List<byte[]> splittedMessage = new ArrayList<>();

            while (count < dataLength && (dataLength - count > maxByteSize)) {
                if (count == 0) {
                    firstMessage = Arrays.copyOfRange(data, count, count + maxByteSize);
                } else {
                    byte[] splitMessage = Arrays.copyOfRange(data, count, count + maxByteSize);
                    splittedMessage.add(splitMessage);
                }
                count += maxByteSize;
            }
            if (count < dataLength) {
                // Other bytes in queue
                byte[] splitMessage = Arrays.copyOfRange(data, count, data.length);
                splittedMessage.add(splitMessage);
            }
            try {
                boolean writeError = false;
                if (!doWrite(firstMessage)) {
                    writeError = true;
                }

                if (!writeError) {
                    Thread.sleep(30);
                    for (byte[] message : splittedMessage) {
                        if (!doWrite(message)) {
                            break;
                        }
                        Thread.sleep(30);
                    }
                }
            } catch (InterruptedException e) {
                Log.i(TAG, Objects.requireNonNull(e.getMessage()));
            }
        } else {
            try {
                ethCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                doWrite(data);//callback.invoke("Write failed");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    private boolean doWrite(byte[] data) {
        ethCharacteristic.setValue(data);
        if (!gatt.writeCharacteristic(ethCharacteristic)) {
            Log.i(TAG, "Error on doWrite");
            tryAgainDialog();
            return false;
        }

        return true;
    }
    private void setDescriptionOnLoading(String message) {
        ((DiscoverDeviceActivity) context).runOnUiThread(() ->
                mLoadingDialog.setLoadingDescription(message)
        );
    }
}
