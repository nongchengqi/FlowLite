package com.evan.flowlite.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.format.DateUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.evan.flowlite.ConfigConst
import com.evan.flowlite.R
import com.evan.flowlite.float.FloatWindowManager
import com.evan.flowlite.utils.AppPref
import com.evan.flowlite.utils.LogUtil
import com.evan.flowlite.utils.RequestTimer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.Exception

class ConfigActivity : AppCompatActivity() {
    private val powerMgr by lazy { getSystemService(POWER_SERVICE) as PowerManager }
    private var normalCookie by mutableStateOf(AppPref.cookie)
    private var unCodeCookie by mutableStateOf("")
    private var mainColor by mutableStateOf(AppPref.mainColor.toString())
    private var refreshTime by mutableStateOf(AppPref.updateTime.toString())
    private var unitLimit by mutableStateOf(AppPref.unitChangeValue.toString())
    private var batteryOtp by mutableStateOf(false)
    private var autoClearUsed by mutableStateOf(AppPref.autoClearUsed)
    private var showLogRecordDialog by mutableStateOf(false)
    private var logRecord by mutableStateOf(LogUtil.isRecord())
    private var natAuto by mutableStateOf(AppPref.autoUpdate)
    //float
    private var floatSize by mutableStateOf(AppPref.floatSize)
    private var floatTextColor by mutableStateOf(AppPref.floatTextColor)
    private var floatTextSize by mutableStateOf(AppPref.floatTextSize.toString())
    private var floatContent by mutableStateOf(AppPref.floatContent)
    private var floatAlpha by mutableStateOf(AppPref.floatAlpha.toString())
    private var floatSwitch by mutableStateOf(AppPref.floatSwitch)
    private var floatAndNotifySwitch by mutableStateOf(AppPref.floatAndNotifySwitch)
    private val listener = object : ConfigEventListener{
        override fun onBackClick() {
            onBackPressed()
        }

        override fun onNatChanged(checked: Boolean) {
            natAuto = checked
            AppPref.autoUpdate = checked
            RequestTimer.switchListenerNetworkChange(checked)
        }


        override fun onLogRecordChanged(checked: Boolean) {
            logRecord = checked
            if (checked) {
                LogUtil.openLogRecord()
            } else {
                LogUtil.closeLogRecord()
            }
        }

        override fun onClearUsdFlow() {
            clearUseFlow()
        }

        override fun onBatteryOptChanged(checked: Boolean) {
            checkPower()
        }

        override fun onNormalCookieChanged(value: String) {
            normalCookie = value
        }

        override fun onUnCodeCookieChanged(value: String) {
            unCodeCookie = value
        }

        override fun onRefreshTimeChanged(value: String) {
            refreshTime = value
        }

        override fun onUnitLimitChanged(value: String) {
            unitLimit = value
            AppPref.unitChangeValue = unitLimit.toIntOrNull()?:1024
        }

        override fun onViewLogRecord() {
            showLogRecordDialog = true
        }

        override fun onLogRecordDialogDismiss() {
            showLogRecordDialog = false
        }

        override fun onClearUsedEveryDayChecked(checked: Boolean) {
            autoClearUsed = checked
            AppPref.autoClearUsed = checked
        }

        override fun feedback() {
            kotlin.runCatching {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://evannong.gitee.io/file/liteflowqr.png")
                startActivity(intent)
            }
        }

        override fun onMainClolorChanged(value: String) {
            mainColor = value
            if (value.startsWith("#") && value.length == 7){
                try {
                    val color = android.graphics.Color.parseColor(value)
                    AppPref.mainColor = color.toLong()
                    LogUtil.toast("主题色更改成功，重启生效")
                }catch (e:Exception){
                    LogUtil.toast("主题色输入有误")
                }

            }
        }

        override fun onFloatWindowSwitch(checked: Boolean) {
            if (checked) {
                lifecycleScope.launch {
                    if (FloatWindowManager.showView(this@ConfigActivity).isSuccess){
                        floatSwitch = checked
                        AppPref.floatSwitch = checked
                    }
                }
            } else {
                FloatWindowManager.removeAllView()
                floatSwitch = checked
                AppPref.floatSwitch = checked
            }
        }

        override fun onFloatAndNotifySwitch(checked: Boolean) {
            floatAndNotifySwitch = checked
            AppPref.floatAndNotifySwitch = checked
            LogUtil.toast("开启成功重启app生效")

        }

        override fun onFloatSizeChanged(value: String) {
            floatSize = value
        }

        override fun onFloatAlphaChanged(value: String) {
            floatAlpha = value
        }

        override fun onFloatTextSizeChanged(value: String) {
           floatTextSize = value
        }

        override fun onFloatContentChanged(value: String) {
            floatContent = value
        }

        override fun onFloatTextColorChanged(value: String) {
            floatTextColor = value
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colors =  MaterialTheme.colors.copy(primary = Color( ConfigConst.mainColor), secondaryVariant = Color( ConfigConst.mainColor))) {
                ConfigView(
                    clearUsedEveryDay = autoClearUsed,
                    unitLimit = unitLimit,
                    refreshTime = refreshTime,
                    normalCookie = normalCookie,
                    unCodeCookie = unCodeCookie,
                    mainColor = mainColor,
                    logContent = LogUtil.getLog(),
                    showLogRecord = showLogRecordDialog,
                    logRecord = logRecord,
                    natSwitch = natAuto,
                    batteryOtp = batteryOtp,
                    floatWindowSwitch = floatSwitch,
                    floatAndNotifySwitch = floatAndNotifySwitch,
                    floatSize = floatSize,
                    floatTextSize = floatTextSize,
                    floatContent = floatContent,
                    floatTextColor = floatTextColor,
                    floatAlpha = floatAlpha,
                    listener = listener
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkPower(true)
    }

    private fun clearUseFlow() {
        AppPref.usedFlow = "0"
        AppPref.usedFlowClearTime = System.currentTimeMillis()
        LogUtil.toast("已清除，下次刷新生效")
    }

    @SuppressLint("BatteryLife")
    private fun checkPower(checkMode:Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            batteryOtp = powerMgr.isIgnoringBatteryOptimizations(packageName)
            if (!checkMode) {
                kotlin.runCatching {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
        } else {
            LogUtil.toast("此安卓版本无需开启")
        }
    }

    override fun onBackPressed() {
        when {
            normalCookie.isNotEmpty() -> {
                AppPref.cookie = normalCookie
                ConfigConst.cookie = AppPref.cookie
            }
            unCodeCookie.isNotEmpty() -> {
                AppPref.cookie = unCodeCookie.replace("\ncookie: ", "; ").replace("cookie: ","")
                ConfigConst.cookie = AppPref.cookie
            }
            else -> {
                LogUtil.toast("cookie 为空")
            }
        }
        AppPref.updateTime = refreshTime.toIntOrNull()?:5
        ConfigConst.queryTime = AppPref.updateTime * DateUtils.MINUTE_IN_MILLIS
        if (AppPref.floatSwitch){
            MainScope().launch {
                updateFloat(floatSize,floatTextSize,floatContent,floatTextColor,floatAlpha)
            }
        }
        LogUtil.toast("所有更改已保存,部分配置需要重启生效")
        super.onBackPressed()
    }

    private fun updateFloat(
        floatSize: String = "",
        floatTextSize: String = "",
        floatContent: String = "",
        floatTextColor: String,
        floatAlpha: String = "255",
    ) {
        val size = floatSize.split(",")
        if (size.size == 2 && size[0].toIntOrNull() != null && size[1].toIntOrNull() != null) {
            AppPref.floatSize = floatSize
        }
        if (floatTextSize.toIntOrNull() != null) {
            AppPref.floatTextSize = floatTextSize.toInt()
        }
        AppPref.floatContent = floatContent
        if (kotlin.runCatching { android.graphics.Color.parseColor(floatTextColor) }.isSuccess) {
            AppPref.floatTextColor = floatTextColor
        }
        if (floatAlpha.toIntOrNull() != null) {
            AppPref.floatAlpha = floatAlpha.toInt()
        }

    }
}