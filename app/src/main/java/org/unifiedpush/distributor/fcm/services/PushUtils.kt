package org.unifiedpush.distributor.fcm.services

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import org.unifiedpush.distributor.fcm.R

/**
 * These functions are used to send messages to other apps
 */

private const val TAG = "PushUtils"

object PushUtils {
    fun sendMessage(context: Context, token: String, message: ByteArray) {
        Intent().apply {
            `package` = getApp(context, token) ?: return
            action = ACTION_MESSAGE
            putExtra(EXTRA_TOKEN, token)
            putExtra(EXTRA_MESSAGE, String(message))
            putExtra(EXTRA_BYTES_MESSAGE, message)
        }.let { intent ->
            context.sendBroadcast(intent)
        }
    }

    fun sendEndpoint(context: Context, token: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "FcmToken successfully received")
                task.result
            } else {
                Log.w(
                    TAG, "FCMToken registration failed: " +
                            "${task.exception?.localizedMessage}"
                )
                null
            }?.let { fcmToken ->
                Intent().apply {
                    `package` = getApp(context, token) ?: return@addOnCompleteListener
                    action = ACTION_NEW_ENDPOINT
                    putExtra(EXTRA_TOKEN, token)
                    putExtra(EXTRA_ENDPOINT, getEndpoint(context, fcmToken, token))
                }.let { intent ->
                    context.sendBroadcast(intent)
                }
            }
        }
    }

    fun sendUnregistered(context: Context, token: String) {
        Intent().apply {
            `package` = getApp(context, token) ?: return
            action = ACTION_UNREGISTERED
            putExtra(EXTRA_TOKEN, token)
        }.let { intent ->
            context.sendBroadcast(intent)
        }
    }

    private fun getApp(context: Context, token: String): String? {
        val db = MessagingDatabase(context)
        val app = db.getApp(token)
        db.close()
        return app.ifBlank {
            Log.w(TAG, "No app found for $token")
            null
        }
    }

    private fun getEndpoint(context: Context, fcmToken: String, appToken: String): String {
        return context.resources.getString(R.string.default_proxy) +
                "?v2&token=$fcmToken&instance=$appToken"
    }
}