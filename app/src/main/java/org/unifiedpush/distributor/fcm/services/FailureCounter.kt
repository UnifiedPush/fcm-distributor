package org.unifiedpush.distributor.fcm.services

import android.content.Context
import org.unifiedpush.distributor.AppNotification
import org.unifiedpush.distributor.FailureCounter as FCounter

object FailureCounter : FCounter<Any>() {
    override val foregroundService = FgService.service

    override fun disconnectedNotification(context: Context): AppNotification? = null

    override fun getDummySource(): Any {
        return Any()
    }

    override fun cancelSource(source: Any?) {}
}
