package com.ethernom.android.etherapi

import kotlin.experimental.or

class EtherTransportProtocol {
    companion object {
        var DELIMITER: Byte = 31
        var ETH_BLE_HEADER_SIZE: Int = 8
        var ETH_BLE_PAYLOAD_HEAD = ETH_BLE_HEADER_SIZE + 1
        var ETH_BLE_ENC_HEADER_SIZE = 16
    }

    var _servicePort: Byte
    var _destPort: Byte
    var _generic_servicePort: Byte = 22
    var _downloader_servicePort: Byte = 20
    var _messageProtocol: TMessageProtocol
    var control: Byte = 0x0

    constructor(servicePort: Byte, messageProtocol: TMessageProtocol) {
        this._servicePort = servicePort
        this._destPort = servicePort
        this._messageProtocol = messageProtocol
    }

    fun MakeTransportHeader(srcport: Byte, destprt: Byte, control: Byte, interfaceKind: Byte, payloadLength: Int, protocol: Byte): List<Byte> {
        var packet = arrayListOf<Byte>()
        packet.add(srcport)
        packet.add(destprt)
        packet.add(control)
        packet.add(interfaceKind)

        val ByteValue0 = (payloadLength and 0x00ff).toByte()
        val ByteValue1 = (payloadLength shr 8).toByte()

        packet.add(ByteValue0)
        packet.add(ByteValue1)
        packet.add(INT8NULL)

        val checksum = GetTransportMessageCheckSum(packet.toByteArray())
        packet.add(checksum)

        return packet
    }

    fun GetServicePort(): Byte = _servicePort

    fun GetGenericServicePort(): Byte = _generic_servicePort

    fun GetDLServicePort(): Byte = _downloader_servicePort

    fun GetApplicationData(data: ByteArray): ByteArray {
        val len = data.size - EtherTransportProtocol.ETH_BLE_HEADER_SIZE
        val start = EtherTransportProtocol.ETH_BLE_HEADER_SIZE

        if (start + len <= data.size) {
            var sliceArray = ByteArray(0)

            for (index in start until start + len) {
                sliceArray += data[index]
            }

            return sliceArray
        }

        return ByteArray(0)
    }

    fun GetTransportMessageCheckSum(packet: ByteArray): Byte {
        var xorValue = packet[0].toInt()
        // xor the packet header for checksum
        val i = 0

        for (j in 1..6) {
            val c = packet[j].toInt()
            xorValue = xorValue xor c
        }

        return xorValue.toByte()
    }

    //*******************************************************************
    //*********************APP WRITE**************************************
    //*******************************************************************
    fun getTransportHeader(payloadLength: Int): List<Byte> {
        val Value0: Byte = (payloadLength and 0x00ff).toByte()
        val Value1: Byte = (payloadLength shr 8).toByte()
        var packet = arrayListOf(
                _servicePort or 0x80.toByte(),
                _servicePort,
                control,
                0x02,
                Value0,
                Value1,
                _messageProtocol.rawValue,
                0
        )

        packet[7] = GetTransportMessageCheckSum(packet.toByteArray())

        return packet
    }

    fun makeTransportPacket(payload: List<Byte>): List<Byte> {
        val packetHeader: List<Byte> = getTransportHeader(payloadLength = payload.size)
        return packetHeader + payload
    }

    // PAYLOADS REQUIRING JUST A COMMAND
    fun GetPayload(cmd: Int): List<Byte> {
        // Construct payload as series of delimited strings
        val payload: List<Byte> = listOf(
                cmd.toByte(),
                0.toByte()
        )

        return makeTransportPacket(payload)
    }

    // PAYLOADS WITH A COMMAND BYTE
    fun GetPayload(cmd: Byte, data: List<Byte>, log: String? = null): List<Byte> {
        val items = arrayListOf(cmd)
        items.addAll(data)

        return items.toList()
    }


    // STRING PAYLOAD
    fun GetPayload(data: String): List<Byte> =
            GetPayload(data.toByteArray(Charsets.UTF_8).toList())

    // STRING WITH COMMAND BYTE
    fun GetPayload(cmd: Byte, data: String): List<Byte> =
            GetPayload(
                    cmd = cmd,
                    data = data.toByteArray(Charsets.UTF_8).toList()
            )

    // RAW BYTE PAYLOADS JUST IN CASE
    fun GetPayload(data: List<Byte>): List<Byte> =
            makeTransportPacket(data)

    // MAJOR USE CASE, PAYLOAD BASED ON A COMMAND AND A STRING ARRAY
    fun GetPayload(cmd: Byte, data: List<String>): List<Byte> {
        var payload = arrayListOf(cmd)

        if (data.isEmpty()) {
            payload.add(0.toByte())
        } else {
            var cc: Int = 0

            for (item in data) {
                payload.addAll(item.toByteArray(Charsets.UTF_8).toList())

                if (cc < data.size - 1) {
                    payload.add(DELIMITER)
                }

                cc += 1
            }

            payload.add(0.toByte())
        }

        return makeTransportPacket(payload = payload)
    }

    //*******************************************************************
    //*********************DL WRITE**************************************
    //*******************************************************************
    fun getTransportHeader_DL(payloadLength: Int): ByteArray {
        val Value0 = (payloadLength and 0x00ff).toByte()
        val Value1 = (payloadLength shr 8).toByte()
        var packet = ByteArray(0)
        packet += _downloader_servicePort or 0x80.toByte()
        packet += _downloader_servicePort
        packet += control
        packet += 0x2
        packet += Value0
        packet += Value1
        packet += _messageProtocol.rawValue
        packet += 0

        packet[7] = GetTransportMessageCheckSum(packet)

        return packet
    }

    fun makeTransportPacket_DL(payload: ByteArray): ByteArray {
        val packetHeader = getTransportHeader_DL(payloadLength = payload.size)
        return packetHeader + payload
    }

    fun GetPayload_DL(cmd: Int): ByteArray {
        // Construct payload as series of delimited strings
        var payload = ByteArray(0)
        payload += cmd.toByte()
        payload += 0.toByte()
        return makeTransportPacket_DL(payload)
    }

    fun GetPayload_DL(cmd: Byte, data: ByteArray): ByteArray {
        var ba = ByteArray(0)
        ba += cmd
        ba += data
        return ba
    }

    fun GetPayload_DL(data: String) : ByteArray =
            GetPayload_DL(data.toByteArray(Charsets.UTF_8))

    fun GetPayload_DL(cmd: Byte, data: String) : ByteArray =
            GetPayload_DL(
                    cmd = cmd,
                    data = data.toByteArray(Charsets.UTF_8)
            )

    // RAW BYTE PAYLOADS JUST IN CASE
    fun GetPayload_DL(data: ByteArray) : ByteArray =
            makeTransportPacket_DL(data)

    fun GetPayload_DL(cmd: Byte, data: List<String>, log: String? = null) :ByteArray {
        var payload = ByteArray(0)

        if (data.isEmpty()) {
            payload += 0.toByte()
        } else {
            var cc: Int = 0

            for (item in data) {
                payload += item.toByteArray(Charsets.UTF_8)

                if (cc < data.size - 1) {
                    payload += DELIMITER
                }

                cc += 1
            }

            payload += 0.toByte()
        }

        return makeTransportPacket_DL(payload)
    }

    //*******************************************************************
    //****************GENERIC WRITE**************************************
    //*******************************************************************
    fun getTransportHeader_Generic(payloadLength: Int) : List<Byte> {
        val Value0 = (payloadLength and 0x00ff).toByte()
        val Value1 = (payloadLength shr 8).toByte()

        var packet = arrayListOf(
                _generic_servicePort or 0x80.toByte(),
                _generic_servicePort,
                control,
                0x02,
                Value0,
                Value1,
                _messageProtocol.rawValue,
                0
        )

        packet[7] = GetTransportMessageCheckSum(packet.toByteArray())

        return packet
    }

    fun makeTransportPacket_Generic(payload: List<Byte>) : List<Byte> {
        val packetHeader: List<Byte> = getTransportHeader_Generic(payloadLength = payload.size)
        return packetHeader + payload
    }

    fun GetPayload_Generic(cmd: Int) : List<Byte> {
        // Construct payload as series of delimited strings
        val payload: List<Byte> = listOf(cmd.toByte(), 0.toByte())
        return makeTransportPacket_Generic(payload = payload)
    }

    fun GetPayload_Generic(cmd: Byte, data: List<Byte>) : List<Byte> =
            listOf(cmd) + data

    fun GetPayload_Generic(data: String) : List<Byte> =
            GetPayload_Generic(data.toByteArray(Charsets.UTF_8).toList())

    fun GetPayload_Generic(cmd: Byte, data: String) : List<Byte> =
            GetPayload_Generic(
                    cmd = cmd,
                    data = data.toByteArray(Charsets.UTF_8).toList()
            )

    // RAW BYTE PAYLOADS JUST IN CASE
    fun GetPayload_Generic(data: List<Byte>) : List<Byte> =
            makeTransportPacket_Generic(payload = data)

    fun GetPayload_Generic(cmd: Byte, data: List<String>, log: String? = null) : List<Byte> {
        var payload = arrayListOf(cmd)

        if (data.size == 0) {
            payload.add(0.toByte())
        } else {
            var cc: Int = 0

            for (item in data) {
                payload.addAll(item.toByteArray(Charsets.UTF_8).toList())

                if (cc < data.size - 1) {
                    payload.add(DELIMITER)
                }

                cc += 1
            }

            payload.add(0.toByte())
        }
        return makeTransportPacket_Generic(payload = payload)
    }
}
