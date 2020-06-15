package com.ethernom.helloworld.statemachine;

import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.Context;
import android.os.Build;
import android.util.Log;


import androidx.annotation.RequiresApi;

import com.ethernom.helloworld.application.MyApplication;
import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.model.CardInfo;
import com.ethernom.helloworld.model.FwInfo;
import com.ethernom.helloworld.screens.DiscoverDeviceActivity;
import com.ethernom.helloworld.util.Conversion;
import com.ethernom.helloworld.util.EthernomConstKt;
import com.ethernom.helloworld.util.StateMachine;
import com.ethernom.helloworld.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static androidx.core.content.ContextCompat.getMainExecutor;
import static com.ethernom.helloworld.util.CardConnection.*;
import static org.bouncycastle.util.Arrays.reverse;

public class FirmwareInfoState {

    private static String TAG = FirmwareInfoState.class.getSimpleName();

    private boolean alreadyCallDisconnect;
    private Context context;
    private List<Byte> _temp_buffer = new ArrayList<>();

    private String _sn;
    private String _m_id;
    private String _card_name;
    private String _card_firmware_version;
    private String _card_ble_version;
    private String _card_boot_version;
    public CardInfo cardInfo;

    enum InputEvent {
        ESTABLISH_CONNECTION,
        GET_FIRMWARE_VERSION,
        GET_BLE_VERSION,
        GET_BOOT_VERSION,
        GET_MENU_FAC_NUMBER,
        GET_SERIAL_NUMBER
    }

    public FirmwareInfoState(Context context) {
        this.context = context;
    }




    // every Input event & Action function inside
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void firmwareDispatcher(InputEvent event, Boolean result) {
        switch (event) {
            case ESTABLISH_CONNECTION: {
                if (result) {
                    saveToLog("ESTABLISH_CONNECTION", true);
                    // Establish Connection Success
                    H2CGetFirmwareVn();
                } else {
                    saveToLog("ESTABLISH_CONNECTION", false);
                    // Establish Connection Failed
                    tryAgainDialog();
                }
                break;
            }
            case GET_FIRMWARE_VERSION: {
                if (result) {
                    saveToLog("GET_FIRMWARE_VERSION", true);
                    // Get Firmware Version Success
                    H2CGetBLEVn();
                } else {
                    saveToLog("GET_FIRMWARE_VERSION", false);
                    // Get Firmware Version Failed
                    tryAgainDialog();

                }
                break;
            }
            case GET_BLE_VERSION: {
                if (result) {
                    saveToLog("GET_BLE_VERSION", true);
                    // Get BLE Version Success
                    H2CGetBOOTVn();
                } else {
                    saveToLog("GET_BLE_VERSION", false);
                    // Get BLE Version Failed
                    tryAgainDialog();
                }
                break;
            }
            case GET_BOOT_VERSION: {
                if (result) {
                    saveToLog("GET_BOOT_VERSION", true);
                    // Get BOOT Version Success
                    H2CGetMENU();
                } else {
                    saveToLog("GET_BOOT_VERSION", false);
                    // Get BOOT Version Failed
                    tryAgainDialog();
                }
                break;
            }
            case GET_MENU_FAC_NUMBER: {
                if (result) {
                    saveToLog("GET_MENU_FAC_NUMBER", true);
                    // Get Manufacture Number Success
                    H2CGetSerialNum();
                } else {
                    saveToLog("GET_MENU_FAC_NUMBER", false);
                    // Get Maufacture Number Failed
                    tryAgainDialog();
                }
                break;
            }

            case GET_SERIAL_NUMBER: {
                if (result) {
                    saveToLog("GET_SERIAL_NUMBER", true);
                    // Get Serial Number Success
                    ArrayList<FwInfo> fwInfoList = new ArrayList<>();
                    fwInfoList.add(new FwInfo(_card_firmware_version, "1"));
                    fwInfoList.add(new FwInfo(_card_ble_version, "2"));
                    fwInfoList.add(new FwInfo(_card_boot_version, "3"));

                    // change to 1003
                    TrackerSharePreference.getConstant(context).setCurrentState(StateMachine.CHECKING_UPDATE_FIRMWARE.getValue());

                    if (Utils.haveNetworkConnection(context)) {
                        // Call to check firmware update
                        new CheckUpdateFirmwareState(context).check(fwInfoList,
                                serialNumber,
                                menuFac
                        );
                        //setDescriptionOnLoading("Loading: Checking compatibility...");
                    } else {
                        MyApplication.saveLogWithCurrentDate("No Internet Connection");
                        getMainExecutor(context).execute(() ->
                                new AlertDialog.Builder(context)
                                        .setTitle("Error")
                                        .setMessage("Please check your internet connection and try again.")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", (dialog, which) -> {
                                            dialog.dismiss();
                                            new InitializeState().goToInitialState(context);

                                        }).show());
                    }

                } else {
                    saveToLog("GET_SERIAL_NUMBER", false);
                    // Get Serial Number Failed
                    tryAgainDialog();
                }
                break;
            }
            default:{
                ((DiscoverDeviceActivity) context).runOnUiThread(() ->
                        stateMachineCallback.unknownEvent()
                );
            }
        }
    }

    private void saveToLog(String value, Boolean result){
        if (result){
            MyApplication.saveLogWithCurrentDate(value+" succeeded");
        }else{
            MyApplication.saveLogWithCurrentDate(value+" failed");
        }
    }


    /* connect to card */
    public void establishBLEConnection() {
        Log.i(TAG, "found specific ethernom card");
        BluetoothDevice periphEthCard = mBluetoothAdapter.getRemoteDevice(cardInfo.GetDeviceID());
        gatt = periphEthCard.connectGatt(context, false, gattCallback);
    }

    /* For Bluetooth Gatt Call back method */
    private BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        Log.i(TAG, "gatt connected, discover services");
                        gatt.discoverServices();
                    }
                    if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                        Log.i(TAG, "gat disconnected");

                        gatt.close();
                        if (alreadyCallDisconnect) {
                            tryAgainDialog();
                            setAlreadyCallDisconnect(false);
                            firmwareDispatcher(InputEvent.ESTABLISH_CONNECTION, false);

                        }
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    ethCharacteristic = gatt.getService(ETH_serviceUUID).getCharacteristic(ETH_characteristicUUID);
                    UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = Conversion.convertFromInteger(0x2902);
                    if (ethCharacteristic != null) {
                        Log.i(TAG, "onServicesDiscovered, ethernom characteristic found");
                        BluetoothGattDescriptor descriptor = ethCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.setCharacteristicNotification(ethCharacteristic, true);
                            Log.i(TAG, "onServicesDiscovered, gatt write descriptor");
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    Log.i(TAG, "onDescriptorWrite, gatt write descriptor: " + status);
                    Log.i(TAG, "onDescriptorWrite, fire listener");
                    if (status == 0) {
                        Log.i(TAG, "Connection success");

                        //setDescriptionOnLoading("Loading: Verifying connection...");
                        firmwareDispatcher(InputEvent.ESTABLISH_CONNECTION, true);
                    } else {
                        Log.i(TAG, "Connection fail");

                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic,
                                                  int status) {
                    if (status != 0) {
                        Log.i(TAG, "oncharacteristicwrite failed write");
                    } else {
                        Log.i(TAG, "oncharacteristicwrite success");
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status != 0)
                        Log.i(TAG, "oncharacteristicread failed write");
                    else
                        Log.i(TAG, "oncharacteristicread success");
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    byte[] value = characteristic.getValue();
                    handle_receives_packet(value);
                }


            };

    /* FOor disconnect card */
    public void DisconnectCard() {
        if (gatt != null) {
            gatt.close();
            setAlreadyCallDisconnect(true);
        }
    }

    public void setAlreadyCallDisconnect(boolean alreadyCallDisconnect) {
        this.alreadyCallDisconnect = alreadyCallDisconnect;
    }

    // Get Card Firmware version
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void H2CGetFirmwareVn() {
        byte[] payload = new byte[4];
        payload[0] = EthernomConstKt.getCMD_VERSION_CHECK();
        payload[1] = 0;
        payload[2] = 0;
        payload[3] = 0;

        byte[] mHeader = makeTransportHeader((byte) 0x94, (byte) 0x14, (byte) 0x00, (byte) 0x02, payload.length);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> {

            String hexa = Conversion.bytesToHex(buffer);
            if (buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCMD_VERSION_RSP()) {
                /*
                    MAX LENGTH OF HEADER 8 CHARS
                    MAX LENGTH OF APDU HEADER 4 CHARS
                    MAX LENGTH OF CARD NAME 16 CHARS
                    MAX LENGTH OF CARD VERSION 12 CHARS
                    => HEADER(8) + APDU HEADER(4) + CARD NAME(16) + CARD BLE VERSION(12);
                    IF WANT TO GET CARD NAME NEED TO START FROM INDEX 12 TO INDEX(12+16)
                   */
                byte[] CName = new byte[16];
                System.arraycopy(buffer, 12, CName, 0, 16);
                this._card_name = Conversion.convertHexToAscII(Conversion.bytesToHex(CName)).trim();
                Log.d(TAG, "CardName: " + Conversion.bytesToHex(CName));
                Log.d(TAG, "CardName: " + this._card_name);

                /*
                    MAX LENGTH OF HEADER 8 CHARS
                    MAX LENGTH OF APDU HEADER 4 CHARS
                    MAX LENGTH OF CARD NAME 16 CHARS
                    MAX LENGTH OF CARD VERSION 12 CHARS
                      => HEADER(8) + APDU HEADER(4) + CARD NAME(16) + CARD BLE VERSION(12);
                    IF WANT TO GET CARD VERSION NEED TO START FROM INDEX (12+16) TO INDEX (12+16+12)
                   */

                byte[] CVersion = new byte[12];
                System.arraycopy(buffer, 28, CVersion, 0, 12);
                this._card_firmware_version = Conversion.convertHexToAscII(Conversion.bytesToHex(CVersion)).trim();
                Log.d(TAG, "CardVersion: " + Conversion.bytesToHex(CVersion));
                Log.d(TAG, "CardVersion: " + this._card_firmware_version);

                firmwareDispatcher(InputEvent.GET_FIRMWARE_VERSION, true);
            } else {
                Log.i(TAG, "FAILED Firmware " + hexa);
                firmwareDispatcher(InputEvent.GET_FIRMWARE_VERSION, false);


            }


        });
    }

    // Get Card BLE version
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void H2CGetBLEVn() {
        byte[] payload = new byte[4];
        payload[0] = EthernomConstKt.getCMD_BLE_VERSION_CHECK();
        payload[1] = 0;
        payload[2] = 0;
        payload[3] = 0;

        byte[] mHeader = makeTransportHeader((byte) 0x94, (byte) 0x14, (byte) 0x00, (byte) 0x02, payload.length);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> {
            String hexa = Conversion.bytesToHex(buffer);

            if (buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCMD_BLE_VERSION_RSP()) {
                Log.i(TAG, "SUCCESS BLE " + hexa);

                /*
                    MAX LENGTH OF HEADER 8 CHARS
                    MAX LENGTH OF APDU HEADER 4 CHARS
                    MAX LENGTH OF CARD BLE VERSION 12 CHARS
                    => HEADER(8) + APDU HEADER(4) + CARD BLE VERSION(12);
                    IF WANT TO GET CARD BLE VERSION NEED TO START FROM INDEX 12 TO INDEX(12+12)
                   */

                byte[] BLEVersion = new byte[12];
                System.arraycopy(buffer, 12, BLEVersion, 0, 12);
                this._card_ble_version = Conversion.convertHexToAscII(Conversion.bytesToHex(BLEVersion)).trim();
                Log.d(TAG, "BLEVersion: " + Conversion.bytesToHex(BLEVersion));
                Log.d(TAG, "BLEVersion: " + this._card_ble_version);

                firmwareDispatcher(InputEvent.GET_BLE_VERSION, true);
            } else {
                Log.i(TAG, "FAILED BLE " + hexa);
                tryAgainDialog();
                firmwareDispatcher(InputEvent.GET_BLE_VERSION, false);


            }

        });
    }

    // Get Card BOOT version
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void H2CGetBOOTVn() {
        byte[] payload = new byte[4];
        payload[0] = EthernomConstKt.getCMD_BOOT2_VERSION_CHECK();
        payload[1] = 0;
        payload[2] = 0;
        payload[3] = 0;

        byte[] mHeader = makeTransportHeader((byte) 0x94, (byte) 0x14, (byte) 0x00, (byte) 0x02, payload.length);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> {
            String hexa = Conversion.bytesToHex(buffer);
            if (buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCMD_BOOT2_VERSION_RSP()) {
                Log.i(TAG, "SUCCESS BOOT " + hexa);
                /*
                    MAX LENGTH OF HEADER 8 CHARS
                    MAX LENGTH OF APDU HEADER 4 CHARS
                    MAX LENGTH OF CARD BOOT VERSION 12 CHARS
                    => HEADER(8) + APDU HEADER(4) + CARD BOOT VERSION(12);
                    IF WANT TO GET CARD BOOT VERSION NEED TO START FROM INDEX 12 TO INDEX(12+12)
                   */
                byte[] BOOTVersion = new byte[12];
                System.arraycopy(buffer, 12, BOOTVersion, 0, 12);
                this._card_boot_version = Conversion.convertHexToAscII(Conversion.bytesToHex(BOOTVersion)).trim();
                Log.d(TAG, "BOOTVersion: " + Conversion.bytesToHex(BOOTVersion));
                Log.d(TAG, "BOOTVersion: " + this._card_boot_version);
                firmwareDispatcher(InputEvent.GET_BOOT_VERSION, true);
            } else {
                Log.i(TAG, "FAILED BOOT " + hexa);
                tryAgainDialog();
                firmwareDispatcher(InputEvent.GET_BOOT_VERSION, false);


            }
        });
    }


    // Get Card MENU FAC version
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void H2CGetMENU() {
        byte[] payload = new byte[4];
        payload[0] = EthernomConstKt.getCM_GET_MENU();
        payload[1] = 0;
        payload[2] = 0;
        payload[3] = 0;
        byte[] mHeader = makeTransportHeader((byte) 0x94, (byte) 0x14, (byte) 0x00, (byte) 0x02, payload.length);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        Log.d(TAG, Conversion.bytesToHex(data));
        InitWriteToCard(data, buffer -> {
            String hexa = Conversion.bytesToHex(buffer);


            if (buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_GET_MENU_RSP()) {
                byte[] Tempmenu = new byte[buffer.length - 12];
                System.arraycopy(buffer, 12, Tempmenu, 0, buffer.length - 12);
                this._m_id = Conversion.convertHexToAscII(Conversion.bytesToHex(Tempmenu));
                Log.i(TAG, "SUCCESS MENU1" + this._m_id);
                menuFac = _m_id;
                firmwareDispatcher(InputEvent.GET_MENU_FAC_NUMBER, true);
            } else {
                Log.i(TAG, "FAILED MANU " + hexa);
                tryAgainDialog();
                firmwareDispatcher(InputEvent.GET_MENU_FAC_NUMBER, false);
            }

        });
    }

    // Get Card SN version
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void H2CGetSerialNum() {
        byte[] payload = new byte[4];
        payload[0] = EthernomConstKt.getCM_GET_SN();
        payload[1] = 0;
        payload[2] = 0;
        payload[3] = 0;

        byte[] mHeader = makeTransportHeader((byte) 0x94, (byte) 0x14, (byte) 0x00, (byte) 0x02, payload.length);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> {
            String hexa = Conversion.bytesToHex(buffer);

            if (buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_GET_SN_RSP()) {
                byte[] sn = new byte[buffer.length - 12];
                System.arraycopy(buffer, 12, sn, 0, buffer.length - 12);
                sn = reverse(sn);
                this._sn = Conversion.bytesToHex(sn);
                Log.i(TAG, "SUCCESS SN1 " + this._sn);
                serialNumber = _sn;
                firmwareDispatcher(InputEvent.GET_SERIAL_NUMBER, true);
            } else {
                Log.i(TAG, "FAILED SN " + hexa);
                tryAgainDialog();
                firmwareDispatcher(InputEvent.GET_SERIAL_NUMBER, false);

            }

        });
    }

    // Call For Suspend app
    public void RequestAppSuspend(byte appID) {
        byte[] payload = new byte[5];
        payload[0] = EthernomConstKt.getCM_SUSPEND_APP();
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = 0;
        payload[4] = appID;
        byte[] mHeader = makeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte) 0x02, payload.length);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> Log.i(TAG, "SUCCESS SUSPEND" + Conversion.bytesToHex(buffer)));
    }

    private void InitWriteToCard(byte[] data, BufferCallback bufferCallback) {
        mBufferCallBack = bufferCallback;
        WriteToCard(data);
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

    /* For Construct Header */
    private byte[] makeTransportHeader(byte srcport, byte destprt, byte control, byte interfaces, int payloadLength) {
        byte[] packet = new byte[8];
        packet[0] = srcport;
        packet[1] = destprt;
        packet[2] = control;
        packet[3] = interfaces;
        // length bytes, length is 2 bytes
        int Value0 = payloadLength & 0x00ff;
        int Value1 = payloadLength >> 8;
        packet[4] = (byte) Value0;
        packet[5] = (byte) Value1;
        packet[6] = (byte) 0;
        packet[7] = (byte) 0;
        int xorValue = packet[0];
        // xor the packet header for checksum
        for (int j = 1; j != 7; j++) {
            int c = packet[j];
            xorValue = xorValue ^ c;
        }
        packet[7] = (byte) xorValue;
        return packet;
    }

    private void tryAgainDialog() {

        Log.d("tryAgainDialog", TrackerSharePreference.getConstant(context).getCurrentState());
        // call disconnect from host & card
        DisconnectCard();
        // Back to discover screen and intent to initial state
        if (!TrackerSharePreference.getConstant(context).getCurrentState().equals(StateMachine.CARD_DISCOVERY_BLE_LOCATION_ON.getValue()))
        ((DiscoverDeviceActivity) context).runOnUiThread(() ->
                stateMachineCallback.showMessageErrorState("Make sure your device is powered on and authenticated. Please try again.")
        );

    }

    private void handle_receives_packet(byte[] value) {
        if (_temp_buffer.size() == 0) {
            int len = Conversion.get_payload_length(value[4], value[5]);
            if (len == (value.length - 8)) {
                on_card_read_success(value);
                _temp_buffer.clear();
            } else {
                for (byte b : value) {
                    _temp_buffer.add(b);
                }
            }
        } else {
            for (byte b : value) {
                _temp_buffer.add(b);
            }
            int initial_len = Conversion.get_payload_length(_temp_buffer.get(4), _temp_buffer.get(5));
            if (initial_len == (_temp_buffer.size() - 8)) {
                byte[] temp_buffer = new byte[_temp_buffer.size()];
                for (int i = 0; i < _temp_buffer.size(); i++) temp_buffer[i] = _temp_buffer.get(i);
                on_card_read_success(temp_buffer);
                _temp_buffer.clear();
            }
        }
    }

    private void on_card_read_success(byte[] value) {
        if (mBufferCallBack != null) {
            BufferCallback _tempBufferCallback = mBufferCallBack;
            mBufferCallBack = null;
            _tempBufferCallback.onData(value);
        }
    }

    private void setDescriptionOnLoading(String message) {
        ((DiscoverDeviceActivity) context).runOnUiThread(() ->
                mLoadingDialog.setLoadingDescription(message)
        );
    }

    public interface BufferCallback {
        void onData(byte[] buffer);
    }


}
