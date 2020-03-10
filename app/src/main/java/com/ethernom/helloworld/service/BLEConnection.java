package com.ethernom.helloworld.service;

import android.content.Context;
import android.util.Log;

import com.ethernom.android.etherapi.Crypto.EtherECDH;
import com.ethernom.android.etherapi.Crypto.Ether_AESEAX;
import com.ethernom.android.etherapi.EtherEncHeader;
import com.ethernom.android.etherapi.EthernomConstKt;
import com.ethernom.helloworld.LinkLayer.CardEventListener;
import com.ethernom.helloworld.LinkLayer.CardInfo;
import com.ethernom.helloworld.LinkLayer.EtherBTAdapter;
import com.ethernom.helloworld.LinkLayer.EtherCommAdapter;
import com.ethernom.helloworld.application.TrackerSharePreference;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.ethernom.android.etherapi.Extentions.ByteArrayKt.hexaSpaced;

public class BLEConnection   {
    EtherCommAdapter _adapter;
    private String TAG = "BLEConnection";
    EtherECDH _ecdh = new EtherECDH();
    Ether_AESEAX _aes_eax = new Ether_AESEAX();
    public etherSecureSessionListener _etherSecureSessionListener;
    private List<Byte> _temp_buffer = new ArrayList<Byte>();
    private Context mContext;

    String _host_name;
    String _pin;

    public BLEConnection(Context context, etherSecureSessionListener _etherSecureSessionListener) {
        _adapter = new EtherBTAdapter();
        _adapter.Init(context);
        this.mContext = context;
        this._etherSecureSessionListener = _etherSecureSessionListener;
        SetupCardListeners(_adapter);
    }

    interface writeCallbackListener {
        void onData(byte[] buffer);
    }

    public interface etherSecureSessionListener {
        void onAuthenticated(int code);
        void onAppLaunched(int code);
        void onPINRequest(String PIN, int code);
        void onRegisterSuccess(String data);
    }

    private writeCallbackListener _writeCallback = null;

     public String GetAdapterAddress() {
        return _adapter.GetAdapterAddress();
     }

     public void SetAdapterAddress(String address){
        _adapter.SetAdapterAddress(address);
     }

     public void CardOpen(CardInfo cardInfo) {
        _adapter.CardOpen(cardInfo);
     }

     public void CardClose() {
        _adapter.CardClose();
     }

    public void WriteToCard(byte [] buffer){
        _adapter.WriteToCard(buffer);
    }

    public void WriteDataToCard_Generic(byte[] data, writeCallbackListener writeCallback) {
        _writeCallback = null;
        _writeCallback = writeCallback;

        byte[] payload = composeBLEPacket_Generic(data);
        WriteToCard(payload);
    }
    public void WriteDataToCard_GenericTracker(byte[] data, writeCallbackListener writeCallback) {
        _writeCallback = null;
        _writeCallback = writeCallback;

        byte[] payload = composeBLEPacket_GenericTracker(data);
        Log.d("INITPAY",  bytesToHex(payload));
        WriteToCard(payload);
    }
    public void WriteDataToCard_Generic(byte[] data) {
        byte[] payload = composeBLEPacket_Generic(data);
        WriteToCard(payload);
    }

    public void WriteDataToCard(byte[] data, EtherEncHeader encHeader, writeCallbackListener writeCallback) {
        _writeCallback = null;
        _writeCallback = writeCallback;

        byte[] payload = composeBLEPacket(data, encHeader);
        WriteToCard(payload);
    }

    private byte[] composeBLEPacket(byte[] data, EtherEncHeader encHeader) {
        byte[] payload = data;
        byte[] packetHeader = getInitedPacked(EthernomConstKt.getPSD_MGR_PORT(), payload.length, encHeader != null ? true : false);
        if (encHeader != null) {
            encHeader.SetPayloadLength(payload.length);
            byte[] epacket = encHeader.GetHeaderBuffer();
            byte[] temp = concatBytesArray(packetHeader, epacket);
            packetHeader = temp;
        }
        byte[] temp2 = concatBytesArray(packetHeader, payload);
        packetHeader = temp2;
        return packetHeader;
    }

    private byte[] getInitedPacked(byte appPort, int payloadLength, boolean useEncryption) {
        int encPayloadLength = payloadLength + 16;
        return MakeTransportHeader((byte) (appPort | 0x80), appPort, useEncryption ? (byte) 0x80 : (byte) 0x00, EthernomConstKt.getETH_BLE_INTERFACE(), encPayloadLength, (byte) 0x00);
    }


    private byte[] composeBLEPacket_Generic(byte[] data) {
        byte[] payload = data;
        byte[] packetHeader = getInitedPacket_Generic(payload.length);
        byte[] temp = concatBytesArray(packetHeader, payload);
        packetHeader = temp;
        return packetHeader;
    }

    private byte[] composeBLEPacket_GenericTracker(byte[] data) {
        byte[] payload = data;
        byte[] packetHeader = getInitedPacket_GenericBLE(payload.length);
        byte[] temp = concatBytesArray(packetHeader, payload);
        packetHeader = temp;
        return packetHeader;
    }

    public byte[] concatBytesArray(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
    private byte[] getInitedPacket_Generic(int payloadLength) {
        return MakeTransportHeader((byte) (EthernomConstKt.getGENERIC_PORT() | 0x80), EthernomConstKt.getGENERIC_PORT(), (byte) 0x00, EthernomConstKt.getETH_BLE_INTERFACE(), payloadLength, (byte) 0x00);
    }
    private byte[] getInitedPacket_GenericBLE(int payloadLength) {
        return MakeTransportHeader((byte) (0x93), (byte) 0x13, (byte) 0x00, EthernomConstKt.getETH_BLE_INTERFACE(), payloadLength, (byte) 0x00);
    }

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
        int i = 0;
        for (int j = 1; j != 7; j++) {
            int c = packet[j];
            xorValue = xorValue ^ c;
        }

        packet[7] = (byte) xorValue;
        return packet;
    }

    public void DoStartCardAuthentication(byte appID){
        //Log.i("TEST_EtherSecureSession", "DoStartCardAuthentication");

        byte[] payload = new byte[5];
        payload[0] = EthernomConstKt.getCM_INIT_APP_PERM();
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = 0;
        payload[4] = appID;

        WriteDataToCard_Generic(payload, buffer -> {
            if(buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_AUTHENTICATE()){
                byte[] challenge = new byte[32];
                for(int i=0; i<32; i++) challenge[i] = buffer[i + 12];
                generate_auth_rsp(challenge);
            }else{
                //Log.i("TEST_EtherSecureSession", "Bad DH sequence from card");
                RequestAppSuspend((byte) 0x01);
            }
        });
    }

    private void generate_auth_rsp(byte[] challenge){
        String pubKey = TrackerSharePreference.getConstant(mContext).getPublicKey();
        String privKey = TrackerSharePreference.getConstant(mContext).getPrivateKey();
        List<Byte> responseBytes = _ecdh.generate_auth_rsp(challenge, pubKey, privKey);

        byte[] temp = new byte[responseBytes.size()];
        for(int i=0; i<responseBytes.size(); i++) temp[i] = responseBytes.get(i);

        Log.i("TEST_EtherSecureSession", "WRITING Auth_rsp");
        WriteDataToCard_Generic(temp, buffer -> {
            if(buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_RSP()){
                if(buffer[12] == EthernomConstKt.getCM_ERR_SUCCESS()){
                    Log.i("TEST_EtherSecureSession", "Auth success");
                    if(_etherSecureSessionListener != null) _etherSecureSessionListener.onAuthenticated(0);
                    RequestAppLaunch(android.os.Build.MODEL, (byte) 0x01);
                }else{
                    Log.i("TEST_EtherSecureSession", "Auth fails");
                    if(_etherSecureSessionListener != null) _etherSecureSessionListener.onAuthenticated(-2);
                    RequestAppSuspend((byte) 0x01);

                }
            }else{
                Log.i("TEST_EtherSecureSession", "Invalid command");
                if(_etherSecureSessionListener != null) _etherSecureSessionListener.onAuthenticated(-1);
                RequestAppSuspend((byte) 0x01);

            }
        });
    }

    private void handle_receives_packet(byte[] value) {
         Log.d("CCCCCC", (value[0] == 0x13) +"");
         if (_temp_buffer.size() == 0) {

            int len = get_payload_length(value[4], value[5]);
            if (len == (value.length - 8)) {
                on_card_read_success(value);
                _temp_buffer.clear();
            } else {
                for (int i = 0; i < value.length; i++) {
                    _temp_buffer.add(value[i]);
                }
            }
        } else {
            for (int i = 0; i < value.length; i++) {
                _temp_buffer.add(value[i]);
            }

            int initial_len = get_payload_length(_temp_buffer.get(4), _temp_buffer.get(5));
            if (initial_len == (_temp_buffer.size() - 8)) {
                byte[] temp_buffer = new byte[_temp_buffer.size()];
                for (int i = 0; i < _temp_buffer.size(); i++) temp_buffer[i] = _temp_buffer.get(i);

                on_card_read_success(temp_buffer);
                _temp_buffer.clear();
            }
        }
    }

    private void on_card_read_success(byte[] value){
        Log.i("TEST_ETHBLEClient: READ", hexaSpaced(value));
        if(_writeCallback != null) {
            writeCallbackListener _tempWriteCallback = _writeCallback;
            _writeCallback = null;
            _tempWriteCallback.onData(value);

        }
    }
    private int get_payload_length(int LSB, int MSB) {
        int len = ((MSB & 0xFF) * 256) + (LSB & 0xFF);
        if (len < 0) {
            len = len & 0xFFFF;
        }
        return len;
    }



    public void RequestAppLaunch(String host_name, byte appID){
        _host_name = host_name;

        byte[] payload = new byte[5];
        payload[0] = EthernomConstKt.getCM_LAUNCH_APP();
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = 0;
        payload[4] = appID;

        WriteDataToCard_Generic(payload, buffer -> {
            if(buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_RSP()) {
                if (buffer[12] == EthernomConstKt.getCM_ERR_SUCCESS()) {
                    Log.i("TEST_EtherSecureSession", "App launched success");
                    requestSessionPIN();

                }else if(buffer[12] == EthernomConstKt.getCM_ERR_APP_BUSY()){
                    Log.i("TEST_EtherSecureSession", "App launched busy");

                    RequestAppSuspend(appID);
                }else{
                    Log.i("TEST_EtherSecureSession", "App launched fails");
                    RequestAppSuspend((byte) 0x01);
                }
            }else{
                Log.i("TEST_EtherSecureSession", "Invalid command: Launch app state");
                RequestAppSuspend((byte) 0x01);
            }
        });
    }

    public void requestSessionPIN() {
        Log.i("TEST_EtherSecureSession","requestSessionPIN");

        List<Byte> payload = new ArrayList<Byte>();
        JSONObject session_pin = null;
        String PIN = "";

        Integer pin_len = TrackerSharePreference.getConstant(mContext).getPinLength();
        byte[] hname = convertToByte(_host_name, 15);

            payload.add(EthernomConstKt.getCM_SESSION_REQUEST());
            payload.add((byte)0x00);
            payload.add((byte)19);
            payload.add((byte)0x00);
            payload.add((byte) 0x01);

            for(int i=0;i<hname.length; i++) payload.add(hname[i]);
            payload.add((byte)0x00);
            payload.add(pin_len.byteValue());
            payload.add((byte)0x00);

        byte[] temp = new byte[payload.size()];
        for(int i=0;i<payload.size();i++) temp[i] = payload.get(i);
        WriteDataToCard_Generic(temp, buffer -> {
            if(buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_SESSION_RSP()) {
                byte[] slice = Arrays.copyOfRange(buffer, 12, buffer.length);
                String new_PIN = convertToString(slice);
                Log.i("TEST_EtherSecurePIN", "Success" + new_PIN);
                //requestBLETrackerInit(new_PIN);
                if(_etherSecureSessionListener != null) {

                    _etherSecureSessionListener.onPINRequest(new_PIN, 1);
                }

            }else if(buffer[EthernomConstKt.getETH_BLE_HEADER_SIZE()] == EthernomConstKt.getCM_RSP()) {
                try {
                    String PIN2 = session_pin.getString("session");
                    if(_etherSecureSessionListener != null) {
                        Log.i("TEST_EtherSecurePIN", "Success" + PIN2);
                        RequestAppSuspend((byte) 0x01);

                        _etherSecureSessionListener.onPINRequest(PIN2, 0);
                    }

                }catch (JSONException e) {
                    if(_etherSecureSessionListener != null) _etherSecureSessionListener.onAppLaunched(-1);
                    RequestAppSuspend((byte) 0x01);
                }
            }else{
                if(_etherSecureSessionListener != null) _etherSecureSessionListener.onAppLaunched(-1);
                RequestAppSuspend((byte) 0x01);
            }
        });
    }
    public void requestBLETrackerInit(){
        Log.i("TEST_EtherSecureSession","requestBLETrkInit");

        //_pin = PIN;
        byte[] payload = convertToByte((byte) 0x81, new String[]{_host_name});
        WriteDataToCard_GenericTracker(payload, buffer -> {
            Log.i("TEST_EtherSecureSesTrx", bytesToHex(buffer));
            String data = bytesToHex(buffer);
            String result = data.substring(data.length() -8);
            RequestAppSuspend((byte) 0x01);
            _etherSecureSessionListener.onRegisterSuccess(result);
        });
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void RequestAppSuspend(byte appID){
        byte[] payload = new byte[5];
        payload[0] = EthernomConstKt.getCM_SUSPEND_APP();
        payload[1] = 0;
        payload[2] = 1;
        payload[3] = 0;
        payload[4] = appID;

        WriteDataToCard_Generic(payload);
    }



    private String convertToString(byte[] payload){
        String value = "";

        for(int i=0; i<payload.length; i++){
            value += (char) payload[i];
        }
        return value;
    }

    private byte[] convertToByte(String value, Integer num){
        List<Byte> payload = new ArrayList<Byte>();

        for(int i=0; i<num; i++){
            if(i < value.length()) {
                payload.add((byte) value.charAt(i));
            }else{
                payload.add((byte) 0x00);
            }
        }

        byte[] temp = new byte[payload.size()];
        for(int k=0; k<payload.size(); k++) temp[k] = payload.get(k);
        return temp;
    }

    private byte[] convertToByte(byte cmd, String[] value){
        List<Byte> payload = new ArrayList<Byte>();
        payload.add(cmd);

        if(value.length == 0){
            payload.add((byte)0x00);
        }else{
            for(int i=0; i<value.length; i++){
                for(int j=0; j<value[i].length(); j++){
                    payload.add((byte)value[i].charAt(j));
                }
                if(i < value.length - 1){
                    payload.add(EthernomConstKt.getDELIMITER());
                }
            }
            payload.add((byte)0x00);
        }

        byte[] temp = new byte[payload.size()];
        for(int k=0; k<payload.size(); k++) temp[k] = payload.get(k);
        return temp;
    }





    /* CARD EVENTS, these are coming from the layer BELOW the API not to be confused with Events
       we sent to the app via the api.  We accept events from the card here and transform them to
       events to the app
    */
    public void SetupCardListeners(EtherCommAdapter adapter){

        _adapter.SetCardEventListener(new CardEventListener() {
            @Override
            public void onCardOpenFail(int resultCode, int hwSpecificError) {
                Log.d(TAG, "onCardOpenFail");

            }


            @Override
            public void onCardConnectionDropped(int resultCode) {
                Log.d(TAG, "onCardConnectionDropped");
            }

            @Override
            public void onCardOpenSuccess(int resultCode) {
                // the card was opened
                // inform the app side
                Log.d(TAG, "onCardOpenSuccess");
                DoStartCardAuthentication((byte) 0x01);

            }

            @Override
            public void onCardClosedByCard(int resultCode) {
                Log.d(TAG, "onCardClosedByCard");

            }

            @Override
            public void onCardClosedFail(int resultCode, int hwSpecificError) {
                Log.d(TAG, "onCardClosedFail");

            }

            @Override
            public void onCardClosedSucess(int resultCode) {
                Log.d(TAG, "onCardClosedSucess");


            }

            @Override
            public void onReadFromCardFail(int resultCode, int hwSpecificError) {
                Log.d(TAG, "onReadFromCardFail");

            }

            @Override
            public void onReadFromCardSuccess(int resultCode, byte[] buffer) {
                // we are coming in here with in theory a full transport layer buffer
                Log.d(TAG, "read from card success");
                Log.d("INITSS", bytesToHex(buffer));
                handle_receives_packet(buffer);
            }

            @Override
            public void onWriteToCardFail(int resultCode, int hwSpecificError) {
                Log.d(TAG, "write to card failed");

            }

            @Override
            public void onWriteToCardSuccess(int resultCode) {
                Log.d(TAG, "write to card succeeded");

            }
        });
    }


}
