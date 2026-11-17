package org.example.tgwsproxy

import android.app.Service
import android.content.Intent
import android.os.IBinder

/** Reserved integration point for the ported Flowseal proxy core.
 * Do not start this service until the actual network engine is implemented.
 */
class ProxyService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
