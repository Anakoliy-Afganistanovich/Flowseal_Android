# Stage 2 — MTProto packet framing (work in progress)

Added `MtprotoPacketSplitter.kt` and JVM unit tests for fragmented abridged and intermediate packets, multi-frame chunks, buffer validation and flush. Tests are configured in GitHub Actions before `assembleDebug`.

**Not yet compiled or executed in this environment.** Upload to GitHub to verify tests and compilation. This is an incremental networking component, not a working proxy: upstream WebSocket, fallback routes, client socket listener, AES stream contexts, foreground service and Telegram connection are still missing.

The existing `MtprotoHandshake.kt` remains experimental and has not been validated against the upstream Python implementation. Do not enable the proxy controls or distribute the APK as functional until end-to-end tests succeed.
