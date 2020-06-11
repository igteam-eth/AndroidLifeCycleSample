package com.ethernom.helloworld.util

val ETH_BLE_HEADER_SIZE = 8
// hack remove when we completely get rid of the extension ble module
val DELIMITER: Byte = 0x1F
//ENC
val CM_AUTHENTICATE_RSP = 0x88.toByte()

val CM_LAUNCH_APP: Byte = 0x81.toByte()
val CM_SUSPEND_APP: Byte = 0x82.toByte()
val CM_INIT_APP_PERM: Byte = 0x87.toByte()
val CM_SESSION_REQUEST: Byte = 0x8B.toByte()
val CM_GET_SN:Byte = 0x8B.toByte()
val CM_GET_SN_RSP:Byte = 0x0B.toByte()
val CM_GET_MENU:Byte = 0x8A.toByte()
val CM_GET_MENU_RSP:Byte = 0x0A.toByte()


//card to host
val CM_RSP: Byte = 0x01
val CM_AUTHENTICATE: Byte = 0x08
val CM_SESSION_RSP: Byte = 0x0B

//response type
val CM_ERR_SUCCESS: Byte = 0x00.toByte()
val CM_ERR_APP_BUSY: Byte = 0x09.toByte()

// check version
val CMD_VERSION_CHECK: Byte = 0x81.toByte()
val CMD_VERSION_RSP: Byte = 0x01.toByte()
val CMD_BLE_VERSION_CHECK: Byte = 0x88.toByte()
val CMD_BLE_VERSION_RSP: Byte = 0x08.toByte()
val CMD_BOOT2_VERSION_CHECK: Byte = 0x91.toByte()
val CMD_BOOT2_VERSION_RSP: Byte = 0x11.toByte()

//14 94 00 02 0C 00 00 8E 0B 0008005F03000002000100
//14 94 00 02 10 00 00 92 11 000C00763131000000000000000000
//14 94 00 02 15 00 00 97 0A 0011006131393130303430323631350000000000

