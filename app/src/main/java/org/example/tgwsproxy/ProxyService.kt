package org.example.tgwsproxy

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

/** Diagnostic local TCP listener; not a functioning Telegram proxy. */
class ProxyService : Service() {
    companion object {
        const val CHANNEL = "proxy_status"
        const val ACTION_STOP = "org.example.tgwsproxy.STOP"
        const val ACTION_START = "org.example.tgwsproxy.START"
        const val NOTIFICATION_ID = 1443
        @Volatile var active = false
            private set
        @Volatile var error: String? = null
            private set
    }
    private val listener = LocalTcpListener()

    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Локальный TCP (диагностика)", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        val openUi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("TG WS Proxy: диагностика")
            .setContentText("TCP 127.0.0.1:1443; Telegram пока не поддерживается")
            .setContentIntent(openUi).setOngoing(true).build()
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        active = listener.start()
        error = listener.lastError
        if (!active) stopSelf()
        return if (active) START_STICKY else START_NOT_STICKY
    }

    override fun onDestroy() {
        listener.stop()
        active = false
        super.onDestroy()
    }
}
