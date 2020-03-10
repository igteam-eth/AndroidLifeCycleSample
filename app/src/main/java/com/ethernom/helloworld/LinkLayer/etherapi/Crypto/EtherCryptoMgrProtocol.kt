package com.ethernom.android.etherapi.Crypto

interface EtherCryptoMgrProtocol {
    fun GetPublicHostKey() : ByteArray
    fun SetCardPublicKey(cardPublicKeyBytes: ByteArray)
    fun InitializeRandomSequence()
    fun GetNextSequence() : Int
    fun EncryptByteArray(encPayload// AEAD
                    // return a buffer with nonce data specific to the impl
                    // finally encrypt/decrypt packets
                    : ByteArray) : ByteArray?
    fun DecryptByteArray(encHeader: ByteArray, encAppData: ByteArray) : ByteArray?
}
