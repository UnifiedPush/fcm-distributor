package org.unifiedpush.distributor.fcm.services

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseRedirectionService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("UP-FCM", "Firebase onNewToken $token")
        val settings = baseContext.getSharedPreferences("Config", Context.MODE_PRIVATE)
        settings.edit().putString("fcmToken",token).commit()
        val db = MessagingDatabase(baseContext)
        val appList = db.listApps()
        db.close()
        appList.forEach{
            sendEndpoint(baseContext, it, getEndpoint(baseContext, it))
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("UP-FCM", "Firebase onMessageReceived ${remoteMessage.messageId}")
        val message = remoteMessage.data["body"]!!
        val appToken = remoteMessage.data["app"]!!
        sendMessage(baseContext, appToken, message)
    }
}