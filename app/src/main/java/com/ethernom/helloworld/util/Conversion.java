package com.ethernom.helloworld.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Conversion {

    /* That class for concat Byte array of Header & Payload */
    public static byte[] concatBytesArray(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }
    /* For convert integer to UUID*/
    public static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }
    public static String convertToString(byte[] payload){
        String value = "";

        for(int i=0; i<payload.length; i++){
            value += (char) payload[i];
        }
        return value;
    }
    /* Convert from bytes to Hexa */
     final static char[] hexArray = "0123456789ABCDEF".toCharArray();
     /* Convert byte  array to Hex string*/
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    /*Get length of payload*/
    public static int get_payload_length(int LSB, int MSB) {
        int len = ((MSB & 0xFF) * 256) + (LSB & 0xFF);
        if (len < 0) {
            len = len & 0xFFFF;
        }
        return len;
    }
    /* Convert string to bytes array by length limit */
    public static byte[] convertToByte(String value, Integer num){
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
    /*Convert string array with command to byte array */
    public static byte[] convertToByte(byte cmd, String[] value){
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
    /*Convert hex to ASCII*/
    public static String convertHexToAscII(String hex) {
        if(hex.length()%2!=0){
            System.err.println("Invlid hex string.");
            return "";
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < hex.length(); i = i + 2) {
            // Step-1 Split the hex string into two character group
            String s = hex.substring(i, i + 2);
            // Step-2 Convert the each character group into integer using valueOf method
            int n = Integer.valueOf(s, 16);
            // Step-3 Cast the integer value to char
            builder.append((char)n);
        }
        return String.valueOf(builder);
    }
}
