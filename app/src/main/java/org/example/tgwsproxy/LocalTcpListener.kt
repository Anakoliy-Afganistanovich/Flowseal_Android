package org.example.tgwsproxy

import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/** Diagnostic-only loopback listener. It does not implement MTProto or forward traffic. */
class LocalTcpListener(private val port: Int = 1443) {
    private val running = AtomicBoolean(false)
    @Volatile private var server: ServerSocket? = null
    @Volatile var lastError: String? = null
        private set

    @Synchronized fun start(): Boolean {
        if (running.get()) return true
        return try {
            val socket = ServerSocket(port, 16, InetAddress.getByName("127.0.0.1"))
            server = socket
            running.set(true)
            lastError = null
            thread(name = "tgws-loopback-listener", isDaemon = true) {
                try {
                    while (running.get()) {
                        try {
                            socket.accept().use { client ->
                                client.soTimeout = 1500
                                // No protocol response: prevent misleading 'connected' status.
                            }
                        } catch (e: SocketException) {
                            if (running.get()) lastError = e.message
                            break
                        } catch (e: Exception) {
                            if (running.get()) lastError = e.message
                        }
                    }
                } finally {
                    running.set(false)
                    if (server === socket) server = null
                    runCatching { socket.close() }
                }
            }
            true
        } catch (e: Exception) {
            lastError = e.message ?: e.javaClass.simpleName
            false
        }
    }

    @Synchronized fun stop() {
        running.set(false)
        val old = server
        server = null
        runCatching { old?.close() }
    }

    fun isRunning(): Boolean = running.get() && server?.isClosed == false
}
