package org.unifiedpush.distributor.fcm


/*
 * FCM Related constants
 */
internal const val GGL_ENDPOINT = "https://fcm.googleapis.com/fcm/send/%s"
internal const val GATEWAY_ENDPOINT = "https://fcm.distributor.unifiedpush.org/wpfcm?t=%s"
internal const val GATEWAY_VAPID_KEY = "BHNcG_luRWfsMIh1z2YxTNWlWHSMMciR8C3R1fwCdahG2zrnc3DRUltqtohzsiSRyUWsP7euJMxZ6Agb8lxBcHk"


internal const val GSF_PACKAGE = "com.google.android.gms"

internal const val ACTION_FCM_TOKEN_REQUEST = "com.google.iid.TOKEN_REQUEST"
internal const val ACTION_FCM_REGISTRATION = "com.google.android.c2dm.intent.REGISTRATION"
internal const val ACTION_FCM_RECEIVE = "com.google.android.c2dm.intent.RECEIVE"

internal const val EXTRA_APPLICATION_PENDING_INTENT = "app"

/**
 * Internal parameter used to indicate a 'subtype',
 * one subtype is a registration ~= instance
 */
internal const val EXTRA_SUBTYPE = "subtype"
internal const val EXTRA_SUBTYPE_X = "X-subtype"

/** Extra used to indicate which senders (Google API project IDs) can send messages to the app  */
internal const val EXTRA_SENDER = "sender"
internal const val EXTRA_SUB = "subscription"
internal const val EXTRA_SUB_X = "X-subscription"
internal const val EXTRA_SCOPE = "scope"
internal const val EXTRA_KID = "kid"

internal const val EXTRA_REGISTRATION_ID = "registration_id"
internal const val EXTRA_RAW_DATA = "rawData"
// internal const val EXTRA_GOOGLE_MSG_ID = "google.message_id"