package com.ethernom.helloworld.model;

public class CardInfo {
    String _cardName;
    String _deviceID;
    String _deviceSN;

    public String GetCardName(){return _cardName;}

    public String GetDeviceID(){ return _deviceID;}

    public String GetDeviceSN(){ return _deviceSN;}

    public CardInfo(String name, String manufacturingID, String SN){
        _cardName = name;
        _deviceID = manufacturingID;
        _deviceSN = SN;
    }
}
