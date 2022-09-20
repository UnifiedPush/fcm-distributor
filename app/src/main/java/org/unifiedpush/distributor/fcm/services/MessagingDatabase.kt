package org.unifiedpush.distributor.fcm.services

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DB_NAME = "unifiedpush"
private const val DB_VERSION = 1
private const val CREATE_TABLE_APPS =
    "CREATE TABLE apps (package_name TEXT, token TEXT, PRIMARY KEY (token));"
private const val TABLE_APPS = "apps"
private const val FIELD_PACKAGE_NAME = "package_name"
private const val FIELD_TOKEN = "token"

class MessagingDatabase(context: Context)
    : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){

    companion object {
        private lateinit var db: MessagingDatabase

        fun getDb(context: Context): MessagingDatabase {
            if (!this::db.isInitialized) {
                checkAndRenameDatabase(context, "gotify_service", DB_NAME)
                db = MessagingDatabase(context)
            }
            return db
        }

        private fun checkAndRenameDatabase(context: Context, oldName: String, newName: String) {
            val oldDatabaseFile = context.getDatabasePath(oldName)
            if(oldDatabaseFile.exists()) {
                val newDatabaseFile = context.getDatabasePath(newName)

                if(oldDatabaseFile.exists()) {
                    if(newDatabaseFile.exists()) {
                        newDatabaseFile.delete()
                    }
                    oldDatabaseFile.renameTo(newDatabaseFile)
                }
                context.getDatabasePath("$oldName-journal").let {
                    if (it.exists()) {
                        it.delete()
                    }
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase){
        db.execSQL(CREATE_TABLE_APPS)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        throw IllegalStateException("Upgrades not supported")
    }

    fun registerApp(packageName: String, token: String){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(FIELD_PACKAGE_NAME, packageName)
            put(FIELD_TOKEN, token)
        }
        db.insert(TABLE_APPS,null,values)
    }

    fun unregisterApp(token: String){
        val db = writableDatabase
        val selection = "$FIELD_TOKEN = ?"
        val selectionArgs = arrayOf(token)
        db.delete(TABLE_APPS,selection,selectionArgs)
    }

    fun isRegistered(token: String): Boolean {
        val db = readableDatabase
        val selection = "$FIELD_TOKEN = ?"
        val selectionArgs = arrayOf(token)
        return db.query(
                TABLE_APPS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        ).use { cursor ->
            (cursor != null && cursor.count > 0)
        }
    }

    fun getApp(token: String): String{
        val db = readableDatabase
        val projection = arrayOf(FIELD_PACKAGE_NAME)
        val selection = "$FIELD_TOKEN = ?"
        val selectionArgs = arrayOf(token)
        return db.query(
            TABLE_APPS,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            null
        ).use { cursor ->
            val column = cursor.getColumnIndex(FIELD_PACKAGE_NAME)
            if (cursor.moveToFirst() && column >= 0) cursor.getString(column) else ""
        }
    }

    fun listTokens(): List<String>{
        val db = readableDatabase
        val projection = arrayOf(FIELD_TOKEN)
        return db.query(
                TABLE_APPS,
                projection,
                null,
                null,
                null,
                null,
                null
        ).use{ cursor ->
            generateSequence { if (cursor.moveToNext()) cursor else null }
                    .mapNotNull{
                        val column = cursor.getColumnIndex(FIELD_TOKEN)
                        if (column >= 0) it.getString(column) else null }
                    .toList()
        }
    }
}