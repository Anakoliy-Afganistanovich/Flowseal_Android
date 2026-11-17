# Stage 2: diagnostic TCP listener

Added a real loopback TCP listening socket at 127.0.0.1:1443 and a user-started Android foreground service, plus start/stop controls and background-mode stop-on-hide behavior.

**NOT a functional Telegram proxy.** Accepted connections are closed without MTProto/WebSocket processing or forwarding. The Telegram connection button remains disabled. Do not treat a green GitHub Actions build as network interoperability verification.

Not compiled or device-tested in this environment. Build with GitHub Actions and check the listener on a real Android 15/16 device. Further work: validate the original Flowseal transport and handshake behavior, implement end-to-end relay, and add integration tests.
