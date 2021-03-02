package org.unifiedpush.distributor.fcm.services

import android.content.Context
import android.content.Intent
import android.util.Log
import org.unifiedpush.distributor.fcm.R

/**
 * These functions are used to send messages to other apps
 */

fun sendMessage(context: Context, token: String, message: String){
    val application = getApp(context, token)
    if (application.isNullOrBlank()) {
        return
    }
    val broadcastIntent = Intent()
    broadcastIntent.`package` = application
    broadcastIntent.action = ACTION_MESSAGE
    broadcastIntent.putExtra(EXTRA_TOKEN, token)
    broadcastIntent.putExtra(EXTRA_MESSAGE, message)
    context.sendBroadcast(broadcastIntent)
}

fun sendEndpoint(context: Context, token: String, endpoint: String) {
    val application = getApp(context, token)
    if (application.isNullOrBlank()) {
        return
    }
    val broadcastIntent = Intent()
    broadcastIntent.`package` = application
    broadcastIntent.action = ACTION_NEW_ENDPOINT
    broadcastIntent.putExtra(EXTRA_TOKEN, token)
    broadcastIntent.putExtra(EXTRA_ENDPOINT, endpoint)
    context.sendBroadcast(broadcastIntent)
}

fun sendUnregistered(context: Context, token: String) {
    val application = getApp(context, token)
    if (application.isNullOrBlank()) {
        return
    }
    val broadcastIntent = Intent()
    broadcastIntent.`package` = application
    broadcastIntent.action = ACTION_UNREGISTERED
    broadcastIntent.putExtra(EXTRA_TOKEN, token)
    context.sendBroadcast(broadcastIntent)
}

fun sendRegistrationFailed(context: Context, application: String, token: String, message: String) {
    val broadcastIntent = Intent()
    broadcastIntent.`package` = application
    broadcastIntent.action = ACTION_REGISTRATION_FAILED
    broadcastIntent.putExtra(EXTRA_TOKEN, token)
    broadcastIntent.putExtra(EXTRA_MESSAGE, message)
    context.sendBroadcast(broadcastIntent)
}

fun sendRegistrationRefused(context: Context, application: String, token: String, message: String){
    val broadcastIntent = Intent()
    broadcastIntent.`package` = application
    broadcastIntent.action = ACTION_REGISTRATION_REFUSED
    broadcastIntent.putExtra(EXTRA_TOKEN, token)
    broadcastIntent.putExtra(EXTRA_MESSAGE, message)
    context.sendBroadcast(broadcastIntent)
}

fun getApp(context: Context, token: String): String?{
    val db = MessagingDatabase(context)
    val app = db.getApp(token)
    db.close()
    return if (app.isBlank()) {
        Log.w("notifyClient", "No app found for $token")
        null
    } else {
        app
    }
}

fun getEndpoint(context: Context, appToken: String): String {
    val settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE)
    val fcmToken = settings?.getString("fcmToken","")
    return context.resources.getString(R.string.default_proxy) +
            "/?token=$fcmToken&app=$appToken"
}