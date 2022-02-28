package org.unifiedpush.distributor.fcm.services

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.unifiedpush.distributor.fcm.services.PushUtils.sendEndpoint
import org.unifiedpush.distributor.fcm.services.PushUtils.sendMessage

private const val TAG = "FirebaseForwardingService"

class FirebaseForwardingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Log.d(TAG, "Firebase onNewToken $token")
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
        Log.d(TAG, "Firebase onMessageReceived ${remoteMessage.messageId}")
        val message = Base64.decode(remoteMessage.data["b"]!!, Base64.DEFAULT)
        val appToken = remoteMessage.data["i"]!!
        sendMessage(baseContext, appToken, message)
    }
}