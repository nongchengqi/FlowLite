package com.evan.flowlite.ui

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
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
import com.evan.flowlite.utils.AppPref
import com.evan.flowlite.utils.CaptureUtil
import com.evan.flowlite.utils.LogUtil
import com.evan.flowlite.utils.RequestTimer

import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
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
                LogUtil.toast("??????????????????????????????")
                return
            }
            when(menu){
                "?????????????????????"-> {
                    if ( (flow.total.toFloatOrNull()?:0f) == 0f){
                        LogUtil.toast("????????????????????????????????????????????????????????????")
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
                "?????????????????????" ->{
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
                LogUtil.log(msg = "????????????")
                LogUtil.toast("?????????...")
            } else {
                LogUtil.log(msg = "????????????")
                LogUtil.toast("?????????...")
                val result = RequestTimer.launchTimer()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(Intent(this@MainActivity, QueryService::class.java))
                } else {
                    startService(Intent(this@MainActivity, QueryService::class.java))
                }
                if (result) {
                    LogUtil.toast("????????????")
                }else {
                    LogUtil.toast("????????????")
                }
            }
        }

        override fun onMenuClick(menu: String) {
            when(menu){
                "????????????" -> {
                    CaptureUtil.stopCapture()
                    createMenuItems(true)
                }
                "????????????" -> {
                    if (CaptureUtil.checkPermission().isSuccess) {
                        capture()
                    } else {
                        CaptureUtil.requestPermission(this@MainActivity)
                    }
                }
                "??????" -> {
                    startActivity(Intent(this@MainActivity, ConfigActivity::class.java))
                }
                "????????????" -> {
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
            LogUtil.toast("Cookie???????????????????????????????????????????????????????????????")
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
    }

    private fun capture(){
        CaptureUtil.setRunningChangeListener {
            createMenuItems(true)
        }
        CaptureUtil.changeVpnRunningStatus(this@MainActivity, true)
        CaptureUtil.listenerCapture {
            CaptureUtil.stopCapture()
            createMenuItems(true)
            LogUtil.toast("????????????Cookie????????????????????????????????????????????????")
        }
    }

    private fun createMenuItems(isVpnChange:Boolean = false){
        val vpn = if (CaptureUtil.vpnRunningStatus()){
            if (isVpnChange){
                LogUtil.toast("??????????????????????????????APP")
                lifecycleScope.launch {
                    val intent = packageManager.getLaunchIntentForPackage("com.sinovatech.unicom.ui")
                    intent?.let {     startActivity(it)  }

                }
            }
            Pair(Icons.Filled.Stop,"????????????")
        } else {
            Pair(Icons.Filled.PlayArrow,"????????????")
        }
        if (isVpnChange){
            menuItems[1] = vpn
        }else {
            menuItems.add(Pair(Icons.Filled.Tune, "??????"))
            menuItems.add(vpn)
            menuItems.add(Pair(Icons.Filled.ExitToApp, "????????????"))
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
}