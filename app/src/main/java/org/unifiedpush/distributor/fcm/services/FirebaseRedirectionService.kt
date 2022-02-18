package org.unifiedpush.distributor.fcm.services

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.unifiedpush.distributor.fcm.services.PushUtils.sendEndpoint
import org.unifiedpush.distributor.fcm.services.PushUtils.sendMessage

class FirebaseRedirectionService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d("UP-FCM", "Firebase onNewToken $token")
        val settings = baseContext.getSharedPreferences("Config", Context.MODE_PRIVATE)
        settings.edit().putString("fcmToken",token).commit()
        val db = MessagingDatabase(baseContext)
        val tokenList = db.listTokens()
        db.close()
        tokenList.forEach{
            sendEndpoint(baseContext, it)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("UP-FCM", "Firebase onMessageReceived ${remoteMessage.messageId}")
        val message = Base64.decode(remoteMessage.data["body"]!!, Base64.DEFAULT)
        val appToken = remoteMessage.data["app"]!!
        sendMessage(baseContext, appToken, message)
    }
}