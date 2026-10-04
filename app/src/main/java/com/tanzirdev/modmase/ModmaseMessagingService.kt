package com.tanzirdev.modmase

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ModmaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val n = message.notification
        val data = message.data
        val title = n?.title ?: data["title"] ?: "MODMASE"
        val body = n?.body ?: data["body"] ?: ""

        val open = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("type", data["type"])
            putExtra("id", data["id"])
        }
        val pending = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            open,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        Push.createChannel(this)
        val notification = NotificationCompat.Builder(this, Push.CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_modmase)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFF3FD11F.toInt())
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        try {
            NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), notification)
        } catch (e: SecurityException) {
            // notification permission not granted
        }
    }

    override fun onNewToken(token: String) {
        // Topic based delivery - no token handling needed.
    }
}
