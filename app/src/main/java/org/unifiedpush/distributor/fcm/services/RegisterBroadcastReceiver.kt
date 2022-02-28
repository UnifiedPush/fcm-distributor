package org.unifiedpush.distributor.fcm.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.unifiedpush.distributor.fcm.services.PushUtils.sendEndpoint
import org.unifiedpush.distributor.fcm.services.PushUtils.sendUnregistered
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

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent!!.action) {
            ACTION_REGISTER ->{
                //We do not check connector version, we handle all
                Log.i(TAG, "REGISTER")
                val token = intent.getStringExtra(EXTRA_TOKEN)?: return
                val application = intent.getStringExtra(EXTRA_APPLICATION)?: return
                thread(start = true) {
                    val db = MessagingDatabase(context!!)
                    registerApp(db, application, token)
                    db.close()
                    Log.i(TAG, "Registration is finished")
                }.join()
                sendEndpoint(context!!, token)
            }
            ACTION_UNREGISTER ->{
                Log.i("Register", "UNREGISTER")
                val token = intent.getStringExtra(EXTRA_TOKEN)?: return
                thread(start = true) {
                    val db = MessagingDatabase(context!!)
                    unregisterApp(db, token)
                    db.close()
                    Log.i(TAG, "Unregistration is finished")
                }
                sendUnregistered(context!!, token)
            }
        }
    }
}