package org.unifiedpush.distributor.fcm

import android.content.Context
import android.util.Log
import org.unifiedpush.distributor.MigrationFactory
import org.unifiedpush.distributor.fcm.utils.TAG

class Migrations(context: Context) : MigrationFactory(context, PREF_NAME) {

    override val migrations = listOf(
        Migration020000
    )

    /**
     * Migration from 0.X.X to 2.0.0
     *
     * Migrate database to unifiedpush lib one
     */
    object Migration020000 : Migration {
        override val version = 20000

        override fun run(context: Context) {
            try {
                OldDatabase(context).listOldApps().forEach { app ->
                    Distributor.createApp(context, app.packageName, app.token, null, null) {
                        Distributor.sendEndpointForCoToken(context, app.token)
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Couldn't migrate db: $e")
            }
        }
    }

    companion object {
        private const val PREF_NAME = "FCMD"
    }
}
