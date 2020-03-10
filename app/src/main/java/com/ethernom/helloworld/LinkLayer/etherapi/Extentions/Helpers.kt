package com.ethernom.android.etherapi.Extentions

import com.ethernom.android.etherapi.EtherTransportProtocol
import com.ethernom.android.etherapi.TRANSPORT_PLUS_ENC_HEADER_SIZE

object Helpers {
    fun ParseEncryptedHeader(payload: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {
        val transportHdr = CopyBytes(
            payload = payload,
            startIdx = 0,
            count = EtherTransportProtocol.ETH_BLE_HEADER_SIZE
        )

        val encHdr = CopyBytes(
            payload = payload,
            startIdx = EtherTransportProtocol.ETH_BLE_HEADER_SIZE,
            count = EtherTransportProtocol.ETH_BLE_ENC_HEADER_SIZE
        )

        val ct = payload.size - 24
        var appPayload = ByteArray(0)

        if (ct != 0) {
            appPayload = CopyBytes(
                payload = payload,
                startIdx = TRANSPORT_PLUS_ENC_HEADER_SIZE.toInt(),
                count = ct
            )
        }

        // no need to do this as payload is decrypted later   transportHdr.add(contentsOf: appPayload)
        return Triple(encHdr, transportHdr, appPayload)
    }

    fun CopyBytes(payload: ByteArray, startIdx: Int, count: Int) :ByteArray {
        var out = ByteArray(0)

        for (i in 0 until count) {
            val idx: Int = startIdx.toInt() + i
            out += payload[idx]
        }

        return out
    }
}