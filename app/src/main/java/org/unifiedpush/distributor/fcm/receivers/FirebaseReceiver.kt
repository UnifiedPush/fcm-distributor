package org.unifiedpush.distributor.fcm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.unifiedpush.distributor.fcm.ACTION_FCM_RECEIVE
import org.unifiedpush.distributor.fcm.ACTION_FCM_REGISTRATION
import org.unifiedpush.distributor.fcm.DatabaseFactory
import org.unifiedpush.distributor.fcm.Distributor
import org.unifiedpush.distributor.fcm.EXTRA_RAW_DATA
import org.unifiedpush.distributor.fcm.EXTRA_REGISTRATION_ID
import org.unifiedpush.distributor.fcm.EXTRA_SUBTYPE
import org.unifiedpush.distributor.fcm.GATEWAY_ENDPOINT
import org.unifiedpush.distributor.fcm.GGL_ENDPOINT
import org.unifiedpush.distributor.fcm.utils.TAG

/**
 * This receivers interacts with Google Services and receives FCM message. It is exposed by the library.
 */
class FirebaseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_FCM_REGISTRATION -> {
                if (intent.hasExtra(EXTRA_REGISTRATION_ID)) {
                    val registration = intent.getStringExtra(EXTRA_REGISTRATION_ID)?.split(":", limit = 3) ?: return
                    if (registration.size != 3) {
                        Log.d(TAG, "Cannot retrieve registration info, aborting.")
                        return
                    }
                    val useGateway = registration[0] == "1"
                    val channelId = registration[1]
                    val fcmToken = registration[2]
                    onNewEndpoint(context, channelId, fcmToken, useGateway)
                    Log.i(
                        TAG,
                        "Successfully registered for FCM"
                    )
                } else {
                    Log.e(
                        TAG,
                        "FCM registration intent did not contain registration_id: $intent"
                    )
                    val extras = intent.extras
                    for (key in extras!!.keySet()) {
                        Log.i(
                            TAG,
                            key + " -> " + extras[key]
                        )
                    }
                }
            }
            ACTION_FCM_RECEIVE -> {
                val channelId = intent.getStringExtra(EXTRA_SUBTYPE)?.let {
                    if (it.startsWith("wp:")) {
                        it.substring(3)
                    } else {
                        Log.d(TAG, "Received message for unknown subtype. Starting with ${it.substring(0,3)}")
                        null
                    }
                } ?: return
                val message = intent.getByteArrayExtra(EXTRA_RAW_DATA) ?: return
                // val messageId = intent.getStringExtra(EXTRA_GOOGLE_MSG_ID)
                Distributor.sendMessage(context, channelId, message)
            }
        }
    }

    private fun onNewEndpoint(context: Context, channelId: String, fcmToken: String, useGateway: Boolean) {
        val endpoint = if (useGateway) {
            GATEWAY_ENDPOINT.format(fcmToken)
        } else {
            GGL_ENDPOINT.format(fcmToken)
        }
        DatabaseFactory.getDb(context).saveEndpoint(channelId, endpoint)
        Distributor.sendEndpointForChannel(context, channelId)
    }
}
