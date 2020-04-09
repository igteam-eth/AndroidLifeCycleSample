package com.ethernom.helloworld.adapter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.ethernom.helloworld.application.TrackerSharePreference;
import com.ethernom.helloworld.model.CardInfo;
import com.ethernom.helloworld.util.Conversion;
import com.ethernom.helloworld.util.ECDSA_P256;
import com.ethernom.helloworld.util.EthernomConstKt;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BLEAdapter {
    private static String TAG = "EtherBTAdapter";
    private static UUID ETH_serviceUUID = UUID.fromString("19490001-5537-4F5E-99CA-290F4FBFF142");
    private static UUID ETH_characteristicUUID = UUID.fromString("19490002-5537-4F5E-99CA-290F4FBFF142");
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice periphEthCard;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic ethCharacteristic;
    private int maxByteSize = 244;
    private Context _context;
    private bufferCallback mBufferCallBack;
    private List<Byte> _temp_buffer = new ArrayList<Byte>();
    private String _host_name;
    private BLEAdapterCallback mBLEAdapterCallback;
    public BLEAdapter(Context context, BLEAdapterCallback mBLEAdapterCallback){
        this._context = context;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) (context.getSystemService(Context.BLUETOOTH_SERVICE));
        assert bluetoothManager != null;
        mBluetoothAdapter = bluetoothManager.getAdapter();
        this.mBLEAdapterCallback = mBLEAdapterCallback;
    }
    /* connect to card */
    public void ConnectCard(CardInfo cardInfo){
        Log.i(TAG, "found specific ethernom card");
        periphEthCard = mBluetoothAdapter.getRemoteDevice(cardInfo.GetDeviceID());
        gatt = periphEthCard.connectGatt(_context, false, gattCallback);
    }
    /* For Bluetooth Gatt Call back method */
    private BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        Log.i(TAG, "gatt connected, discover services");
                        gatt.discoverServices();
                    }
                    if(newState == BluetoothGatt.STATE_DISCONNECTED){
                        Log.i(TAG,"gat disconnected");
                            gatt.close();
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
                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    Log.i(TAG, "onDescriptorWrite, gatt write descriptor: "+Integer.toString(status));
                        Log.i(TAG, "onDescriptorWrite, fire listener");
                        if (status == 0) {
                            Log.i(TAG, "Connection success");
                            H2CAuthentication((byte) 0x01);
                        }
                        else
                            Log.i(TAG, "Connection fail");
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
                };
            };
    /* FOor disconnect card */
    public void DisconnectCard(){
        if(gatt != null)
            gatt.close();
    }
    public void H2CAuthentication(byte appID){
        byte[] payload = new byte[5];
        payload[0] = EthernomConstKt.getCM_INIT_APP_PERM();
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = 0;
        payload[4] = appID;
        byte[] mHeader = MakeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte)0x02, payload.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> {
            if(buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_AUTHENTICATE()){
                byte[] challenge = new byte[32];
                for(int i=0; i<32; i++) challenge[i] = buffer[i + 12];
                generate_auth_rsp(challenge);
            }else{
                RequestAppSuspend((byte) 0x01);
            }
        });
    }
    private void generate_auth_rsp(byte[] challenge){
        ECDSA_P256 mSign = new ECDSA_P256();
        String pubKey = TrackerSharePreference.getConstant(_context).getPublicKey();
        String privKey = TrackerSharePreference.getConstant(_context).getPrivateKey();
        List<Byte> responseBytes = mSign.generate_auth_rsp(challenge, pubKey, privKey);
        byte[] temp = new byte[responseBytes.size()];
        for(int i=0; i<responseBytes.size(); i++) temp[i] = responseBytes.get(i);
        byte[] mHeader = MakeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte)0x02, temp.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, temp);
        InitWriteToCard(data, buffer -> {
            if(buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_RSP()){
                if(buffer[12] == EthernomConstKt.getCM_ERR_SUCCESS()){
                    Log.i(TAG, "Auth success");
                    H2CAppLaunch(android.os.Build.MODEL, (byte) 0x01);
                }else{
                    Log.i(TAG, "Auth fails");
                    RequestAppSuspend((byte) 0x01);
                }
            }else{
                Log.i(TAG, "Invalid command");
                RequestAppSuspend((byte) 0x01);
            }
        });
    }
    public void H2CAppLaunch(String host_name, byte appID){
        _host_name = host_name;
        byte[] payload = new byte[5];
        payload[0] = EthernomConstKt.getCM_LAUNCH_APP();
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = 0;
        payload[4] = appID;
        byte[] mHeader = MakeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte)0x02, payload.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);

        InitWriteToCard(data, buffer -> {
            if(buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_RSP()) {
                if (buffer[12] == EthernomConstKt.getCM_ERR_SUCCESS()) {
                    Log.i(TAG, "App launched success");
                    H2CRequestSessionPIN();

                }else if(buffer[12] == EthernomConstKt.getCM_ERR_APP_BUSY()){
                    Log.i(TAG, "App launched busy");

                    RequestAppSuspend(appID);
                }else {
                    Log.i(TAG, "App launched fails");
                    RequestAppSuspend((byte) 0x01);
                }
            }else{
                Log.i(TAG, "Invalid command: Launch app state");
                RequestAppSuspend((byte) 0x01);
            }
        });
    }
    public void H2CRequestSessionPIN() {
        Log.i(TAG,"requestSessionPIN");
        List<Byte> payload = new ArrayList<Byte>();
        JSONObject session_pin = null;
        Integer pin_len = TrackerSharePreference.getConstant(_context).getPinLength();
        byte[] hname = Conversion.convertToByte(_host_name, 15);
        payload.add(EthernomConstKt.getCM_SESSION_REQUEST());
        payload.add((byte)0x00);
        payload.add((byte)19);
        payload.add((byte)0x00);
        payload.add((byte) 0x01);
        for (byte b : hname) payload.add(b);
        payload.add((byte)0x00);
        payload.add(pin_len.byteValue());
        payload.add((byte)0x00);

        byte[] payloadTemp = new byte[payload.size()];
        for(int i=0;i<payload.size();i++) payloadTemp[i] = payload.get(i);
        byte[] mHeader = MakeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte)0x02, payloadTemp.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, payloadTemp);

        InitWriteToCard(data, buffer -> {
            if(buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_SESSION_RSP()) {
                byte[] slice = Arrays.copyOfRange(buffer, 12, buffer.length);
                String new_PIN = Conversion.convertToString(slice);
                Log.i(TAG, "Success" + new_PIN);
                //H2CRequestBLETrackerInit();
                mBLEAdapterCallback.onGetPinSucceeded(new_PIN);

            }else if(buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_RSP()) {
                try {
                    String PIN2 = session_pin.getString("session");
                        Log.i(TAG, "Success" + PIN2);
                        RequestAppSuspend((byte) 0x01);
                }catch (JSONException e) {
                    RequestAppSuspend((byte) 0x01);
                }
            }else{
                RequestAppSuspend((byte) 0x01);
            }
        });
    }
    public void H2CRequestBLETrackerInit(){
        Log.i(TAG,"requestBLETrkInit");
        byte[] payload = Conversion.convertToByte((byte) 0x81, new String[]{_host_name});
        byte[] mHeader = MakeTransportHeader((byte) 0x93, (byte) 0x13, (byte) 0x00, (byte)0x02, payload.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> {
            String mBuffer = Conversion.bytesToHex(buffer);
            String result = mBuffer.substring(mBuffer.length() -8);
            Log.i(TAG, result);
            RequestAppSuspend((byte) 0x01);
            mBLEAdapterCallback.onGetMajorMinorSucceeded(result);
        });
    }
    public void RequestAppSuspend(byte appID){
        byte[] payload = new byte[5];
        payload[0] = EthernomConstKt.getCM_SUSPEND_APP();
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = 0;
        payload[4] = appID;
        byte[] mHeader = MakeTransportHeader((byte) 0x96, (byte) 0x16, (byte) 0x00, (byte)0x02, payload.length, (byte) 0x00);
        byte[] data = Conversion.concatBytesArray(mHeader, payload);
        InitWriteToCard(data, buffer -> {
            Log.i(TAG, "SUCCESS SUSPEND"+ Conversion.bytesToHex(buffer));
        });
    }
    private void InitWriteToCard(byte[] data, bufferCallback bufferCallback) {
        mBufferCallBack = bufferCallback;
        WriteToCard(data);
    }
    /* For Construct Header */
    private byte[] MakeTransportHeader(byte srcport, byte destprt, byte control, byte interfaces, int payloadLength, byte protocol) {
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
    private void on_card_read_success(byte[] value){
        if( mBufferCallBack != null) {
            bufferCallback _tempBufferCallback = mBufferCallBack;
            mBufferCallBack = null;
            _tempBufferCallback.onData(value);
        }
    }
    // a way to write byte buffers
    private void WriteToCard(byte[] data) {

        if(data.length > maxByteSize){
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
                    Thread.sleep(10);
                    for (byte[] message : splittedMessage) {
                        if (!doWrite(message)) {
                            break;
                        }
                        Thread.sleep(10);
                    }
                }
            } catch (InterruptedException e) {
                Log.i(TAG, Objects.requireNonNull(e.getMessage()));
            }
        }else{
            ethCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            doWrite(data);//callback.invoke("Write failed");
        }
    }
    private boolean doWrite(byte[] data){
        ethCharacteristic.setValue(data);
        if (!gatt.writeCharacteristic(ethCharacteristic)) {
            Log.i(TAG, "Error on doWrite");
            return false;
        }
        return true;
    }
    interface bufferCallback {
        void onData(byte[] buffer);
    }
    public interface BLEAdapterCallback {
        void onGetPinSucceeded(String pin);
        void onGetMajorMinorSucceeded(String data);
    }
}
