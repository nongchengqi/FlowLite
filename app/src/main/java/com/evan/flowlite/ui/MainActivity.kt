package com.evan.flowlite.ui

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.text.format.DateUtils
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.lifecycleScope
import com.evan.flowlite.*
import com.evan.flowlite.float.FloatWindowManager
import com.evan.flowlite.utils.*

import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import java.io.InputStreamReader

class MainActivity : AppCompatActivity(), Shizuku.OnRequestPermissionResultListener {

    private val REQUEST_CODE = 1000
    private val REQUEST_PERMISSION_RESULT_LISTENER: Shizuku.OnRequestPermissionResultListener = this

    private var mBinder: FlowBinder? = null
    private val mConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mBinder = (service as FlowBinder)
            RequestTimer.setOnNotifyUpdateListener { s, s2 ->
                mBinder?.updateNotification(s,s2)
            }
        }
        override fun onServiceDisconnected(name: ComponentName?) {}
    }
    @Stable
    private val listener = object : MainEventListener{
        override fun onItemClick(flow: FlowBean,menu:String) {
            if (flow.id == "0" || flow.id.isEmpty()){
                LogUtil.toast("无效流量包，无法更改")
                return
            }
            when(menu){
                "移动到普通流量"-> {
                    if ( (flow.total.toFloatOrNull()?:0f) == 0f){
                        LogUtil.toast("此套餐为无限流量包，不支持移动到普通流量")
                        return
                    }
                    if (flow.isFree) {
                        AppPref.userNormalMoveIds = AppPref.userNormalMoveIds + "#${flow.id}#"
                    }
                    AppPref.userFreeMoveIds = AppPref.userFreeMoveIds.replace("#${flow.id}#", "")
                    lifecycleScope.launch {
                        RequestTimer.parseFromCache()
                    }
                }
                "移动到免费流量" ->{
                    if (!flow.isFree) {
                        AppPref.userFreeMoveIds = AppPref.userFreeMoveIds + "#${flow.id}#"
                    }
                    AppPref.userNormalMoveIds = AppPref.userNormalMoveIds.replace("#${flow.id}#","")
                    lifecycleScope.launch {
                        RequestTimer.parseFromCache()
                    }
                }
            }
        }

        override fun onStartClick() {
            if (RequestTimer.timerFlow.value){
                lifecycleScope.launch {
                    RequestTimer.refreshFlow()
                }
                LogUtil.log(msg = "刷新点击")
                LogUtil.toast("刷新中...")
            } else {
                LogUtil.log(msg = "启动监控")
                LogUtil.toast("启动中...")
                val result = RequestTimer.launchTimer()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(Intent(this@MainActivity, QueryService::class.java))
                } else {
                    startService(Intent(this@MainActivity, QueryService::class.java))
                }
                if (result) {
                    LogUtil.toast("启动成功")
                }else {
                    LogUtil.toast("启动失败")
                }
            }
        }

        override fun onMenuClick(menu: String) {
            when(menu){
                "停止抓包" -> {
                    CaptureUtil.stopCapture()
                    createMenuItems(true)
                }
                "启动抓包" -> {
                    if (CaptureUtil.checkPermission().isSuccess) {
                        capture()
                    } else {
                        CaptureUtil.requestPermission(this@MainActivity)
                    }
                }
                "配置" -> {
                    startActivity(Intent(this@MainActivity, ConfigActivity::class.java))
                }
                "退出监控" -> {
                    stopService(Intent(this@MainActivity, QueryService::class.java))
                    RequestTimer.stopTimer()
                    finish()
                }
            }
        }
    }
    private var flowInfo by mutableStateOf(SimpleFlowInfoProvider().values.first())
    private var menuItems = mutableStateListOf<Pair<ImageVector,String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isRunning by RequestTimer.timerFlow.collectAsState()
            MaterialTheme {
                MainView(flowInfo,menuItems,isRunning,listener)
            }
        }
        if (AppPref.cookie.isEmpty() && ConfigConst.cookie.isEmpty()){
            LogUtil.toast("Cookie未配置，可以启动自动抓包获取，或者自行配置")
        }
        RequestTimer.initCookie()
        RequestTimer.setOnUpdateListener {
            flowInfo =it
        }
        RequestTimer.switchListenerNetworkChange(AppPref.autoUpdate)
        lifecycleScope.launchWhenResumed {
            RequestTimer.parseFromCache()
        }
        createMenuItems()
        if (!AppPref.floatSwitch || AppPref.floatAndNotifySwitch) {
            bindService(
                Intent(this@MainActivity, QueryService::class.java),
                mConn,
                Service.BIND_AUTO_CREATE
            )
        }
        if (AppPref.floatSwitch) {
           lifecycleScope.launchWhenResumed {
               FloatWindowManager.showView(this@MainActivity)
           }
        }
        // 添加shizuku请求回调，并且判断是否拥有权限，为拥有的话进行申请权限
        Shizuku.addRequestPermissionResultListener(this)
        if (!SZKUtil.checkPermission()) Shizuku.requestPermission(REQUEST_CODE)
    }

    private fun capture(){
        CaptureUtil.setRunningChangeListener {
            createMenuItems(true)
        }
        CaptureUtil.changeVpnRunningStatus(this@MainActivity, true)
        CaptureUtil.listenerCapture {
            CaptureUtil.stopCapture()
            createMenuItems(true)
            LogUtil.toast("成功获取Cookie，请返回应用查看，已自动退出抓包")
        }
    }

    private fun createMenuItems(isVpnChange:Boolean = false){
        val vpn = if (CaptureUtil.vpnRunningStatus()){
            if (isVpnChange){
                LogUtil.toast("正在抓包，请打开联通APP")
                lifecycleScope.launch {
                    val intent = packageManager.getLaunchIntentForPackage("com.sinovatech.unicom.ui")
                    intent?.let {     startActivity(it)  }

                }
            }
            Pair(Icons.Filled.Stop,"停止抓包")
        } else {
            Pair(Icons.Filled.PlayArrow,"启动抓包")
        }
        if (isVpnChange){
            menuItems[1] = vpn
        }else {
            menuItems.add(Pair(Icons.Filled.Tune, "配置"))
            menuItems.add(vpn)
            menuItems.add(Pair(Icons.Filled.ExitToApp, "退出监控"))
        }
    }

    override fun onBackPressed() {
        if (RequestTimer.timerFlow.value){
            backToHome()
        }else {
            super.onBackPressed()
        }
    }

    private fun backToHome(): Boolean {
        return try {
            val startMain = Intent(Intent.ACTION_MAIN)
            startMain.addCategory(Intent.CATEGORY_HOME)
            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(startMain)
            true
        } catch (e: Throwable) {
            false
        }
    }

    override fun onDestroy() {
        FloatWindowManager.removeAllView()
        super.onDestroy()
        unbindService(mConn)
        stopService(Intent(this@MainActivity, QueryService::class.java))
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CaptureUtil.START_VPN_SERVICE_REQUEST_CODE && resultCode == RESULT_OK) {
            capture()
        }
    }

    //shizuku回调方法
    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
        if (grantResult == 0) {
            LogUtil.log(msg = "shizuku权限申请成功")
        } else {
            LogUtil.log(msg = "shizuku权限申请失败")
            Toast.makeText(this, "Shizuku权限申请失败", Toast.LENGTH_SHORT).show()
        }
    }
}