package org.unifiedpush.distributor.fcm.utils

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import java.util.concurrent.atomic.AtomicBoolean
import org.unifiedpush.android.distributor.ui.R as LibR
import org.unifiedpush.distributor.AppNotification
import org.unifiedpush.distributor.fcm.R
import org.unifiedpush.distributor.fcm.activities.MainActivity
import org.unifiedpush.distributor.fcm.services.MainRegistrationCounter

const val NOTIFICATION_ID_FOREGROUND = 51115

class MainNotificationData(
    title: String,
    text: String,
    ticker: String,
    priority: Int,
    ongoing: Boolean
) : AppNotification.NotificationData(
    smallIcon = R.drawable.ic_launcher_notification,
    title = title,
    text = text,
    ticker = ticker,
    priority = priority,
    ongoing = ongoing,
    activity = MainActivity::class.java
)


class ForegroundNotification(context: Context) : AppNotification(
    context,
    Notifications.ignoreShown,
    NOTIFICATION_ID_FOREGROUND,
    MainNotificationData(
        context.getString(R.string.app_name),
        if (MainRegistrationCounter.oneOrMore(context)) {
            MainRegistrationCounter.getCount(context).let {
                context.resources.getQuantityString(LibR.plurals.foreground_notif_content_with_reg, it, it)
            }
        } else {
            context.getString(LibR.string.foreground_notif_content_no_reg)
        },
        context.getString(LibR.string.foreground_service),
        Notification.PRIORITY_LOW,
        true
    ),
    ChannelData(
        "Foreground",
        context.getString(LibR.string.foreground_service),
        NotificationManager.IMPORTANCE_LOW,
        context.getString(LibR.string.foreground_notif_description)
    )
)

private object Notifications {
    val ignoreShown = AtomicBoolean(true)
}
