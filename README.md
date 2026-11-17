# TG WS Proxy Android — Android Studio prototype

**STATUS: UI PROTOTYPE ONLY. NOT A WORKING TELEGRAM PROXY.**

This repository builds an installable Android 15+ prototype APK with the agreed Russian UI and saved background toggle. The original Flowseal Python MTProto/WebSocket network engine **has not been ported**. Start/Connect controls remain disabled intentionally. Do not interpret a green GitHub Actions build as proof of a working proxy.

## Build in GitHub Actions

1. Create an empty GitHub repository (private is fine).
2. Upload the **contents** of this directory to its root, including `.github/workflows/android-apk.yml`.
3. Open Actions > Android APK (prototype) > Run workflow (or push to main).
4. After the workflow succeeds, download the `tg-ws-proxy-android-prototype-debug` artifact and extract `app-debug.apk`.
5. This is a debug-signed APK, intended for testing only. Do not publish as a production release.

## Remaining work before a working proxy APK

- Port Flowseal's actual MTProto handshake, encrypted bridge, WebSocket transport, CF routing, and fallback to Android-compatible code or a validated Python runtime.
- Implement a loopback-only listener, service lifecycle, cancellation, error handling and notification.
- Verify the correct Android foreground-service type and Android 15/16 policies; test background behavior on physical devices.
- Implement Telegram proxy configuration/deep link using actual generated secret and port.
- Perform connection tests and security review, then set up a separate release signing key.

The Flowseal source archive provided by the user was reviewed; this prototype is **not** a translated copy of the Python network core. Original project: https://github.com/Flowseal/tg-ws-proxy ; license: MIT (see `upstream/FLOWSEAL_LICENSE.txt`).
