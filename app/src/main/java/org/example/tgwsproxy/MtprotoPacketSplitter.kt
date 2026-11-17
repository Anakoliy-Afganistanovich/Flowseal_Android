package org.example.tgwsproxy


/**
 * Stateful MTProto packet framing, ported from Flowseal's bridge.MsgSplitter.
 * Both inputs must cover exactly the same bytes, before/after AES-CTR processing.
 * This class does not establish any upstream connection.
 */
internal class MtprotoPacketSplitter(private val transportTag: Int) {
    private var encrypted = ByteArray(0)
    private var plain = ByteArray(0)
    private var passthrough = false

    fun accept(cipherChunk: ByteArray, plainChunk: ByteArray): List<ByteArray> {
        require(cipherChunk.size == plainChunk.size) { "Encrypted/plain chunk lengths differ" }
        if (cipherChunk.isEmpty()) return emptyList()
        if (passthrough) return listOf(cipherChunk.copyOf())
        encrypted += cipherChunk
        plain += plainChunk
        // Cap buffered data on malformed or incomplete frames.
        if (encrypted.size > MAX_BUFFER) return disableAndFlush()
        val packets = mutableListOf<ByteArray>()
        var offset = 0
        while (offset < plain.size) {
            val remaining = plain.size - offset
            val frameSize = when (transportTag) {
                ABRIDGED -> {
                    val first = plain[offset].toInt() and 255
                    val length: Int
                    val header: Int
                    if (first == 0x7f || first == 0xff) {
                        if (remaining < 4) break
                        length = ((plain[offset + 1].toInt() and 255) or
                            ((plain[offset + 2].toInt() and 255) shl 8) or
                            ((plain[offset + 3].toInt() and 255) shl 16)) * 4
                        header = 4
                    } else {
                        length = (first and 0x7f) * 4
                        header = 1
                    }
                    if (length == 0 || length > MAX_PACKET) -1 else header + length
                }
                INTERMEDIATE, PADDED_INTERMEDIATE -> {
                    if (remaining < 4) break
                    val raw = (plain[offset].toLong() and 255) or
                        ((plain[offset + 1].toLong() and 255) shl 8) or
                        ((plain[offset + 2].toLong() and 255) shl 16) or
                        ((plain[offset + 3].toLong() and 255) shl 24)
                    val length = raw and 0x7fffffff
                    if (length == 0L || length > MAX_PACKET) -1 else 4 + length.toInt()
                }
                else -> -1
            }
            if (frameSize < 0) {
                packets += encrypted.copyOfRange(offset, encrypted.size)
                encrypted = ByteArray(0)
                plain = ByteArray(0)
                passthrough = true
                return packets
            }
            if (remaining < frameSize) break
            packets += encrypted.copyOfRange(offset, offset + frameSize)
            offset += frameSize
        }
        if (offset > 0) {
            encrypted = encrypted.copyOfRange(offset, encrypted.size)
            plain = plain.copyOfRange(offset, plain.size)
        }
        return packets
    }

    fun flush(): List<ByteArray> {
        if (encrypted.isEmpty()) return emptyList()
        val remainder = encrypted
        encrypted = ByteArray(0)
        plain = ByteArray(0)
        return listOf(remainder)
    }

    private fun disableAndFlush(): List<ByteArray> {
        passthrough = true
        return flush()
    }

    companion object {
        const val ABRIDGED = 0xEFEFEFEF.toInt()
        const val INTERMEDIATE = 0xEEEEEEEE.toInt()
        const val PADDED_INTERMEDIATE = 0xDDDDDDDD.toInt()
        private const val MAX_PACKET = 4 * 1024 * 1024
        private const val MAX_BUFFER = MAX_PACKET + 4
    }
}
