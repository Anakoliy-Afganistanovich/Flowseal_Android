# Flowseal Android — сетевой перенос (этап 2)

## Проверенные исходники
Исходный архив `tg-ws-proxy-main.zip`, коммит `6690b788432fd0f59b0f38e228494bcd6bede1ca`.
Ключевые файлы: `proxy/tg_ws_proxy.py`, `proxy/bridge.py`, `proxy/raw_websocket.py`, `proxy/fake_tls.py`, `proxy/cf_h2.py`, `proxy/_aes.py`, `proxy/config.py`.

## Почему простой TCP-forwarder не подходит
Flowseal принимает MTProto obfuscated2 handshake, расшифровывает заголовок, формирует relay init, ведёт отдельные криптоконтексты и использует WebSocket/H2 с fallback. Простая переадресация TCP не эквивалентна Flowseal и не должна называться рабочим портом.

## Последовательность реализации
1. Перенести handshake и криптографические тест-векторы из `tg_ws_proxy.py` и `_aes.py` на Kotlin/JCA.
2. Перенести `MsgSplitter` и управление потоками из `bridge.py` с тестами на пакетизацию и закрытие соединений.
3. Перенести WebSocket/TLS и H2 transport с учётом fallback и проверки сертификатов.
4. Подключить локальный TCP listener к сервису Android, безопасно генерировать секрет и ссылку `tg://proxy`.
5. Реализовать Foreground Service и переключатель фонового режима; проверить ограничения Android 15/16.
6. Интеграционные тесты Telegram на реальном устройстве и тесты сети (Wi-Fi / мобильная сеть / сон).

## Текущий статус
Сборка UI-прототипа подтверждена GitHub Actions и установкой на Android 16. Сетевой порт и тесты соединений НЕ выполнены. Не включать кнопку запуска до появления рабочего движка.
