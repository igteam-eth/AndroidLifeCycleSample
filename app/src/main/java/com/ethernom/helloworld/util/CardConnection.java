package com.ethernom.helloworld.util;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;

import com.ethernom.helloworld.callback.StateMachineCallback;
import com.ethernom.helloworld.dialog.LoadingDialog;
import com.ethernom.helloworld.statemachine.FirmwareInfoState;

import java.util.UUID;

public class CardConnection {

    public static UUID ETH_serviceUUID = UUID.fromString("19490001-5537-4F5E-99CA-290F4FBFF142");
    public static UUID ETH_characteristicUUID = UUID.fromString("19490002-5537-4F5E-99CA-290F4FBFF142");
    public static BluetoothGatt gatt;
    public static BluetoothAdapter mBluetoothAdapter;
    public static FirmwareInfoState.BufferCallback mBufferCallBack;
    public static BluetoothGattCharacteristic ethCharacteristic;
    public static LoadingDialog mLoadingDialog;
    public static String _host_name;
    public static StateMachineCallback stateMachineCallback;
    public static String serialNumber;
    public static String menuFac;
    public static Boolean isUserRefuseUpdate = false;

    static public void initBluetoothConnection(Context context, LoadingDialog loadingDialog){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) (context.getSystemService(Context.BLUETOOTH_SERVICE));
        assert bluetoothManager != null;
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mLoadingDialog = loadingDialog;
        stateMachineCallback  = ((StateMachineCallback) context);

    }



}
