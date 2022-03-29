package com.evan.flowlite

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val FLOAT_CODE = 1001
    private val sNotificationManager by lazy {
        App.getApp().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun showFloatNotification(
        title: String,
        desc: String,
        clickIntent: Intent,
        @DrawableRes smallIcon: Int,
        bigIcon: Bitmap?,
        channelId: String = "def_notify",
        channelName: String = "float_notify",
        notifyId: Int = FLOAT_CODE
    ) {
        showNotification(
            title,
            desc,
            clickIntent,
            channelId,
            channelName,
            smallIcon,
            bigIcon,
            notifyId,
            true
        )
    }


    fun showNormalNotification(
        title: String,
        desc: String,
        clickIntent: Intent,
        @DrawableRes smallIcon: Int,
        bigIcon: Bitmap?,
        channelId: String = "def_notify",
        channelName: String = "def_notify",
        notifyId: Int = FLOAT_CODE
    ) {
        showNotification(
            title,
            desc,
            clickIntent,
            channelId,
            channelName,
            smallIcon,
            bigIcon,
            notifyId,
            false
        )
    }


    private fun showNotification(
        title: String,
        desc: String,
        clickIntent: Intent,
        channelId: String ,
        channelName: String ,
        @DrawableRes smallIcon: Int,
        bigIcon: Bitmap?,
        notifyId: Int = FLOAT_CODE,
        isFloat: Boolean
    ) {
        sNotificationManager.notify(notifyId, getNotification(title, desc, clickIntent, channelId, channelName, smallIcon, bigIcon, isFloat))
    }


    fun getNotification(
        title: String,
        desc: String,
        clickIntent: Intent,
        channelId: String = "def_notify",
        channelName: String = "def_notify",
        @DrawableRes smallIcon: Int,
        bigIcon: Bitmap? = null,
        isFloat: Boolean = false
    ):Notification {
        val pendingIntent = PendingIntent.getActivity(
            App.getApp(),
            FLOAT_CODE,
            clickIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            mChannel.setShowBadge(true)
            mChannel.enableLights(true)
            sNotificationManager.createNotificationChannel(mChannel)
        }
        val builder = NotificationCompat.Builder(App.getApp(), channelId)
            .setContentTitle(title)
            .setContentText(desc)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(smallIcon)
            .setDefaults(if (isFloat) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setPriority(if (isFloat) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            //.setDefaults(NotificationCompat.DEFAULT_SOUND)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        if (bigIcon != null) {
            builder.setLargeIcon(
                bigIcon
            ).setStyle(
                NotificationCompat.BigPictureStyle()
                    .setBigContentTitle(title)
                    .bigLargeIcon(bigIcon)
                    .bigPicture(bigIcon)
            )
        }
        return builder.build()
    }

}