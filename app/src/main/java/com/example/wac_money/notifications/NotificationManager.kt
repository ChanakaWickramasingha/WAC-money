package com.example.wac_money.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.wac_money.R
import com.example.wac_money.MainActivity

class BudgetNotificationManager(private val context: Context) {
    companion object {
        private const val CHANNEL_ID = "budget_alerts"
        private const val CHANNEL_NAME = "Budget Alerts"
        private const val CHANNEL_DESCRIPTION = "Notifications for budget alerts"
        private const val NOTIFICATION_ID = 1
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBudgetAlert(remainingAmount: Double, percentage: Double, isExceeded: Boolean) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isExceeded) {
            "Budget Exceeded!"
        } else {
            "Budget Alert"
        }

        val message = if (isExceeded) {
            "You have exceeded your budget by ${String.format("%.1f", percentage)}%"
        } else {
            "Remaining budget: $${String.format("%.2f", remainingAmount)} (${String.format("%.1f", percentage)}% used)"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
