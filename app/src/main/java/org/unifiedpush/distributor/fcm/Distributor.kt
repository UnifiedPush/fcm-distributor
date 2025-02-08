package org.unifiedpush.distributor.fcm

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import org.unifiedpush.distributor.ChannelCreationStatus
import org.unifiedpush.distributor.Database
import org.unifiedpush.distributor.UnifiedPushDistributor

/**
 * These functions are used to send messages to other apps
 */
object Distributor : UnifiedPushDistributor() {
    override fun getDb(context: Context): Database {
        return DatabaseFactory.getDb(context)
    }

    @OptIn(ExperimentalUuidApi::class)
    override fun registerChannelIdToServer(
        context: Context,
        packageName: String,
        channelId: String?,
        title: String?,
        vapid: String?,
        callback: (ChannelCreationStatus) -> Unit
    ) {
        val channelId = channelId ?: Uuid.random().toString()
        val useGateway = vapid == null
        val vapid = vapid ?: GATEWAY_VAPID_KEY
        registerFCM(context, channelId, vapid, useGateway)
        // We call the callback even without the endpoint, it will send the REGISTER later
        callback(ChannelCreationStatus.Ok(
            channelId = channelId,
            sendEndpoint = false
        ))
    }

    override fun unregisterChannelIdToServer(context: Context, appToken: String, callback: (Boolean) -> Unit) {
        // TODO: see how to unregister a channel from FCM
        callback(true)
    }

    /** Register to Google services */
    private fun registerFCM(context: Context, channelId: String, vapid: String, useGateway: Boolean) {
        val subtype = "wp:$channelId"
        // kid is the registration id, it is return when we receive a new
        // token. The extra contains "KID:TOKEN"
        val kid = if (useGateway) "1:$channelId" else "0:$channelId"
        val intent = Intent(ACTION_FCM_TOKEN_REQUEST).apply {
            `package` = GSF_PACKAGE
            putExtra(EXTRA_SCOPE, "GCM")
            putExtra(EXTRA_SENDER, vapid)
            putExtra(EXTRA_SUB, vapid)
            putExtra(EXTRA_SUB_X, vapid)
            putExtra(EXTRA_SUBTYPE, subtype)
            putExtra(EXTRA_SUBTYPE_X, subtype)
            // When we receive a new token, the extra contains
            // "KID:TOKEN"
            putExtra(EXTRA_KID, kid)
            putExtra(
                EXTRA_APPLICATION_PENDING_INTENT,
                PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
        context.sendBroadcast(intent)
    }
}
