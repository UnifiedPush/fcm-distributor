package org.unifiedpush.distributor.fcm.services

import android.content.Context
import org.unifiedpush.distributor.Database
import org.unifiedpush.distributor.RegistrationCounter
import org.unifiedpush.distributor.fcm.DatabaseFactory
import org.unifiedpush.distributor.fcm.activities.UiAction
import org.unifiedpush.distributor.fcm.utils.ForegroundNotification

object MainRegistrationCounter : RegistrationCounter() {

    override val workerCompanion = RestartWorker.Companion

    override fun onCountRefreshed(context: Context) {
        ForegroundNotification(context).update()
        UiAction.publish(UiAction.Action.RefreshRegistrations)
    }

    override fun getDb(context: Context): Database {
        return DatabaseFactory.getDb(context)
    }
}
