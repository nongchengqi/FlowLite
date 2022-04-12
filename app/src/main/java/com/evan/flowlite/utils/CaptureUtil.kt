package com.evan.flowlite.utils

import android.app.Activity
import android.content.Context
import android.net.VpnService
import com.evan.flowlite.App
import kotlinx.coroutines.*

object CaptureUtil {
    private var captureJob: Job? = null
    private var isCaptureSuccess = false
    const val START_VPN_SERVICE_REQUEST_CODE = 2022//VpnServiceHelper.START_VPN_SERVICE_REQUEST_CODE

    fun init(context: Context){
       /* CaptureCore.init(context)
        CaptureCore.setSelectPkg("com.sinovatech.unicom.ui")
        if (CaptureCore.getNotification() != null) {
            CaptureCore.setNotification(
                NotificationHelper.getNotification(
                    title = "Cookie抓包进行中",
                    desc = "请打开联通APP首页，无需其他操作，等待获取cookie成功提示即可",
                    clickIntent = Intent(App.getApp(), MainActivity::class.java),
                    smallIcon = R.mipmap.ic_launcher_round
                )
            )
        }*/
    }

    private fun startTimer(captureSuc:()->Unit) {
        /*LogUtil.log(msg = "抓包监听中...")
        val allNetConnection = VpnServiceHelper.getAllSession() ?: return
        val iterator: MutableIterator<NatSession> = allNetConnection.iterator()
        val packageName: String = App.getApp().packageName

        val isShowUDP = true
        val selectPackage = CaptureCore.getSelectPkg()
        while (iterator.hasNext() && !isCaptureSuccess) {
            val next = iterator.next()
            if (next.bytesSent == 0 && next.receiveByteNum == 0L) {
                iterator.remove()
                continue
            }
            if (NatSession.UDP == next.type && !isShowUDP) {
                iterator.remove()
                continue
            }
            val appInfo = next.appInfo
            if (appInfo != null) {
                val appPackageName = appInfo.pkgs.getAt(0)
                if (packageName == appPackageName) {
                    iterator.remove()
                    continue
                }
                if (selectPackage != null && selectPackage != appPackageName) {
                    iterator.remove()
                }
            }
            LogUtil.log(msg = "抓取请求：${next.remoteHost}")
            if (next.remoteHost.contains("smartad.10010.com")) {
                LogUtil.log(msg = "成功捉到包含ookie的请求：${next.remoteHost}")
                val dir = (VPNConstants.DATA_DIR
                        + TimeFormatUtil.formatYYMMDDHHMMSS(next.vpnStartTime)
                        + "/"
                        + next.uniqueName)
                Log.e("TEST", "${dir}")
                parseCookie(dir,captureSuc)
            }

        }*/

    }
    fun listenerCapture(captureSuc:()->Unit){
        isCaptureSuccess = false
        captureJob = MainScope().launch(Dispatchers.IO) {
            while (isActive){
                delay(1000)
                try {
                    startTimer(captureSuc)
                } catch (e:Exception){
                    LogUtil.log(msg = "抓包过程出现异常: ${e.message}")
                }
            }
        }
    }

    fun checkPermission(): Result<Unit> {
        return kotlin.runCatching {
            val intent = VpnService.prepare(App.getApp())
            if (intent != null) {
                throw Throwable("no vpn Permission")
            }
        }
    }

    fun requestPermission(activity: Activity){
        kotlin.runCatching {
            val intent = VpnService.prepare(App.getApp())
            if (intent != null) {
                activity.startActivityForResult(intent,START_VPN_SERVICE_REQUEST_CODE)
            }
        }
    }

    fun stopCapture(){
        isCaptureSuccess = false
        LogUtil.log(msg = "停止抓包")
        captureJob?.cancel()
   /*     VpnServiceHelper.removeRunningChangeListener()
        VpnServiceHelper.changeVpnRunningStatus(App.getApp(), false)*/

    }

    fun setRunningChangeListener(cb:(Boolean) ->Unit){
      /*  VpnServiceHelper.setRunningChangeListener {
            cb.invoke(it)
        }*/
    }

    fun vpnRunningStatus() = false//VpnServiceHelper.vpnRunningStatus()

    fun changeVpnRunningStatus(context: Context, run:Boolean){
        //VpnServiceHelper.changeVpnRunningStatus(context, run)
    }

    private fun parseCookie(filePath:String,captureSuc:()->Unit) {
        /*LogUtil.log(msg = "开始解析cookie")
        val file = File(filePath)
        val files = file.listFiles()
        if (files == null || files.isEmpty()) {
            return
        }
        val filesList: MutableList<File> = ArrayList()
        for (childFile in files) {
            filesList.add(childFile)
        }

        val showDataList = mutableListOf<ShowData>()
        for (childFile in filesList) {
            val showData = SaveDataFileParser.parseSaveFile(childFile)
            if (showData != null) {
                showDataList.add(showData)
            }
            val data = showData.headStr.split("\n")
            val cookie = StringBuilder()
            data.forEach {
                if (it.startsWith("Cookie:")) {
                    cookie.append(it.replace("Cookie:", ""))
                    cookie.append(";")
                }
            }
            if (cookie.length > 100) {
                isCaptureSuccess = true
                LogUtil.log(msg = "成获取cookie")
                captureSuc.invoke()
                AppPref.cookie = cookie.toString()
                ConfigConst.cookie = AppPref.cookie
                captureJob?.cancel()
                return
            }
        }
        */
    }
}