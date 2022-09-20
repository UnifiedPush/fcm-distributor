package org.unifiedpush.distributor.fcm.services

import android.util.Base64
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.unifiedpush.distributor.fcm.services.MessagingDatabase.Companion.getDb
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
        getDb(baseContext).listTokens().forEach{
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
                        message = when (splitId) {
                            "1" -> {
                                Base64.decode(b64, Base64.DEFAULT) +
                                        (pendingMessages[mId] ?: ByteArray(0))
                            }
                            "2" -> {
                                (pendingMessages[mId] ?: ByteArray(0)) +
                                        Base64.decode(b64, Base64.DEFAULT)
                            }
                            else -> ByteArray(0)
                        }
                        pendingMessages.remove(mId)
                    } else {
                        pendingMessages[mId] = Base64.decode(b64, Base64.DEFAULT)
                        Timer().schedule(3000) {
                            pendingMessages.remove(mId)
                        }
                    }
                }
            } ?: run {
                return Base64.decode(b64, Base64.DEFAULT)
            }
        }
        return message
    }
}