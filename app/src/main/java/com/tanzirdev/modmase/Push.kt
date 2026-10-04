package com.tanzirdev.modmase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging

object Push {
    const val CHANNEL = "modmase_updates"
    val TOPICS = listOf("news", "mods", "apps", "games", "tools")

    fun createChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = ctx.getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(CHANNEL, "MODMASE updates", NotificationManager.IMPORTANCE_HIGH)
            ch.description = "New mods, apps and announcements from MODMASE"
            nm.createNotificationChannel(ch)
        }
    }

    fun notificationsAllowed(ctx: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /** Subscribes / unsubscribes FCM topics. Silently does nothing if Firebase is not configured. */
    fun syncTopics() {
        try {
            val fm = FirebaseMessaging.getInstance()
            val on = Prefs.pushEnabled
            if (on) fm.subscribeToTopic("all") else fm.unsubscribeFromTopic("all")
            TOPICS.forEach { t ->
                if (on && Prefs.topics[t] != false) fm.subscribeToTopic(t) else fm.unsubscribeFromTopic(t)
            }
        } catch (e: Throwable) {
            // google-services.json missing -> push inactive
        }
    }
}
