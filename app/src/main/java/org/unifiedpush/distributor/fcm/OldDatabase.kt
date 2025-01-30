package org.unifiedpush.distributor.fcm

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class OldDatabase(context: Context)
        : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    data class OldRecord(
        val packageName: String,
        val token: String
    )

    override fun onCreate(db: SQLiteDatabase) {}

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    fun listOldApps(): List<OldRecord> {
        val db = readableDatabase
        return db.query(
            TABLE_APPS,
            null,
            null,
            null,
            null,
            null,
            null
        ).use { cursor ->
            generateSequence { if (cursor.moveToNext()) cursor else null }
                .mapNotNull {
                    val tokenColumn = cursor.getColumnIndex(FIELD_TOKEN)
                    val packageNameColumn = cursor.getColumnIndex(FIELD_PACKAGE_NAME)
                    val token = if (tokenColumn >= 0) it.getString(tokenColumn) else return@mapNotNull null
                    val packageName = if (packageNameColumn >= 0) it.getString(packageNameColumn) else return@mapNotNull null
                    OldRecord(packageName, token)
                }
                .toList()
        }
    }

    companion object {
        private const val DB_NAME = "unifiedpush"
        private const val DB_VERSION = 1
        private const val TABLE_APPS = "apps"
        private const val FIELD_PACKAGE_NAME = "package_name"
        private const val FIELD_TOKEN = "token"
    }
}