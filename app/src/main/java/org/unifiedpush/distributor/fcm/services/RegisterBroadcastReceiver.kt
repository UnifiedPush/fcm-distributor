package org.unifiedpush.distributor.fcm.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.unifiedpush.distributor.fcm.services.MessagingDatabase.Companion.getDb
import org.unifiedpush.distributor.fcm.services.PushUtils.sendEndpoint
import org.unifiedpush.distributor.fcm.services.PushUtils.sendUnregistered
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

/**
 * THIS SERVICE IS USED BY OTHER APPS TO REGISTER
 */

private const val TAG = "RegisterBroadcastReceiver"

class RegisterBroadcastReceiver : BroadcastReceiver() {

    private fun unregisterApp(db: MessagingDatabase, token: String) {
        Log.i(TAG, "Unregistering app with token: $token")
        db.unregisterApp(token)
    }

    private fun registerApp(db: MessagingDatabase, application: String, token: String) {
        if (application.isBlank()) {
            Log.w(TAG, "Trying to register an app without packageName")
            return
        }
        Log.i(TAG, "registering $application token: $token")
        if (db.isRegistered(token)) {
            Log.i(TAG, "$application already registered")
            return
        }
        db.registerApp(application, token)
    }

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            ACTION_REGISTER ->{
                //We do not check connector version, we handle all
                Log.i(TAG, "REGISTER")
                val connectorToken = intent.getStringExtra(EXTRA_TOKEN)?: return
                val application = intent.getStringExtra(EXTRA_APPLICATION)?: return
                if (!createQueue.containsTokenElseAdd(connectorToken)) {
                    thread(start = true) {
                        registerApp(getDb(context), application, connectorToken)
                        Log.i(TAG, "Registration is finished")
                        sendEndpoint(context, connectorToken)
                        createQueue.removeToken(connectorToken)
                    }.join()
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