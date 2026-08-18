package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.domain.model.ReceiptItem

object NotificationHelper {
    const val CHANNEL_ID = "warranty_reminders_channel"
    private const val CHANNEL_NAME = "Warranty Expiry Alerts"
    private const val CHANNEL_DESC = "Notifications for upcoming warranty expirations"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showExpiryReminder(context: Context, receipt: ReceiptItem) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("receipt_id", receipt.id)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            receipt.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val daysLeft = receipt.remainingDays
        val timeText = when {
            daysLeft < 0 -> "has expired"
            daysLeft == 0L -> "expires today!"
            daysLeft == 1L -> "expires tomorrow!"
            else -> "expires in $daysLeft days (${receipt.formattedExpiryDate})"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⚠️ Warranty Alert: ${receipt.merchantName}")
            .setContentText("${receipt.itemName} warranty $timeText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your warranty for '${receipt.itemName}' ($${String.format("%.2f", receipt.totalAmount)}) from ${receipt.merchantName} $timeText. Tap to view receipt and warranty proof.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(receipt.id.toInt(), notification)
        } catch (_: SecurityException) {
            // Notification permission might not be granted
        }
    }

    fun showTestNotification(context: Context, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(9999, notification)
        } catch (_: SecurityException) {
            // Ignore if permission not yet granted
        }
    }
}
