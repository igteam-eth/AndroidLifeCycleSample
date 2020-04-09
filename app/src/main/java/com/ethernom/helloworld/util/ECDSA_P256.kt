package com.ethernom.helloworld.util

import android.security.keystore.KeyProperties
import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPrivateKeySpec

val curveName            = "secp256r1"

class ECDSA_P256 {
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

    fun getSignature(challenge: ByteArray, privateKey: PrivateKey): ByteArray {
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(privateKey)
        sig.update(challenge)

        return sig.sign()
    }
    fun getPrivateKeyFromString(privateKey: String): PrivateKey? {
        val keyFactoryPriv = KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC)

        val parameters: AlgorithmParameters =
            AlgorithmParameters.getInstance(KeyProperties.KEY_ALGORITHM_EC)
        parameters.init(ECGenParameterSpec(curveName))

        val ecParameterSpec: ECParameterSpec = parameters.getParameterSpec<ECParameterSpec>(
            ECParameterSpec::class.java
        )

        val ecPrivateKeySpec =
            ECPrivateKeySpec(
                BigInteger(privateKey.hexa()),
                ecParameterSpec
            )

        val privateKey = keyFactoryPriv.generatePrivate(ecPrivateKeySpec)

        println("Private Key: " + privateKey.encoded.hexa())

        return privateKey
    }

    fun generate_auth_rsp(challenge: ByteArray, pubKey: String, privKey: String) : List<Byte>? {

        try {
            val privateKey = getPrivateKeyFromString(privKey)

            privateKey?.let {
                val signature = getSignature(challenge, it)
                val (rawSig, cc) = lunsDerDecoder(signature.toList())
                val payload = arrayListOf<Byte>()

                payload.add(CM_AUTHENTICATE_RSP)
                payload.add(0)
                payload.add(64)
                payload.add(0)

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

    fun String.hexa(): ByteArray? {
        val str = this

        val `val` = ByteArray(str.length / 2)

        for (i in `val`.indices) {
            val index = i * 2
            val j: Int = str.substring(index, index + 2).toInt(16)
            `val`[i] = j.toByte()
        }

        return `val`
    }

    fun ByteArray.hexa(): String? {
        val sb = java.lang.StringBuilder()
        for (b in this) {
            sb.append(String.format("%02x", b))
        }

        return sb.toString()
    }

    fun ByteArray.hexaSpaced(): String? {
        val sb = java.lang.StringBuilder()

        for (b in this) {
            sb.append(String.format("%02x ", b))
        }

        return sb.toString()
    }
}
