package org.example.tgwsproxy

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder

/** Service lifecycle scaffold. No proxy listener is started until the Flowseal core is ported. */
class ProxyService : Service() {
    companion object {
        const val CHANNEL = "proxy_status"
        const val ACTION_STOP = "org.example.tgwsproxy.STOP"
        const val NOTIFICATION_ID = 1443
    }
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(CHANNEL, "Работа прокси", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        // Deliberately refuse to claim proxy readiness without a network engine.
        stopSelf()
        return START_NOT_STICKY
    }
}
