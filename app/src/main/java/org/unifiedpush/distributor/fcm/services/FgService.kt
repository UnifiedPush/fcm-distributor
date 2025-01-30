package org.unifiedpush.distributor.fcm.services

import android.content.Context
import android.util.Log
import androidx.lifecycle.AtomicReference
import org.unifiedpush.distributor.fcm.callback.BatteryCallbackFactory
import org.unifiedpush.distributor.fcm.callback.NetworkCallbackFactory
import org.unifiedpush.distributor.fcm.utils.ForegroundNotification
import org.unifiedpush.distributor.fcm.utils.NOTIFICATION_ID_FOREGROUND
import org.unifiedpush.distributor.fcm.utils.TAG
import org.unifiedpush.distributor.service.ForegroundService
import org.unifiedpush.distributor.service.ForegroundServiceFactory

class FgService : ForegroundService() {

    override val networkCallbackFactory = NetworkCallbackFactory
    override val batteryCallbackFactory = BatteryCallbackFactory
    override val registrationCounter = MainRegistrationCounter
    override val workerCompanion = RestartWorker.Companion
    override val staticRef = service

    override fun startForegroundNotification() {
        val notification = ForegroundNotification(this).create()
        startForeground(NOTIFICATION_ID_FOREGROUND, notification)
    }

    override fun shouldAbortNewSync(): Boolean {
        return started
    }

    override fun isConnected(): Boolean {
        return true
    }

    override fun sync(releaseLock: () -> Unit) {
        started = true
        releaseLock()
    }

    companion object : ForegroundServiceFactory {
        private var started = false
        override val service = AtomicReference<ForegroundService?>(null)
        override val serviceClass = FgService::class.java

        override fun startService(context: Context) {
            Log.d(FgService.TAG, "nFails: ${FailureCounter.nFails}")
            super.startService(context)
        }
    }
}
