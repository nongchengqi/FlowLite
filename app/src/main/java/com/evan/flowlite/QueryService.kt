package com.evan.flowlite

import android.app.Service
import android.content.Intent
import android.os.*
import com.evan.flowlite.ui.MainActivity
import com.evan.flowlite.utils.NotificationHelper

class QueryService : Service() {
    override fun onCreate() {
        super.onCreate()
    }
    fun updateNotification(title:String,desc:String){
        startForeground(
            10010, NotificationHelper.getNotification(
                title = title,
                desc = desc,
                clickIntent = Intent(this, MainActivity::class.java),
                smallIcon = R.mipmap.ic_launcher_round
            )
        )
    }
    override fun onBind(intent: Intent): IBinder {
        return FlowBinder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            10010, NotificationHelper.getNotification(
                title = "正在等待信息更新",
                desc = "如果没配置Cookie，请配置好并启动服务",
                clickIntent = Intent(this, MainActivity::class.java),
                smallIcon = R.mipmap.ic_launcher_round
            )
        )
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }
}

class FlowBinder(private val service: QueryService) : Binder() {
    fun updateNotification(title:String,desc:String){
        service.updateNotification(title, desc)
    }
}