package org.example.tgwsproxy

import org.junit.Assert.*
import org.junit.Test

class MtprotoPacketSplitterTest {
    @Test fun abridgedFragmentedPacket() {
        val s = MtprotoPacketSplitter(MtprotoPacketSplitter.ABRIDGED)
        val bytes = byteArrayOf(1, 10, 20, 30, 40)
        assertTrue(s.accept(bytes.copyOfRange(0, 2), bytes.copyOfRange(0, 2)).isEmpty())
        val frames = s.accept(bytes.copyOfRange(2, 5), bytes.copyOfRange(2, 5))
        assertEquals(1, frames.size)
        assertArrayEquals(bytes, frames[0])
    }
    @Test fun intermediateTwoPackets() {
        val s = MtprotoPacketSplitter(MtprotoPacketSplitter.INTERMEDIATE)
        val bytes = byteArrayOf(4, 0, 0, 0, 1, 2, 3, 4, 4, 0, 0, 0, 5, 6, 7, 8)
        val frames = s.accept(bytes, bytes)
        assertEquals(2, frames.size)
        assertArrayEquals(bytes.copyOfRange(0, 8), frames[0])
        assertArrayEquals(bytes.copyOfRange(8, 16), frames[1])
    }
    @Test fun rejectsMismatchedBuffers() {
        val s = MtprotoPacketSplitter(MtprotoPacketSplitter.INTERMEDIATE)
        try { s.accept(byteArrayOf(1), byteArrayOf()); fail("Expected exception") }
        catch (_: IllegalArgumentException) { }
    }
    @Test fun flushReturnsPartial() {
        val s = MtprotoPacketSplitter(MtprotoPacketSplitter.INTERMEDIATE)
        val bytes = byteArrayOf(8, 0, 0, 0, 1)
        assertTrue(s.accept(bytes, bytes).isEmpty())
        assertArrayEquals(bytes, s.flush().single())
        assertTrue(s.flush().isEmpty())
    }
}
