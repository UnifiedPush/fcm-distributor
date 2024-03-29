package org.unifiedpush.distributor.fcm.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import org.unifiedpush.distributor.fcm.Database
import org.unifiedpush.distributor.fcm.Database.Companion.getDb
import org.unifiedpush.distributor.fcm.distributor.Distributor.sendEndpoint
import org.unifiedpush.distributor.fcm.distributor.Distributor.sendUnregistered
import org.unifiedpush.distributor.fcm.distributor.ACTION_REGISTER
import org.unifiedpush.distributor.fcm.distributor.ACTION_UNREGISTER
import org.unifiedpush.distributor.fcm.distributor.EXTRA_APPLICATION
import org.unifiedpush.distributor.fcm.distributor.EXTRA_TOKEN
import org.unifiedpush.distributor.fcm.utils.getApplicationName
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

/**
 * THIS SERVICE IS USED BY OTHER APPS TO REGISTER
 */

private const val TAG = "RegisterBroadcastReceiver"

class RegisterBroadcastReceiver : BroadcastReceiver() {

    private fun unregisterApp(db: Database, token: String) {
        Log.i(TAG, "Unregistering app with token: $token")
        db.unregisterApp(token)
    }

    // Returns true if it register a new app
    private fun registerApp(db: Database, application: String, token: String): Boolean {
        if (application.isBlank()) {
            Log.w(TAG, "Trying to register an app without packageName")
            return false
        }
        Log.i(TAG, "registering $application token: $token")
        if (db.isRegistered(token)) {
            Log.i(TAG, "$application already registered")
            return false
        }
        db.registerApp(application, token)
        return true
    }

    override fun onReceive(rContext: Context, intent: Intent?) {
        val context = rContext.applicationContext
        when (intent?.action) {
            ACTION_REGISTER ->{
                //We do not check connector version, we handle all
                Log.i(TAG, "REGISTER")
                val connectorToken = intent.getStringExtra(EXTRA_TOKEN)?: return
                val application = intent.getStringExtra(EXTRA_APPLICATION)?: return
                if (!createQueue.containsTokenElseAdd(connectorToken)) {
                    var toast = false
                    thread(start = true) {
                        toast = registerApp(getDb(context), application, connectorToken)
                        Log.i(TAG, "Registration is finished")
                        sendEndpoint(context, connectorToken)
                        createQueue.removeToken(connectorToken)
                    }.join()
                    if (toast) {
                        val appName = context.getApplicationName(application) ?: application
                        Toast.makeText(context, "$appName registered.", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Log.d(TAG, "Already registering this token")
                }
            }
            ACTION_UNREGISTER ->{
                Log.i("Register", "UNREGISTER")
                val connectorToken = intent.getStringExtra(EXTRA_TOKEN)?: return
                if (!delQueue.containsTokenElseAdd(connectorToken)) {
                    thread(start = true) {
                        unregisterApp(getDb(context), connectorToken)
                        Log.i(TAG, "Unregistration is finished")
                        sendUnregistered(context, connectorToken)
                        delQueue.removeToken(connectorToken)
                    }
                } else {
                    Log.d(TAG, "Already deleting this token")
                }
            }
        }
    }

    private fun MutableList<String>.containsTokenElseAdd(connectorToken: String): Boolean {
        return synchronized(this) {
            if (connectorToken !in this) {
                Log.d(TAG, "Token: $this")
                this.add(connectorToken)
                delayRemove(this, connectorToken)
                false
            } else {
                true
            }
        }
    }

    private fun MutableList<String>.removeToken(connectorToken: String) {
        synchronized(this) {
            this.remove(connectorToken)
        }
    }

    private fun delayRemove(list: MutableList<String>, token: String) {
        Timer().schedule(1_000L /* 1sec */) {
            list.removeToken(token)
        }
    }

    companion object {
        private val createQueue = emptyList<String>().toMutableList()
        private val delQueue = emptyList<String>().toMutableList()
    }
}