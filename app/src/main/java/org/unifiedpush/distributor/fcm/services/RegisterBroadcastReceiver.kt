package org.unifiedpush.distributor.fcm.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.concurrent.thread

/**
 * THIS SERVICE IS USED BY OTHER APPS TO REGISTER
 */

class RegisterBroadcastReceiver : BroadcastReceiver() {

    private fun unregisterApp(db: MessagingDatabase, application: String, token: String) {
        // we only trust unregistered demands from the uid who registered the app
        if (db.strictIsRegistered(application, token)) {
            Log.i("RegisterService","Unregistering $application token: $token")
            db.unregisterApp(application, token)
        }
    }

    private fun registerApp(context: Context?, db: MessagingDatabase, application: String, token: String) {
        if (application.isBlank()) {
            Log.w("RegisterService","Trying to register an app without packageName")
            return
        }
        Log.i("RegisterService","registering $application token: $token")
        // The app is registered with the same token : we re-register it
        // the client may need its endpoint again
        if (db.strictIsRegistered(application, token)) {
            Log.i("RegisterService","$application already registered : unregistering to register again")
            unregisterApp(db,application,token)
        }
        // The app is registered with a new token.
        // User should unregister this app manually
        // to avoid an app to impersonate another one
        if (db.isRegistered(application)) {
            val message = "$application already registered with a different token"
            Log.w("RegisterService",message)
            sendRegistrationRefused(context!!,application,token,message)
            return
        }

        db.registerApp(application, token)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent!!.action) {
            ACTION_REGISTER ->{
                Log.i("Register","REGISTER")
                val token = intent.getStringExtra(EXTRA_TOKEN)?: ""
                val application = intent.getStringExtra(EXTRA_APPLICATION)?: ""
                thread(start = true) {
                    val db = MessagingDatabase(context!!)
                    registerApp(context, db, application, token)
                    db.close()
                    Log.i("RegisterService","Registration is finished")
                }.join()
                val endpoint = getEndpoint(context!!, application)
                sendEndpoint(context,application,endpoint)
            }
            ACTION_UNREGISTER ->{
                Log.i("Register","UNREGISTER")
                val token = intent.getStringExtra(EXTRA_TOKEN)?: ""
                val application = intent.getStringExtra(EXTRA_APPLICATION)?: ""
                thread(start = true) {
                    val db = MessagingDatabase(context!!)
                    unregisterApp(db,application, token)
                    db.close()
                    Log.i("RegisterService","Unregistration is finished")
                }
                sendUnregistered(context!!,application,token)
            }
        }
    }
}