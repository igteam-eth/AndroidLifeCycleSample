package com.ethernom.helloworld.service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BLEScan {
    private BluetoothLeScanner btScanner;
    private DeviceDiscoveredCallBack mDeviceDiscoverCallback;
    public BLEScan(Context context, DeviceDiscoveredCallBack mDeviceDiscoverCallback) {
        this.mDeviceDiscoverCallback = mDeviceDiscoverCallback;
        BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        assert btManager != null;
        BluetoothAdapter btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();
    }
    public void startScanning() {
        List<ScanFilter> scanFilters = new ArrayList<>();
        final ScanSettings settings = new ScanSettings.Builder().build();
        String ethernomUUIDServiceDiscover = "19490016-5537-4f5e-99ca-290f4fbff142";
        ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(ethernomUUIDServiceDiscover)).build();
        scanFilters.add(scanFilter);
        btScanner.startScan(scanFilters,settings,leScanCallback);
    }
    public void stopScanning() {
        btScanner.stopScan(leScanCallback);
    }
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            if(result.getScanRecord().getManufacturerSpecificData() == null) return;

            if(Objects.requireNonNull(result.getScanRecord()).getServiceUuids() != null) {
                SparseArray<byte[]> mdata = result.getScanRecord().getManufacturerSpecificData();
                String deviceSN = "";
                for (int i = 0; i < mdata.size(); i++) {
                    byte[] mfdata = mdata.get(mdata.keyAt(i));
                    if (mfdata == null) return;

                    int[] manufacturerData = toUnsignedIntArray(mfdata);

                    for (int k = 0; k < manufacturerData.length-1; k++) {
                        //deviceSN += Integer.toHexString(manufacturerData[k]);
                        deviceSN += String.format("%02x", manufacturerData[k]);
                    }


                }
//                deviceSN = Conversion.conversion_sn(deviceSN);
                mDeviceDiscoverCallback.DeviceDiscover(result.getDevice().getName(), result.getScanRecord().getServiceUuids().get(0).toString(), result.getDevice().getAddress(), Integer.toString(result.getRssi()), deviceSN);
            }
        }
    };
    public interface  DeviceDiscoveredCallBack {
        void DeviceDiscover(String deviceName, String uuid, String macadd, String rssi, String SNDevice);
    }

    private int[] toUnsignedIntArray(byte[] barray) {
        int[] ret = new int[barray.length];
        for (int i = 0; i < barray.length; i++) {
            ret[i] = barray[i] & 0xff; // Range 0 to 255, not -128 to 127
        }
        return ret;
    }



}
