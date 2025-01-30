package org.unifiedpush.distributor.fcm

import android.content.Context
import org.unifiedpush.distributor.MigrationFactory

class Migrations(context: Context) : MigrationFactory(context, PREF_NAME) {

    override val migrations = emptyList<Migration>()

    // TODO: migration shared pref => db
    companion object {
        private const val PREF_NAME = "FCMD"
    }
}
