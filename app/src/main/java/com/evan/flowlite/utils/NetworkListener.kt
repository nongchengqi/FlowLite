package com.evan.flowlite.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import com.evan.flowlite.App

object NetworkListener {
    private var onAvailable:(()->Unit)? = null
    private var isRegister = false
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            onAvailable?.invoke()
            LogUtil.log(msg = "监听打网络恢复")
        }
    }
    private val connMgr by lazy {
        App.getApp().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun register( onAvailable:(()->Unit)? = null){
        this.onAvailable = onAvailable
        isRegister = true
        connMgr.registerNetworkCallback(NetworkRequest.Builder().build(),networkCallback)
        LogUtil.log(msg = "启动网络变化监听")
    }

    fun unregister(){
        onAvailable = null
        if (isRegister) {
            isRegister = false
            connMgr.unregisterNetworkCallback(networkCallback)
        }
        LogUtil.log(msg = "取消网络变化监听")
    }
}