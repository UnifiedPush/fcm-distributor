package org.unifiedpush.distributor.fcm.services

import android.content.Context
import android.content.Intent
import android.provider.Settings.Global.getString
import android.util.Log
import org.unifiedpush.distributor.fcm.R

/**
 * These functions are used to send messages to other apps
 */

fun sendMessage(context: Context, token: String, message: String){
    val application = getApp(context, token)!!
    val broadcastIntent = Intent()
    broadcastIntent.`package` = application
    broadcastIntent.action = ACTION_MESSAGE
    broadcastIntent.putExtra(EXTRA_TOKEN, token)
    broadcastIntent.putExtra(EXTRA_MESSAGE, message)
    context.sendBroadcast(broadcastIntent)
}

fun sendEndpoint(context: Context, application: String, endpoint: String) {
    val token = getToken(context,application)!!
    val broadcastIntent = Intent()
    broadcastIntent.`package` = application
    broadcastIntent.action = ACTION_NEW_ENDPOINT
    broadcastIntent.putExtra(EXTRA_TOKEN, token)
    broadcastIntent.putExtra(EXTRA_ENDPOINT, endpoint)
    context.sendBroadcast(broadcastIntent)
}

fun sendUnregistered(context: Context, application: String, _token: String?){
    val token = _token?: getToken(context,application)!!
    val broadcastIntent = Intent()
    broadcastIntent.`package` = application
    broadcastIntent.action = ACTION_UNREGISTERED
    broadcastIntent.putExtra(EXTRA_TOKEN, token)
    context.sendBroadcast(broadcastIntent)
}

fun sendRegistrationFailed(context: Context, application: String, token: String, message: String){
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

fun getToken(context: Context, application: String): String?{
    val db = MessagingDatabase(context)
    val token = db.getToken(application)
    db.close()
    return if (token.isBlank()) {
        Log.w("notifyClient", "No token found for $application")
        null
    } else {
        token
    }
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

fun getEndpoint(context: Context, application: String): String {
    val db = MessagingDatabase(context)
    val appToken = db.getToken(application)
    db.close()
    val settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE)
    val fcmToken = settings?.getString("fcmToken","")
    return context.resources.getString(R.string.default_proxy) +
            "/?token=$fcmToken&app=$appToken"
}