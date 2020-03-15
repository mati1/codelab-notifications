package com.mati1.codelabs.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var notifyManager: NotificationManager

    private val receiver = NotificationReceiver()

    private val notificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle("You've been notified!")
                .setContentText("This is your notification text.")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(notificationPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)

    private val notificationPendingIntent: PendingIntent
        get() = PendingIntent.getActivity(
                this,
                NOTIFICATION_ID,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
        )

    private val updateIntent: PendingIntent
        get() = PendingIntent.getBroadcast(
                this,
                NOTIFICATION_ID,
                Intent(ACTION_UPDATE_NOTIFICATION),
                PendingIntent.FLAG_ONE_SHOT
        )

    private val deleteIntent: PendingIntent
        get() = PendingIntent.getBroadcast(
                this,
                NOTIFICATION_ID,
                Intent(ACTION_DELETE_NOTIFICATION),
                PendingIntent.FLAG_ONE_SHOT
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notify.setOnClickListener { sendNotification() }
        cancel.setOnClickListener { cancelNotification() }
        update.setOnClickListener { updateNotification() }

        createNotificationChannel()

        setNotificationButtonState(
                isNotifyEnabled = true,
                isUpdateEnabled = false,
                isCancelEnabled = false
        )

        registerReceiver(receiver, IntentFilter().apply {
            addAction(ACTION_UPDATE_NOTIFICATION)
            addAction(ACTION_DELETE_NOTIFICATION)
        })
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(receiver)
    }

    private fun createNotificationChannel() {
        notifyManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel(PRIMARY_CHANNEL_ID, "Mascot Notification",
                    NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableLights(true)
                enableVibration(true)

                lightColor = Color.RED
                description = "Notification from Mascot"
            }.let(notifyManager::createNotificationChannel)
        }
    }

    private fun sendNotification() = notificationBuilder
            .addAction(R.drawable.ic_update, "Update Notification", updateIntent)
            .setDeleteIntent(deleteIntent)
            .build()
            .let { notification -> notifyManager.notify(NOTIFICATION_ID, notification) }
            .also {
                setNotificationButtonState(
                        isNotifyEnabled = false,
                        isUpdateEnabled = true,
                        isCancelEnabled = true
                )
            }

    private fun cancelNotification() = notifyManager
            .cancel(NOTIFICATION_ID)
            .also {
                setNotificationButtonState(
                        isNotifyEnabled = true,
                        isUpdateEnabled = false,
                        isCancelEnabled = false
                )
            }

    private fun updateNotification() = NotificationCompat
            .BigPictureStyle()
            .bigPicture(BitmapFactory.decodeResource(resources, R.drawable.mascot_1))
            .setBigContentTitle("Notification Updated!")
            .let(notificationBuilder::setStyle)
            .also { notificationBuilder ->
                notifyManager.notify(NOTIFICATION_ID, notificationBuilder.build())

                setNotificationButtonState(
                        isNotifyEnabled = false,
                        isUpdateEnabled = false,
                        isCancelEnabled = true
                )
            }

    private fun setNotificationButtonState(isNotifyEnabled: Boolean,
                                           isUpdateEnabled: Boolean,
                                           isCancelEnabled: Boolean) {
        notify.isEnabled = isNotifyEnabled
        update.isEnabled = isUpdateEnabled
        cancel.isEnabled = isCancelEnabled
    }

    companion object {
        private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"
        private const val NOTIFICATION_ID = 0
        private const val ACTION_UPDATE_NOTIFICATION =
                "com.mati1.codelabs.notifications.ACTION_UPDATE_NOTIFICATION"
        private const val ACTION_DELETE_NOTIFICATION =
                "com.mati1.codelabs.notifications.ACTION_DELETE_NOTIFICATION"
    }

    inner class NotificationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_UPDATE_NOTIFICATION -> updateNotification()
                ACTION_DELETE_NOTIFICATION -> cancelNotification()
            }
        }
    }
}
