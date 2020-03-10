package com.ethernom.android.etherapi.Crypto

import com.ethernom.android.etherapi.INT8NULL
import java.lang.Exception

val CM_AUTHENTICATE_RSP: Byte = 0x88.toByte()
val FLAG_CONTAIN_ENCRYPTION_HDR = 0x80
val ALLOWED_SEQ_NUM_SKIPS = 4
val APP_FLAG_H2C = 0x80
val APP_FLAG_ENCRYPT = 0x40
//val APP_CMD_MASK = ~APP_FLAG_ENCRYPT
val APP_H2C_KEY_EXCHANGE = APP_FLAG_H2C or 0x01
val APP_H2C_ENCRYPT_START = APP_FLAG_H2C or 0x02
val APP_H2C_MSG = APP_FLAG_H2C or 0x11
val APP_C2H_KEY_EXCHANGE = 0x01
val APP_C2H_ENCRYPT_START = 0x02
val APP_C2H_MSG = 0x11
val APP_ERROR_SEQUENCE = 0x81
val APP_ERROR_AUTH = 0x82
val APP_ERROR_INTERNAL = 0x83
val APP_ERROR_FORMAT = 0x84

class EtherECDH {
    var _authenticating = false

    private fun lunsDerDecoder(der: List<Byte>): Pair<List<Byte>,Boolean> {
        val len_r: Int = der[3].toInt()
        println("r length: $len_r")
        val len_s: Int = (der[5 + (len_r)]).toInt()
        println("s length: $len_s")
        var sig: ArrayList<Byte> = arrayListOf()
        if ((der[0].toInt() != 0x30 || len_r <= 0 || len_r > 33 || len_s <= 0 || len_s > 33 || der[2].toInt() != 0x02 || der[4 + len_r].toInt() != 0x02)) {
            // error
            return Pair(sig, false)
        }
        println(der)
        if (len_r <= 32) {
            val fill = ByteArray(32 - len_r) { 0 }
//            val fill = ArrayList<Byte>(32 - len_r) { 0 }
//            val fill = arrayListOf<Byte>(repeatElement(0, count = 32 - len_r))
            sig.addAll(fill.toList())
            val movePart = der.subList(4, 4 + len_r )
            sig.addAll(movePart)
        } else {
            val movePart =  der.subList(4 + 1, 4 + 33)
            sig.addAll(movePart)
        }
        if ((len_s <= 32)) {
            val movePart = der.subList(6 + len_r, 6 + len_s + len_r )
            sig.addAll(movePart)
        } else {
            val movePart = der.subList(6 + len_r + 1, 6 + len_r + 33)
            sig.addAll(movePart)
        }
        return Pair(sig, true)
    }

    fun generate_auth_rsp(challenge: ByteArray, pubKey: String, privKey: String) : List<Byte>? {

        val eax = Ether_AESEAX()
        try {
            val privateKey = eax.getPrivateKeyFromString(privKey)

            privateKey?.let {
                val signature = eax.getSignature(challenge, it)
                val (rawSig, cc) = lunsDerDecoder(signature.toList())
                val payload = arrayListOf<Byte>()

                payload.add(CM_AUTHENTICATE_RSP)
                payload.add(INT8NULL)
                payload.add(64)
                payload.add(INT8NULL)

                if ((cc)) {
                    for (num in rawSig) {
                        payload.add(num)
                    }
                }

                _authenticating = true

                return payload
            }
        }
        catch (e: Exception) {
            println("Cannot create users private key: $e")
        }

        return null
    }
}
