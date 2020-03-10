package com.ethernom.android.etherapi.Crypto

import android.security.keystore.KeyProperties
import com.ethernom.android.etherapi.EtherEncHeader
import com.ethernom.android.etherapi.Extentions.Helpers.CopyBytes
import com.ethernom.android.etherapi.Extentions.hexa
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.modes.EAXBlockCipher
import org.bouncycastle.crypto.params.AEADParameters
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import java.math.BigInteger
import java.security.*
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.spec.*
import java.util.*
import javax.crypto.KeyAgreement
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random


class Ether_AESEAX: EtherCryptoMgrProtocol {

     val curveName            = "secp256r1"
     val NONCE_SIZE_BYTES      = 16
     val AES_MAC_SIZE_BITS    = 64
     val AES_KEY_SIZE         = 16;

    var _publicKey: PublicKey? = null
    var _rawPublicKeyBytes = ByteArray(0)
    var _privateKey: PrivateKey? = null
    var _rawPrivateKey = ByteArray(0)
    var _cardPublicKey: PublicKey? = null
    var _rawCardPublicKey = ByteArray(0)
    var _sessionKey = ByteArray(0)
    var _secretKeySpec : SecretKeySpec? = null
    val _macLength = 8

    fun canary() {
        return
//        val hashstring = "c54976dcc31f669581bf9b077931a06e25fff47613ec287e178f275dc858fc3d353134313535"
//        val s256val = sha256(decodeHexString(hashstring)!!)
//        if (s256val != "af5973c3dc1c99fbb009814eb7fa5e98b1a6bb73fc7ecf0fb52137635c2e7c49"){
//            println("canary error")
//            println(s256val)
//            println("af5973c3dc1c99fbb009814eb7fa5e98b1a6bb73fc7ecf0fb52137635c2e7c49")
//        }
        ///       val bigkey = s256val!!.toByteArray(Charsets.UTF_8)
    }

    constructor() {
  //      Security.addProvider(BouncyCastleProvider())
        canary()
        _publicKey = null
        _privateKey = null
        _cardPublicKey = null
        _sessionKey = ByteArray(0)
    }

    var _nce_sequence: Int = 0


    fun keyToHexString(key: Key): String? {
        return key.encoded.hexa()
    }

    fun GenerateKeyPair() {
        try {
            val _object = generateKeyPair()
            _privateKey = _object.first
            _publicKey = _object.second
        } catch (e: Exception) {
            println(e.message)
        }
    }

    fun  truncCoordinate(c : ByteArray, coord: String): ByteArray{
        if (c.count() != 32){
            println (coord +" NOT 32 in Len")
            if (c.count() == 33){
                if (c[0].toInt() != 0){
                    println ("ERROR unexpexted non zero discard byte for "+coord)
                }
                else {
                    val t = CopyBytes(c,1,32)
                    return t
                }
            }
            else
                println ("ERROR " +coord +" NOT 32 or 33 in Len")
        }
        return c
    }

    // caution we remove the leading DER 04 for the card here!
    override fun GetPublicHostKey() : ByteArray {

        val publicKey = _publicKey as ECPublicKey
        var x: ByteArray = publicKey.getW().getAffineX().toByteArray()
        var y: ByteArray = publicKey.getW().getAffineY().toByteArray()

        println("bc public host x")
        println(x.hexa())
        println("bc public host y")
        println(y.hexa())

        x = truncCoordinate(x, "X")
        y = truncCoordinate(y, "Y")

        _rawCardPublicKey +=  x + y

        println("Raw app public key to card")
        println(_rawCardPublicKey.hexa())
        return _rawCardPublicKey
    }

    override fun SetCardPublicKey(cardPublicKeyBytes: ByteArray) {
        try {
            println("Card Public key bytes:"+cardPublicKeyBytes.hexa())
            println("card public key len:"+ cardPublicKeyBytes.count())
            if (cardPublicKeyBytes.count() != 64)
                throw java.lang.Exception("card key len not expected")
            val eckf = KeyFactory.getInstance("EC")
            // the key needs to be sply to x and y
            val cardPublicx = CopyBytes(cardPublicKeyBytes,0,32)
            println("Card Public key xbytes:"+cardPublicx.hexa())
            val cardPublicy = CopyBytes(cardPublicKeyBytes,32,32)
            println("Card Public key ybytes:"+cardPublicy.hexa())
            // construc a point on the curve using the 64 byte public key the card sends split into 2 32 byte values, x,y. first half is x, second half is y
            // use values to make an EC point


            val point = ECPoint(BigInteger(1, cardPublicx), BigInteger(1, cardPublicy))

            // we have to tell the key factory which specific EC curve we are using and provide a point on that
            // curve to generate a key object for the DH compare
            // get a ECParameter for the actual curve
            val params = ECGenParameterSpec(curveName)
            val parameters = AlgorithmParameters.getInstance("EC")
            parameters.init(params)
            // create a key factory keyspec parameter indicating EC and secp256r1
            val ecParameters = parameters.getParameterSpec(ECParameterSpec::class.java)

            // at last get the actual key
             _cardPublicKey = eckf.generatePublic(ECPublicKeySpec(point, ecParameters)) as ECPublicKey

        } catch (e: Exception) {
            println(e.message)
        }
    }

    override fun InitializeRandomSequence() {
        // need a random uint32, but not near an overflow, so will get
        // a random uint16 and go up
        val number = Random.nextInt(0, 65536)
        _nce_sequence = number
    }

    override fun GetNextSequence(): Int {
        _nce_sequence += 2
        return _nce_sequence
    }
    fun hexToByte(hexString: String): Byte {
        val firstDigit = toDigit(hexString[0])
        val secondDigit = toDigit(hexString[1])
        return ((firstDigit shl 4) + secondDigit).toByte()
    }

    private fun toDigit(hexChar: Char): Int {
        val digit = Character.digit(hexChar, 16)
        require(digit != -1) { "Invalid Hexadecimal Character: $hexChar" }
        return digit
    }
    fun decodeHexString(hexString: String): ByteArray? {
        require(hexString.length % 2 != 1) { "Invalid hexadecimal String supplied." }
        val bytes = ByteArray(hexString.length / 2)
        var i = 0
        while (i < hexString.length) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2))
            i += 2
        }
        return bytes
    }

    fun getSecretKeySpec( privateKey: PrivateKey, publicKey: PublicKey): ByteArray {

        // this code when you use the cards private and the app/host public works to get the right shared secret
        // that proves that the key agreement algo is right and that my public key is correctly formatted
        // the smoking gun then is when you use the apps private key against the cards public indicating that my private key is malformed or just wrong

        val x = privateKey as ECPrivateKey
        println("privkey ")
        println(x.s)
        val ba = x.s.toByteArray()
        println(ba.hexa())
        val keyAgree = KeyAgreement.getInstance("ECDH")
        println(keyAgree.provider.name)
        println(keyAgree.algorithm)
        keyAgree.init(privateKey)
        keyAgree.doPhase(publicKey, true)

        val serverSharedSecret = keyAgree.generateSecret()
        println("Shared Secret: " + serverSharedSecret.hexa())
        return serverSharedSecret

    /*    val serverAesKey = SecretKeySpec(serverSharedSecret, 0, 16, KeyProperties.KEY_ALGORITHM_EC)

        Log.d("AESEAX", "Shared Secret key: " + serverAesKey.encoded.hexa())
        return serverAesKey*/
    }

    //  func DoDH(cardPublicKeyHexBuffer:String,  appKey:SecKey)->CFByteArray{
    fun GenerateSessionKeyFromSecret(pin: String) {
        val sharedSecret = getSecretKeySpec(_privateKey!!, _cardPublicKey!!)

        println("shared secret:" + sharedSecret.hexa())
        _sessionKey = ByteArray(0)
        _sessionKey += sharedSecret

        val pdata = pin.toByteArray(Charsets.UTF_8)
        println("pin as hex string: "+pdata.hexa())
        _sessionKey += pdata
        println("premsg sesskey: "+ _sessionKey.hexa())

        val s256val = sha256(_sessionKey)
        println("s256val : "+s256val)
        val fullHexValue = decodeHexString(s256val!!)
        println("decoded hex string: "+fullHexValue!!.hexa())
        _sessionKey = ByteArray(0)
        for (i in 0 .. 15) {
            _sessionKey += fullHexValue!![i]
        }
        println("computed session key")
        println(_sessionKey.hexa())
    }

    fun EmptyEncryptionHeader(payload: ByteArray) : ByteArray? {
        // we need to calc the nonce which is in the enc header, so create it here
        // new create actual encryption header and payload
        val cmd: Byte = APP_H2C_MSG.toByte()
        val len = payload.size
        val encHeader = EtherEncHeader(
                cmd = cmd,
                status = 0,
                length = len,
                sequence = GetNextSequence()
        )

        println("clear encheader: nonce,key,data")
        println(payload.hexa())

        var packet = ByteArray(0)
        packet += encHeader.GetHeaderBuffer()
        packet += payload

        return packet
    }
    // we use the last calced nonce
    override fun EncryptByteArray(encPayload: ByteArray): ByteArray? {
        if (_privateKey != null && _publicKey !== null) {

            // we need to calc the nonce which is in the enc header, so create it here
            // new create actual encryption header and payload
            val cmd: Byte = (APP_H2C_MSG or APP_FLAG_ENCRYPT).toByte()
            val len = encPayload.size
            val encHeader = EtherEncHeader(cmd = cmd, status = 0, length = len, sequence = GetNextSequence())
            var nonce = encHeader.GetNonce()
            var mac = encHeader.GetMac()
            val payload = arrayListOf<Byte>()
            payload.addAll(encPayload.toList())

            println("ENCRYPTING")
            println("nonce,key,data")
            println(nonce.hexa())
            println(_sessionKey.hexa())
            println(encPayload.hexa())
            val ba = "EAX Mode Test".toByteArray(Charsets.UTF_8)

            val encBufferResult = encrypt(_sessionKey!!,  nonce, encPayload, mac.toByteArray())
            val encBuffer = encBufferResult.first

            println("ENCBuffer:")
            println(encBuffer.hexa())

            // get the mac
            val cmac = encBufferResult.second
            var _encMac = arrayListOf<Byte>()

            for (i in 0 .. 7) {
                _encMac.add(cmac[i])
            }

            println("MAC")
            println(_encMac.toByteArray().hexa())

            encHeader.SetMac(mac = _encMac)

            return (encHeader.GetHeaderBuffer() + encBuffer.toList())
        }

        return byteArrayOf()
    }

    // this sends in the Encrypted Header and app data payload within the bigger transport packet
    override fun DecryptByteArray(encHeader: ByteArray, encAppData: ByteArray) : ByteArray? {
        if (_privateKey != null && _publicKey !== null) {
//            val aes_eax_bridge = AES_EAX()
            val skey = _sessionKey
            // we need the nonce and can grab it directly from the buffer

            var nonce = encHeader.copyOfRange(0, 8).toList().toByteArray()
            for (i in 0 .. 7) {
                nonce += 0.toByte()
            }
            var mac = encHeader.copyOfRange(8,16).toList().toByteArray()

            println("DECRYPTING")
            println("nonce,key,data")
            println(nonce.hexa())
            println(skey?.hexa())
            println(encAppData.hexa())

//            val decryptBuffer = aes_eax_bridge.aes_EAXDoDecrypt(ctx, key = skey, nonce = nonce, crypt_text = encAppData)
            val decryptBuffer = decrypt(_sessionKey!!, encAppData, nonce, mac)
            println(decryptBuffer.hexa())

            return decryptBuffer
        }

        return byteArrayOf()
    }

    fun sha256(base: ByteArray): String? {
        val digest = MessageDigest.getInstance("SHA-256")
        println("digest")
        println(digest.provider.name)
        println(digest.algorithm)

        val hash :ByteArray = digest.digest(base)
        println("hash: "+hash.hexa())
        val hexString = StringBuffer()

        for (i in hash.indices) {
            val hex = Integer.toHexString(0xff and hash[i].toInt())
            if (hex.length == 1) hexString.append('0')
            hexString.append(hex)
        }

        return hexString.toString()
    }

    fun generateKeyPair(): Pair<PrivateKey, PublicKey> {
        val spec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(curveName)
        val kpGen: KeyPairGenerator =  KeyPairGenerator.getInstance("ECDH")
        val random =  SecureRandom ()
        if (random == null) {
            kpGen.initialize(spec)
        } else {
            kpGen.initialize(spec, random)
        }
        val keyPair =  kpGen.generateKeyPair()
        val x = keyPair.private as ECPrivateKey
        println("privkey at gen")
        println(x.s)
        val ba = x.s.toByteArray()
        println("reduced priv key")
        println(ba.hexa())

        return Pair(keyPair.private, keyPair.public)
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


    fun getSignature(challenge: ByteArray, privateKey: PrivateKey): ByteArray {
        // sign using the private key
        // sign using the private key
        val sig = Signature.getInstance("SHA256withECDSA")
        sig.initSign(privateKey)
        sig.update(challenge)

        return sig.sign()
    }

    fun decrypt(sessionKey : ByteArray, data: ByteArray, nonce: ByteArray, mac: ByteArray): ByteArray {

        // so
        println("nonce: " + nonce.hexa())
        println("mac: "+mac.hexa())
        println("data: "+data.hexa())
        val cipherTextIn = data + mac
        val cipher = EAXBlockCipher(AESEngine())
        //SecureRandom().nextBytes(nonce)
        //val key: ByteArray = generateKey()
        val kp = KeyParameter(sessionKey)
        val par = AEADParameters(kp, AES_MAC_SIZE_BITS, nonce)
        cipher.init(false, par)
        val sz = cipher.getOutputSize(cipherTextIn.count())
        val out = ByteArray(sz)
        var off = 0
        off += cipher.processBytes(cipherTextIn, 0, cipherTextIn.count(), out, off)

        cipher.doFinal(out, off)

        return out
    }


    fun encrypt(sessionKey : ByteArray, nonce: ByteArray, data: ByteArray, mac :ByteArray): Pair<ByteArray, ByteArray> {
        val eng = AESEngine()
        val cipher = EAXBlockCipher(eng)
        val kp = KeyParameter(sessionKey)
        val par = AEADParameters(kp, AES_MAC_SIZE_BITS, nonce)
        cipher.init(true, par)
        val sz = cipher.getOutputSize(data.count())
        val out = ByteArray(sz)
        var off = 0
        off += cipher.processBytes(data, 0, data.count(), out, off)

        cipher.doFinal(out, off)

        // out at this point has the encrypted data at its start, and the mac at its end, split them
        val edata = CopyBytes(out,0,data.count())
        val mymac = CopyBytes(out,data.count(), out.count() - data.count())
        val themac = cipher.mac
        if (!Arrays.equals(mymac, themac))
            throw java.lang.Exception("bad mac")

        return Pair(edata,cipher.mac)
    }

}