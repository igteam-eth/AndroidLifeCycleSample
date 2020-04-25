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
val CM_GET_MENU:Byte = 0x8A.toByte()


//card to host
val CM_RSP: Byte = 0x01
val CM_AUTHENTICATE: Byte = 0x08
val CM_SESSION_RSP: Byte = 0x0B

//response type
val CM_ERR_SUCCESS: Byte = 0x00.toByte()
val CM_ERR_APP_BUSY: Byte = 0x09.toByte()

