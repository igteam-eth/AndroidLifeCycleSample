package com.ethernom.helloworld.LinkLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.util.Arrays;

import static com.ethernom.helloworld.LinkLayer.Constant.EtherError.ETH_FAIL;
import static com.ethernom.helloworld.LinkLayer.Constant.EtherError.ETH_SUCCESS;


public class EtherBTAdapter extends EtherCommAdapter {
    static String TAG = "EtherBTAdapter";

    static UUID ETH_serviceUUID = UUID.fromString("19490001-5537-4F5E-99CA-290F4FBFF142");
    static UUID ETH_characteristicUUID = UUID.fromString("19490002-5537-4F5E-99CA-290F4FBFF142");

    private String _adapterAddress;
    private Context _context;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice periphEthCard;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic ethCharacteristic;
    private int maxByteSize = 244;

    // IETHERCOMADAPTER

    // these need to set the link layer specific address of the BLE adapter, COM/USB adapter or NFC
    // as appropriate to the link layer in question
    public String GetAdapterAddress() {
        return _adapterAddress;
    }

    public void SetAdapterAddress(String adapterAddress) {
        _adapterAddress = adapterAddress;
    }

    public void CardClose(){
        if(gatt != null)
            gatt.close();
        if (CardEventListener != null)
            CardEventListener.onCardClosedSucess(ETH_SUCCESS);
    }
    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }


    public void CardOpen(CardInfo cardInfo){
        Log.i(TAG, "found specific ethernom card");
        periphEthCard = mBluetoothAdapter.getRemoteDevice(cardInfo.GetDeviceID());
        gatt = periphEthCard.connectGatt(_context, false, gattCallback);
    }

    BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    if (newState == BluetoothGatt.STATE_CONNECTED) {
                        Log.i(TAG, "gatt connected, discover services");
                        gatt.discoverServices();
                    }
                    if(newState == BluetoothGatt.STATE_DISCONNECTED){
                        Log.i(TAG,"gat disconnected");
                        if(CardEventListener != null) {
                            //CardEventListener.onCardConnectionDropped(status);
                            CardEventListener.onCardConnectionDropped(ETH_SUCCESS);
							gatt.close();
                        }
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    ethCharacteristic = gatt.getService(ETH_serviceUUID).getCharacteristic(ETH_characteristicUUID);
                    UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = convertFromInteger(0x2902);
                    if (ethCharacteristic != null) {
                        Log.i(TAG, "onServicesDiscovered, ethernom characteristic found");
                        BluetoothGattDescriptor descriptor = ethCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                        if (descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.setCharacteristicNotification(ethCharacteristic, true);
                            Log.i(TAG, "onServicesDiscovered, gatt write descriptor");
                            gatt.writeDescriptor(descriptor);
                        }
                        //gatt.setCharacteristicNotification(ethCharacteristic, true);
                    }
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    Log.i(TAG, "onDescriptorWrite, gatt write descriptor: "+Integer.toString(status));
                    if (CardEventListener != null) {
                        Log.i(TAG, "onDescriptorWrite, fire listener");

                        if (status == 0)
                            CardEventListener.onCardOpenSuccess(ETH_SUCCESS);
                        else
                            CardEventListener.onCardOpenFail(ETH_FAIL, status);
                    }
                }

                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt,
                                                  BluetoothGattCharacteristic characteristic,
                                                  int status) {
                    if (CardEventListener != null) {
                        if (status != 0) {
                            CardEventListener.onWriteToCardFail(ETH_FAIL, status);
                            Log.i(TAG, "oncharacteristicwrite failed write");
                        } else {
                            CardEventListener.onWriteToCardSuccess(ETH_SUCCESS);
                            Log.i("EtherBLE", "oncharacteristicwrite success");
                        }
                    }
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (status != 0)
                        Log.i("EtherBLE", "oncharacteristicread failed write");
                    else
                        Log.i("EtherBLE", "oncharacteristicread success");

                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    byte[] value = characteristic.getValue();
                    if (CardEventListener != null) {
                        CardEventListener.onReadFromCardSuccess(ETH_SUCCESS, value);
                    } else {
                        CardEventListener.onReadFromCardFail(ETH_FAIL, 404);
                    }
                };
            };

    public void Init(Context context){
        final BluetoothManager bluetoothManager =
                (BluetoothManager) (context.getSystemService(Context.BLUETOOTH_SERVICE));
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }



    // a way to write byte buffers
    public void WriteToCard(byte[] data) {
        //ethCharacteristic.setValue(transportPacket);
        //gatt.writeCharacteristic(ethCharacteristic);

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
                    //callback.invoke("Write failed");
                }

                if (!writeError) {
                    Thread.sleep(10);
                    for (byte[] message : splittedMessage) {
                        if (!doWrite(message)) {
                            writeError = true;
                            //callback.invoke("Write failed");
                            break;
                        }
                        Thread.sleep(10);
                    }

                    if (!writeError) {
                        //callback.invoke("Write success");
                    }
                }
            } catch (InterruptedException e) {
                    //callback.invoke("Write failed");
            }

        }else{
            ethCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            if(!doWrite(data)){
                //callback.invoke("Write failed");
            };
        }

    }

    public boolean doWrite(byte[] data){
        ethCharacteristic.setValue(data);

        if (!gatt.writeCharacteristic(ethCharacteristic)) {
            Log.d("BLE_MANAGER: WRITE", "Error on doWrite");
            return false;
        }
        return true;
    }


        private int[] toUnsignedIntArray(byte[] barray) {

            int[] ret = new int[barray.length];
            for (int i = 0; i < barray.length; i++) {
                ret[i] = barray[i] & 0xff; // Range 0 to 255, not -128 to 127
            }
            return ret;

        }

}
