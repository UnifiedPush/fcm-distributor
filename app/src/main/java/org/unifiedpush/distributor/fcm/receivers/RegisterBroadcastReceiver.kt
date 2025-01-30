@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.unifiedpush.distributor.fcm.receivers

import android.content.Context
import org.unifiedpush.distributor.fcm.Distributor
import org.unifiedpush.distributor.fcm.callback.NetworkCallbackFactory
import org.unifiedpush.distributor.receiver.DistributorReceiver

/**
 * THIS SERVICE IS USED BY OTHER APPS TO REGISTER
 */
class RegisterBroadcastReceiver : DistributorReceiver() {

    override val distributor = Distributor

    override fun isConnected(context: Context): Boolean {
        // We don't have to care about login
        return true
    }

    override fun hasInternet(context: Context): Boolean {
        return NetworkCallbackFactory.hasInternet()
    }

    /**
     * We handle known and updated token as new tokens
     */
    override fun onRegisterKnownToken(context: Context, connectorToken: String, application: String, vapid: String?) {
        onRegisterUpdatedToken(context, connectorToken, application, vapid)
    }
}
