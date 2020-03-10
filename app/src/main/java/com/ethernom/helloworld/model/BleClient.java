package com.ethernom.helloworld.model;
public class BleClient {
    private String id,uuid,macAddress,devName,status,rssi, deviceSN;
    public BleClient(String id, String uuid, String MacAddress, String DevName, String rssi, String Status, String deviceSN) {
        this.id = id;this.uuid = uuid;this.macAddress = MacAddress;this.devName = DevName;this.status = Status;this.rssi = rssi;this.deviceSN = deviceSN;
    }
   public String getId() { return this.id; }
   public void setId(String Id) { this.id = Id; }
   public String getUuid() { return this.uuid; }
   public void setUuid(String Uuid) {this.uuid = Uuid;}
   public String getMacAddress() { return  this.macAddress; }
   public void setMacAddress(String MacAddress){ this.macAddress = MacAddress; }
   public String getDevName() { return this.devName; }
   public void setDevName(String DevName) { this.devName = DevName; }
   public String getStatus() { return this.status; }
   public void setStatus(String Status) { this.status = Status; }
   public String getRssi() { return this.rssi; }
   public void setRssi(String Rssi) { this.rssi = Rssi; }
   public String getDeviceSN() { return this.deviceSN;}
   public void setDeviceSN(String deviceSN) { this.deviceSN = deviceSN;}
}