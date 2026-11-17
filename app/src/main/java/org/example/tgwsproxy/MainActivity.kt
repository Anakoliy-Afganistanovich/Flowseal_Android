package org.example.tgwsproxy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(36, 60, 36, 24) }
        fun label(value: String) = TextView(this).apply { text = value; textSize = 17f; setPadding(0, 12, 0, 12) }
        root.addView(label("TG WS Proxy · Android 15–16"))
        root.addView(label("Этап 2: проверка локального TCP-сервера. Это НЕ работающий Telegram-прокси."))
        val status = label("")
        root.addView(status)
        root.addView(label("Локальный адрес: 127.0.0.1:1443"))
        val start = Button(this).apply { text = "Запустить TCP-сервер (диагностика)" }
        val stop = Button(this).apply { text = "Остановить TCP-сервер" }
        fun refresh() {
            status.text = when {
                ProxyService.active -> "TCP-сервер запущен (без MTProto/WebSocket)"
                ProxyService.error != null -> "Ошибка: ${ProxyService.error}"
                else -> "TCP-сервер остановлен"
            }
        }
        start.setOnClickListener {
            try {
                startForegroundService(Intent(this, ProxyService::class.java).setAction(ProxyService.ACTION_START))
                status.postDelayed({ refresh() }, 400)
            } catch (e: Exception) {
                status.text = "Ошибка запуска: ${e.message}"
            }
        }
        stop.setOnClickListener {
            stopService(Intent(this, ProxyService::class.java))
            status.postDelayed({ refresh() }, 200)
        }
        root.addView(start)
        root.addView(stop)
        root.addView(Button(this).apply {
            text = "Подключить Telegram (недоступно)"
            isEnabled = false
        })
        root.addView(Switch(this).apply {
            text = "Работа в фоне"
            isChecked = prefs.getBoolean("background", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("background", checked).apply()
                // Disabled means explicitly stop when UI goes to background.
            }
        })
        root.addView(label("При отключённой фоновой работе сервер останавливается после сворачивания приложения."))
        setContentView(root)
        refresh()
    }

    override fun onStop() {
        super.onStop()
        if (!getSharedPreferences("settings", MODE_PRIVATE).getBoolean("background", true)) {
            stopService(Intent(this, ProxyService::class.java))
        }
    }
}
