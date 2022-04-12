package com.evan.flowlite.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evan.flowlite.ConfigConst

interface ConfigEventListener{
    fun onBackClick(){}
    fun onNatChanged(checked:Boolean){}
    fun onBatteryOptChanged(checked:Boolean){}
    fun onLogRecordChanged(checked:Boolean){}
    fun onNormalCookieChanged(value:String){}
    fun onUnCodeCookieChanged(value:String){}
    fun onRefreshTimeChanged(value:String){}
    fun onUnitLimitChanged(value:String){}
    fun onMainClolorChanged(value:String){}
    fun onClearUsdFlow(){}
    fun onViewLogRecord(){}
    fun onLogRecordDialogDismiss(){}
    fun feedback(){}
    fun onClearUsedEveryDayChecked(checked: Boolean){}
    fun onFloatWindowSwitch(checked: Boolean){}
    fun onFloatAndNotifySwitch(checked: Boolean){}
    fun onFloatSizeChanged(value:String){}
    fun onFloatAlphaChanged(value:String){}
    fun onFloatBackgroundChanged(value:String){}
    fun onFloatTextSizeChanged(value:String){}
    fun onFloatContentChanged(value:String){}
    fun onFloatTextColorChanged(value:String){}
}

@Preview
@Composable
fun ConfigPreview(listener:ConfigEventListener = object :ConfigEventListener{}){
    ConfigView(normalCookie = "123456789", listener = listener)
}

@Composable
fun ConfigView(
    showLogRecord: Boolean = false,
    clearUsedEveryDay: Boolean = false,
    mainColor: String = "",
    logContent: String = "",
    unitLimit: String = "1024",
    refreshTime: String = "5",
    normalCookie: String = "",
    unCodeCookie: String = "",
    logRecord: Boolean = false,
    natSwitch: Boolean = false,
    batteryOtp: Boolean = false,
    //float
    floatWindowSwitch:Boolean = false,
    floatAndNotifySwitch:Boolean = false,
    floatSize:String = "",
    floatTextSize:String = "",
    floatContent:String = "",
    floatTextColor:String = "",
    floatAlpha:String = "255",
    listener: ConfigEventListener
) {
    Column(
        Modifier.fillMaxSize().background(color = Color.White)
            .background(
                brush = Brush.verticalGradient(
                    0f to Color(ConfigConst.mainColor).copy(alpha = 0.5f),
                    0.3f to Color.White
                )
            )
    ) {
        ConfigTopBar(title = "配置", onMenuClick = listener::onBackClick)
        ScrollContent(
            clearUsedEveryDay,
            mainColor,
            unitLimit,
            refreshTime,
            normalCookie,
            unCodeCookie,
            logRecord,
            natSwitch,
            batteryOtp,
            floatWindowSwitch,
            floatAndNotifySwitch,
            floatSize,
            floatTextSize,
            floatContent,
            floatTextColor,
            floatAlpha,
            listener
        )
    }
    if (showLogRecord){
        LogRecordDialog(log = logContent, onDismiss = listener::onLogRecordDialogDismiss)
    }
}

@Composable
fun ScrollContent(
    clearUsedEveryDay:Boolean,
    mainColor: String,
    unitLimit: String,
    refreshTime: String,
    normalCookie: String,
    unCodeCookie: String,
    logRecord: Boolean,
    natSwitch: Boolean,
    batteryOtp: Boolean,
    floatWindowSwitch:Boolean = false,
    floatAndNotifySwitch:Boolean = false,
    floatSize:String = "",
    floatTextSize:String = "",
    floatContent:String = "",
    floatTextColor:String = "",
    floatAlpha:String,
    listener: ConfigEventListener
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            SwitchColumn(
                Modifier.height(100.dp).weight(1f).padding(start = 12.dp, end = 6.dp),
                title = "网络切换自动刷新",
                isCheck = natSwitch,
                onCheckedChange = listener::onNatChanged
            )
            SwitchColumn(
                Modifier.height(100.dp).weight(1f).padding(start = 6.dp, end = 12.dp),
                title = "忽略省电优化",
                desc = "不管，除非后台无法刷新",
                isCheck = batteryOtp,
                onCheckedChange = listener::onBatteryOptChanged
            )
        }
        Row(Modifier.padding(top = 20.dp).fillMaxWidth()) {
            BottonColumn(
                Modifier.height(100.dp).weight(1f).padding(start = 12.dp, end = 6.dp),
                clearUsedEveryDay = clearUsedEveryDay,
                title = "清除跳点记录",
                desc = "下次刷新时，重置跳点记录",
                onClick = listener::onClearUsdFlow,
                onCheckedChange = listener::onClearUsedEveryDayChecked
            )
            SwitchColumn(
                Modifier.height(100.dp).weight(1f).padding(start = 6.dp, end = 12.dp)
                    .rippleClickable(onClick = listener::onViewLogRecord),
                title = "记录本次操作日志",
                desc = "点击非按钮区域查看日志",
                isCheck = logRecord,
                onCheckedChange = listener::onLogRecordChanged
            )
        }
        OutlinedTextField(
            value = unitLimit,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = listener::onUnitLimitChanged,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 8.dp)
                .fillMaxWidth(),
            label = {
                Text(text = "流量大于多少M时以G为单位")
            }
        )
        OutlinedTextField(
            value = refreshTime,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = listener::onRefreshTimeChanged,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            label = {
                Text(text = "刷新时间间隔(分钟)")
            }
        )

        OutlinedTextField(
            value = normalCookie,
            onValueChange = listener::onNormalCookieChanged,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            label = {
                Text(text = "标准Cookie")
            }
        )

        OutlinedTextField(
            value = unCodeCookie,
            onValueChange = listener::onUnCodeCookieChanged,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            label = {
                Text(text = "未格式化Cookie")
            }
        )

        Text(
            text = "有问题忍不了，论坛反馈太慢了，点击这里直接开喷",
            textDecoration = TextDecoration.Underline,
            fontSize = 14.sp,
            color = Color(0xff333333),
            modifier = Modifier.padding(vertical = 40.dp).align(Alignment.CenterHorizontally).clickable(onClick = listener::feedback)
        )

        Spacer(Modifier.padding(horizontal = 12.dp).height(1.dp).fillMaxWidth().background(color = Color(0xfffafafa)))
        Text(
            text = "个性化配置",
            fontSize = 14.sp,
            color = Color(0xff999999),
            modifier = Modifier.padding(vertical = 20.dp).align(Alignment.CenterHorizontally).clickable(onClick = listener::feedback)
        )

        OutlinedTextField(
            value = mainColor,
            onValueChange = listener::onMainClolorChanged,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            label = {
                Text(text = "主题色-格式:#AAAAAA")
            }
        )
        Spacer(Modifier.height(20.dp))
        Spacer(Modifier.padding(horizontal = 12.dp).height(1.dp).fillMaxWidth().background(color = Color(0xfffafafa)))
        Text(
            text = "小组件配置",
            fontSize = 14.sp,
            color = Color(0xff999999),
            modifier = Modifier.padding(vertical = 20.dp).align(Alignment.CenterHorizontally).clickable(onClick = listener::feedback)
        )
        Spacer(Modifier.padding(horizontal = 12.dp).height(1.dp).fillMaxWidth().background(color = Color(0xfffafafa)))
        Text(
            text = "悬浮窗配置",
            fontSize = 14.sp,
            color = Color(0xff999999),
            modifier = Modifier.padding(vertical = 20.dp).align(Alignment.CenterHorizontally).clickable(onClick = listener::feedback)
        )
        Row(Modifier.fillMaxWidth()) {
            SwitchColumn(
                Modifier.height(100.dp).weight(1f).padding(start = 12.dp, end = 6.dp),
                title = "启用悬浮窗",
                isCheck = floatWindowSwitch,
                onCheckedChange = listener::onFloatWindowSwitch
            )
            SwitchColumn(
                Modifier.height(100.dp).weight(1f).padding(start = 6.dp, end = 12.dp),
                title = "悬浮+通知",
                desc = "同时显示常驻通知，可以提高保活不被系统杀",
                isCheck = floatAndNotifySwitch,
                onCheckedChange = listener::onFloatAndNotifySwitch
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(top = 12.dp)) {
            OutlinedTextField(
                value = floatSize,
                onValueChange = listener::onFloatSizeChanged,
                modifier = Modifier.weight(1f).padding(end = 6.dp),
                label = {
                    Text(text = "悬浮窗尺寸")
                }
            )
            OutlinedTextField(
                value = floatTextSize,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = listener::onFloatTextSizeChanged,
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                label = {
                    Text(text = "悬浮窗字体大小")
                }
            )
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            OutlinedTextField(
                value = floatTextColor,
                onValueChange = listener::onFloatTextColorChanged,
                modifier = Modifier.weight(1f).padding(end = 6.dp),
                label = {
                    Text(text = "悬浮窗字体颜色")
                }
            )
            OutlinedTextField(
                value =floatAlpha,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = listener::onFloatAlphaChanged,
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                label = {
                    Text(text = "悬浮窗透明度")
                }
            )
        }
        OutlinedTextField(
            value = floatContent,
            onValueChange = listener::onFloatContentChanged,
            modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 100.dp).fillMaxWidth(),
            label = {
                Text(text = "悬浮内容：[跳]-跳点，[总]-总用，[通]-通用使用，[剩]-通用剩余，[行]-换行", fontSize = 10.sp)
            }
        )
    }
}


@Composable
fun SwitchColumn(modifier: Modifier = Modifier,title: String,desc:String = "",isCheck:Boolean = false,onCheckedChange:(Boolean)->Unit){
    Surface( modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        elevation = 4.dp) {
        Column(Modifier.fillMaxSize().padding(vertical = 8.dp, horizontal = 12.dp)) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color(0xff333333),
                modifier = Modifier.padding(bottom = 2.dp).align(Alignment.CenterHorizontally)
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color(0xff999999),
                modifier = Modifier.padding(bottom = 2.dp).align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(checked = isCheck,modifier = Modifier.align(Alignment.CenterHorizontally), onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun BottonColumn(modifier: Modifier = Modifier,title: String,desc:String = "",clearUsedEveryDay:Boolean,onClick:()->Unit,onCheckedChange: (Boolean) -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        elevation = 4.dp
    ) {
        Box(Modifier.fillMaxSize().clickable(onClick = onClick).padding(vertical = 8.dp, horizontal = 12.dp)) {
            Column(Modifier.align(Alignment.Center)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color(0xff333333),
                    modifier = Modifier.padding(bottom = 2.dp).align(Alignment.CenterHorizontally)
                )
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = Color(0xff999999),
                    modifier = Modifier.padding(bottom = 2.dp).align(Alignment.CenterHorizontally)
                )

                Row (Modifier.align(Alignment.CenterHorizontally)){
                    Text(
                        text = "每日自动清零",
                        fontSize = 12.sp,
                        color = Color(0xff333333),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Switch(checked = clearUsedEveryDay,onCheckedChange = onCheckedChange,modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        }
    }
}

@Composable
fun ConfigTopBar(
    title: String,
    showStatusBarPadding: Boolean = true,
    onMenuClick: () -> Unit = {}
) {
    val height = if (showStatusBarPadding) {
        val context = LocalContext.current
        val density = LocalDensity.current
        remember {
            val value = getStatusBarHeight(context = context)
            if (value <= 0) {
                25.dp
            } else {
                Dp(value / density.density)
            }
        }
    } else {
        0.dp
    }
    Row(
        Modifier
            .padding(top = height)
            .height(56.dp)
            .fillMaxWidth().padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Image(
            modifier = Modifier
                .align(Alignment.CenterVertically).padding(end = 12.dp)
                .size(42.dp)
                .rippleClickable(
                    onClick = onMenuClick,
                )
                .padding(8.dp),
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = null
        )
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF18212C),
            modifier =  Modifier.align(Alignment.CenterVertically)
        )
        Spacer(Modifier.weight(1f))
    }
}