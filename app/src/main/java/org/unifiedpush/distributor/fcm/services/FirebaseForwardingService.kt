package org.unifiedpush.distributor.fcm.services

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.unifiedpush.distributor.fcm.services.PushUtils.sendEndpoint
import org.unifiedpush.distributor.fcm.services.PushUtils.sendMessage
import java.util.Timer
import kotlin.concurrent.schedule

private const val TAG = "FirebaseForwardingService"

class FirebaseForwardingService : FirebaseMessagingService() {

    companion object {
        val pendingMessages = mutableMapOf<String, ByteArray>()
    }

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
        Log.d(TAG, "Firebase message ${remoteMessage.messageId} received")
        remoteMessage.data["i"]?.let { appToken ->
            getMessage(remoteMessage.data)?.let { message ->
                sendMessage(baseContext, appToken, message)
            }
        } ?: run {
            (remoteMessage.data["instance"] ?: remoteMessage.data["app"])
                ?.let { appToken ->
                    remoteMessage.data["body"]?.let { message ->
                        sendMessage(baseContext, appToken, message.encodeToByteArray())
                }
            }
        }
    }

    private fun getMessage(data: MutableMap<String, String>): ByteArray? {
        var message: ByteArray? = null
        data["b"]?.let { b64 ->
            data["m"]?.let { mId ->
                data["s"]?.let { splitId ->
                    if (pendingMessages.containsKey(mId)) {
                        Log.d(TAG, "Found pending message")
                        when(splitId) {
                            "1" -> {
                                message = Base64.decode(b64, Base64.DEFAULT) +
                                        pendingMessages[mId]!!
                            }
                            "2" -> {
                                message = pendingMessages[mId]!! +
                                        Base64.decode(b64, Base64.DEFAULT)
                            }
                        }
                        pendingMessages.remove(mId)
                    } else {
                        pendingMessages[mId] = Base64.decode(b64, Base64.DEFAULT)
                        Timer().schedule(3000) {
                            pendingMessages.remove(mId)
                        }
                    }
                }
            }?:run {
                message = Base64.decode(b64, Base64.DEFAULT)
            }
        }
        return message
    }
}