package org.example.tgwsproxy

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*

/** UI prototype. No network proxy implementation is included yet. */
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(36, 60, 36, 24) }
        fun label(value: String) = TextView(this).apply { text = value; textSize = 18f; setPadding(0, 12, 0, 12) }
        root.addView(label("TG WS Proxy · Android 15–16"))
        root.addView(label("Состояние: сетевое ядро ещё не подключено"))
        root.addView(label("Локальный сервер: 127.0.0.1:1443"))
        root.addView(Button(this).apply {
            text = "Запустить прокси (пока недоступно)"
            isEnabled = false // Never claim a working connection without a real proxy core.
        })
        root.addView(Button(this).apply {
            text = "Подключить Telegram (пока недоступно)"
            isEnabled = false
        })
        root.addView(Switch(this).apply {
            text = "Работа в фоне"
            isChecked = prefs.getBoolean("background", true)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("background", checked).apply() }
        })
        root.addView(label("Переключатель сохраняет настройку. Фоновая служба будет подключена после переноса сетевого ядра."))
        setContentView(root)
    }
}
