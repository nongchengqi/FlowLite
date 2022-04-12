package com.evan.flowlite.ui

import android.content.Context
import androidx.compose.foundation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evan.flowlite.ConfigConst
import com.evan.flowlite.FlowBean
import com.evan.flowlite.InfoBean
import com.evan.flowlite.R
import com.evan.flowlite.utils.ParseUtil

class SimpleFlowInfoProvider:PreviewParameterProvider<InfoBean>{
    private val baseInfo = FlowBean(false,"正在等待更新...","0","0","0","0",false)
    override val values: Sequence<InfoBean>
        get() = sequenceOf(InfoBean(
            pkgName = "正在等待更新...",
            normalFlow = emptyList(),
            mlFlow = emptyList(),
            useMlFlow = "0",
            useResFlow = "0",
            useTotalFlow = "0",
            remindFlow = "0"
        ))

}
interface MainEventListener{
    fun onItemClick(flow:FlowBean,menu: String){}
    fun onMenuClick(menu:String){}
    fun onStartClick(){}
}

@Composable
fun MainPreview(@PreviewParameter(SimpleFlowInfoProvider::class) infoBean: InfoBean,listener:MainEventListener = object :MainEventListener{}){
    MainView(infoBean, emptyList(),false, listener)
}

@Composable
fun MainView(infoBean: InfoBean,menu: List<Pair<ImageVector,String>>,isRunning: Boolean = false,listener:MainEventListener) {
    Column(
        Modifier.fillMaxSize().background(color = Color(0xFFF7F7F7))
            .background(
                brush = Brush.verticalGradient(
                    0f to Color(ConfigConst.mainColor).copy(alpha = 0.5f),
                    0.3f to Color.White
                )
            )
    ) {
        Box {
            val expandState = remember {
                mutableStateOf(false)
            }
            DropdownMenuView(expandState, DpOffset(250.dp, 0.dp), menu, listener::onMenuClick)
            MainTopBar(
                title = "FlowLite",
                isRunning = isRunning,
                onMenuClick = { expandState.value = true }) {
                listener.onStartClick()
            }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(12.dp))
            MainHeader(
                pkgName = infoBean.pkgName,
                total = infoBean.useTotalFlow,
                resRemind = infoBean.remindFlow,
                resUsed = infoBean.useResFlow,
                freeUsed = infoBean.useMlFlow
            )
            if (infoBean.normalFlow.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                PackageView("普通流量套餐", infoBean.normalFlow, listener::onItemClick)
            }
            if (infoBean.mlFlow.isNotEmpty()) {
                Spacer(Modifier.height(20.dp))
                PackageView("免费流量套餐", infoBean.mlFlow, listener::onItemClick)
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Preview
@Composable
fun MainHeader(
    pkgName: String = "联通最牛逼的卡",
    total: String = "0",
    resUsed: String = "0",
    resRemind: String = "0",
    freeUsed: String = "0"
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(ConfigConst.mainColor),
        elevation = 4.dp
    ) {
        val title = remember { arrayOf("总用", "通用使用", "通用剩余", "已免") }
        val number = arrayOf(total, resUsed, resRemind, freeUsed)
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = "基本信息",
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp)
            )
            Column(
                Modifier.fillMaxWidth().padding(1.dp)
                    .background(color = Color.White, shape = RoundedCornerShape(10.dp))
            ) {
                Text(
                    text = pkgName,
                    fontSize = 16.sp,
                    color = Color(0xff333333),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                        .padding(vertical = 16.dp)
                )
                Spacer(
                    Modifier.fillMaxWidth().padding(start = 30.dp, end = 30.dp,bottom = 16.dp).height(1.dp)
                        .background(color = Color(0xfffafafa))
                )

                Row(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    for (index in 0 until 4) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = title[index],
                                fontSize = 16.sp,
                                color = Color(0xff333333),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Text(
                                text = ParseUtil.parseFlowUnit(number[index]),
                                fontSize = 14.sp,
                                color = Color(0xff666666),
                                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PackageView(
    title: String = "普通套餐信息",
    list: List<FlowBean>,
    onItemClick:(FlowBean,String)->Unit = {_,_->}
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color(ConfigConst.mainColor),
        elevation = 4.dp
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp)
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(1.dp)
                    .background(color = Color.White, shape = RoundedCornerShape(10.dp))
            ) {
                list.forEach { item ->
                    val menuShow = remember { mutableStateOf(false) }
                    Column(Modifier.fillMaxSize().combinedClickable(onLongClick = {
                        menuShow.value = true
                    }, onClick = {}) .padding(8.dp)) {
                        FlowItemMenuView(menuShow, DpOffset(80.dp,0.dp), isFree = (item.isFree&& !item.isUserMove) ||(!item.isFree && item.isUserMove)){
                            onItemClick.invoke(item,it)
                        }
                        Text(
                            text = item.name,
                            fontSize = 16.sp,
                            color = Color(0xff333333),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        val subTitle = if ((item.remind.toFloatOrNull() ?: 0f) > 0) {
                            stringResource(
                                R.string.pkg_info,
                                ParseUtil.parseFlowUnit(item.remind),
                                ParseUtil.parseFlowUnit(item.used),
                                ParseUtil.parseFlowUnit(item.total)
                            )
                        } else {
                            stringResource(R.string.used_, ParseUtil.parseFlowUnit(item.used))
                        }
                        Text(
                            text = subTitle,
                            fontSize = 14.sp,
                            color = Color(0xff666666),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        val total = item.total.toFloatOrNull()?:0f
                        val present = (item.used.toFloatOrNull()?:0f) / if(total == 0f) 1024*100f else total

                        Canvas(Modifier.fillMaxWidth().padding(top = 8.dp).height(4.dp)) {
                            drawLine(
                                color = Color(0xfff1f1f1),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                cap = StrokeCap.Round,
                                strokeWidth = size.height
                            )
                            drawLine(
                                color = Color(ConfigConst.mainColor),
                                start = Offset(0f, 0f),
                                end = Offset(size.width * present, 0f),
                                cap = StrokeCap.Round,
                                strokeWidth = size.height
                            )
                        }

                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun MainTopBar(
    title: String,
    showStatusBarPadding: Boolean = true,
    isRunning:Boolean = false,
    onMenuClick: () -> Unit = {},
    onRunClick: () -> Unit = {}
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
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF18212C),
            modifier =  Modifier.align(Alignment.CenterVertically)
        )
        Spacer(Modifier.weight(1f))
        Image(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(36.dp)
                .rippleClickable(
                    onClick = onRunClick,
                )
                .padding(8.dp),
            imageVector = if (isRunning) Icons.Filled.Refresh else Icons.Filled.Slideshow,
            contentDescription = null
        )
        Image(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(36.dp)
                .rippleClickable(
                    onClick = onMenuClick,
                )
                .padding(8.dp),
            imageVector = Icons.Filled.MoreVert,
            contentDescription = null
        )
    }
}

fun getStatusBarHeight(context: Context): Int {
    var result = 0
    val resourceId: Int = try {
        context.resources.getIdentifier("status_bar_height", "dimen", "android")
    } catch (e: Throwable) {
        0
    }
    if (resourceId > 0) {
        result = context.resources.getDimensionPixelSize(resourceId)
    }
    return result
}