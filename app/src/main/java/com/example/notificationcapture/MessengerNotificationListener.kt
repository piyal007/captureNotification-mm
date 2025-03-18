// File: app/src/main/java/com/example/notificationcapture/MessengerNotificationListener.kt
package com.example.notificationcapture

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MessengerNotificationListener : NotificationListenerService() {

    private val MESSENGER_PACKAGE = "com.facebook.orca"
    private val TAG = "NotificationListener"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Notification Listener Service created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            Log.d(TAG, "Notification received from: ${sbn.packageName}")

            // Filter notifications from Messenger only
            if (sbn.packageName == MESSENGER_PACKAGE) {
                val extras = sbn.notification.extras

                // Get notification text content - handle null safely
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""

                Log.d(TAG, "Messenger notification: $title - $text")

                // Broadcast the captured notification
                val intent = Intent("com.example.notificationcapture.NOTIFICATION_RECEIVED")
                intent.putExtra("notification_text", text)
                intent.putExtra("notification_sender", title)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Not handling removed notifications
    }
}