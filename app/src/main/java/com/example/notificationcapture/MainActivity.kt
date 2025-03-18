// File: app/src/main/java/com/example/notificationcapture/MainActivity.kt
package com.example.notificationcapture

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var notificationText: TextView
    private lateinit var permissionButton: Button
    private lateinit var clearButton: Button
    private val TAG = "MainActivity"

    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                Log.d(TAG, "Broadcast received")
                if (intent?.action == "com.example.notificationcapture.NOTIFICATION_RECEIVED") {
                    val message = intent.getStringExtra("notification_text") ?: "No text"
                    val sender = intent.getStringExtra("notification_sender") ?: "Unknown"
                    val time = intent.getStringExtra("notification_time") ?: ""

                    runOnUiThread {
                        // Create a spannable string to show time in smaller text
                        val timeSpan = SpannableString(" $time")
                        timeSpan.setSpan(RelativeSizeSpan(0.7f), 0, timeSpan.length, 0)
                        timeSpan.setSpan(StyleSpan(Typeface.ITALIC), 0, timeSpan.length, 0)

                        notificationText.append("\n\nFrom: $sender")
                        notificationText.append(timeSpan)
                        notificationText.append("\nMessage: $message")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in broadcast receiver", e)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            notificationText = findViewById(R.id.notificationText)
            permissionButton = findViewById(R.id.permissionButton)
            clearButton = findViewById(R.id.clearButton)

            permissionButton.setOnClickListener {
                // Check if we already have permission
                val notificationListenerString = Settings.Secure.getString(contentResolver,
                    "enabled_notification_listeners")

                if (notificationListenerString == null || !notificationListenerString.contains(packageName)) {
                    // We don't have permission, so ask for it
                    startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                } else {
                    // We already have permission, just restart the service
                    toggleNotificationListenerService()
                }
            }

            clearButton.setOnClickListener {
                notificationText.text = "Captured Messenger Notifications:"
            }

            // Register broadcast receiver
            LocalBroadcastManager.getInstance(this).registerReceiver(
                notificationReceiver,
                IntentFilter("com.example.notificationcapture.NOTIFICATION_RECEIVED")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure the service is running
        toggleNotificationListenerService()
    }

    override fun onDestroy() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        super.onDestroy()
    }

    private fun toggleNotificationListenerService() {
        // This forces the notification service to reconnect
        val packageName = packageName
        packageManager.setComponentEnabledSetting(
            android.content.ComponentName(packageName,
                "$packageName.MessengerNotificationListener"),
            android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            android.content.pm.PackageManager.DONT_KILL_APP
        )
        packageManager.setComponentEnabledSetting(
            android.content.ComponentName(packageName,
                "$packageName.MessengerNotificationListener"),
            android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            android.content.pm.PackageManager.DONT_KILL_APP
        )
    }
}