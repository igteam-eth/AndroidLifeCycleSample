package com.ethernom.helloworld.LinkLayer.Constant;

public class EtherError {
    static int  DELIMITER   = 31;
    public static int  ETH_HEADER_SIZE = 8;
    public static int  ETH_PAYLOAD_HEAD = ETH_HEADER_SIZE + 1;

    public static int ETH_SUCCESS = 0;
    public static int ETH_FAIL = -1;

    public static int ETH_DISCOVERY_COMPLETE = 0X1001;
    public static int ETH_SCAN_FOREVER       = 0X1002;

    // card errors
    public static int ERR_ETH_CARDSCAN_FAILED = 0x0101;

    // Select errors
    public static int ERR_ETH_SELECT_DENIED = 0x0102;
    public static int ERR_ETH_SELECT_FAILED = 0x0103;
    public static int ERR_ETH_DISCONNECTED = 0x0104;

    // service errors
    public static int ERR_ETH_SERVICE_OPEN_DENIED = 0x0201;

}
