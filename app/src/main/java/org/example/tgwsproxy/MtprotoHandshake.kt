package org.example.tgwsproxy

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/** Port of the obfuscated MTProto handshake primitives from Flowseal.
 * Not a proxy listener, WebSocket transport, or complete Telegram proxy.
 */
internal object MtprotoHandshake {
    data class ClientInit(val dcId: Int, val media: Boolean, val tag: Int, val clientKeyIv: ByteArray)
    private val random = SecureRandom()
    private val supportedTags = setOf(0xEFEFEFEF.toInt(), 0xEEEEEEEE.toInt(), 0xDDDDDDDD.toInt())
    private fun leInt(b: ByteArray, offset: Int): Int =
        (b[offset].toInt() and 255) or ((b[offset + 1].toInt() and 255) shl 8) or
        ((b[offset + 2].toInt() and 255) shl 16) or ((b[offset + 3].toInt() and 255) shl 24)

    private fun crypt(key: ByteArray, iv: ByteArray, input: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CTR/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return cipher.doFinal(input)
    }

    fun parseClientInit(handshake: ByteArray, secret: ByteArray): ClientInit? {
        require(handshake.size == 64) { "MTProto obfuscated handshake must be 64 bytes" }
        require(secret.isNotEmpty()) { "Proxy secret is missing" }
        val keyIv = handshake.copyOfRange(8, 56)
        val key = MessageDigest.getInstance("SHA-256")
            .digest(handshake.copyOfRange(8, 40) + secret)
        val decrypted = crypt(key, handshake.copyOfRange(40, 56), handshake)
        val tag = leInt(decrypted, 56)
        if (tag !in supportedTags) return null
        val dcSigned = ((decrypted[60].toInt() and 255) or ((decrypted[61].toInt() and 255) shl 8)).toShort().toInt()
        if (dcSigned == Short.MIN_VALUE.toInt()) return null
        return ClientInit(kotlin.math.abs(dcSigned), dcSigned < 0, tag, keyIv)
    }

    fun createRelayInit(tag: Int, signedDc: Int): ByteArray {
        require(tag in supportedTags) { "Unsupported MTProto transport tag" }
        require(signedDc in Short.MIN_VALUE..Short.MAX_VALUE)
        val prefix = ByteArray(64)
        do { random.nextBytes(prefix) } while (
            (prefix[0].toInt() and 255) == 0xEF ||
            prefix.copyOfRange(0, 4).contentEquals(byteArrayOf(0x47, 0x45, 0x54, 0x20)) ||
            prefix.copyOfRange(0, 4).contentEquals(byteArrayOf(0x50, 0x4F, 0x53, 0x54)) ||
            prefix.copyOfRange(0, 4).contentEquals(byteArrayOf(0x48, 0x45, 0x41, 0x44)) ||
            prefix.copyOfRange(0, 4).contentEquals(byteArrayOf(0xEE.toByte(), 0xEE.toByte(), 0xEE.toByte(), 0xEE.toByte())) ||
            prefix.copyOfRange(0, 4).contentEquals(byteArrayOf(0xDD.toByte(), 0xDD.toByte(), 0xDD.toByte(), 0xDD.toByte())) ||
            prefix.copyOfRange(0, 4).contentEquals(byteArrayOf(0x16, 0x03, 0x01, 0x02)) ||
            prefix.copyOfRange(0, 4).contentEquals(byteArrayOf(0x4F, 0x50, 0x54, 0x49)) ||
            prefix.copyOfRange(4, 8).contentEquals(byteArrayOf(0, 0, 0, 0))
        )
        val stream = crypt(prefix.copyOfRange(8, 40), prefix.copyOfRange(40, 56), prefix)
        val tail = byteArrayOf(
            (tag and 255).toByte(), ((tag ushr 8) and 255).toByte(),
            ((tag ushr 16) and 255).toByte(), ((tag ushr 24) and 255).toByte(),
            (signedDc and 255).toByte(), ((signedDc ushr 8) and 255).toByte(), 0, 0
        )
        random.nextBytes(tail, 6, 2)
        for (i in 0 until 8) prefix[56 + i] = (tail[i].toInt() xor stream[56 + i].toInt() xor prefix[56 + i].toInt()).toByte()
        return prefix
    }
    private fun SecureRandom.nextBytes(buffer: ByteArray, offset: Int, length: Int) {
        val extra = ByteArray(length); nextBytes(extra); extra.copyInto(buffer, offset)
    }
}
