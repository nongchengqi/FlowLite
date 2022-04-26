package com.evan.flowlite.utils

import android.content.Intent
import android.text.format.DateUtils
import android.util.Log
import com.evan.flowlite.*
import com.evan.flowlite.float.FloatView
import com.evan.flowlite.float.FloatWindowManager
import com.evan.flowlite.ui.MainActivity
import com.evan.flowlite.utils.ParseUtil.getCurrentTime
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object RequestTimer {

    private var isInNetwork = false
    private var isQueryRunning = false
    private var timerJob:Job? = null
    private var mUpdateListener: ((InfoBean) -> Unit)? = null
    private var mNotifyListener: ((String,String) -> Unit)? = null
    private var mFlowInfoBean: InfoBean? = null
    private var mIpInfo:Pair<String,String>? = null
    val timerFlow  =  MutableStateFlow(isQueryRunning)
    val networkFlow  =  MutableStateFlow(isInNetwork)
    fun initCookie(){
        if (ConfigConst.cookie.isEmpty()){
            ConfigConst.cookie = AppPref.cookie
            ConfigConst.queryTime = AppPref.updateTime * DateUtils.MINUTE_IN_MILLIS
        }
    }
    suspend fun refreshFlow(finish:()->Unit = {}){
        initCookie()
        MainScope().launch {
            val ipInfo = getIpInfo()
            if (ipInfo.first.isNotEmpty()){
                mIpInfo = ipInfo
            }
        }
        doNetwork {
            MainScope().launch(Dispatchers.IO) {
                decodeFlowInfo(it)
                finish.invoke()
            }
        }
    }

    fun setOnUpdateListener(cb: (InfoBean) -> Unit) {
        mUpdateListener = cb
    }
    fun setOnNotifyUpdateListener(cb: (String,String) -> Unit) {
        mNotifyListener = cb
    }

    fun switchListenerNetworkChange(isOpen:Boolean){
        if (isOpen){
            NetworkListener.register{
                MainScope().launch(Dispatchers.IO) {
                    refreshFlow()
                }
            }
        }else{
            NetworkListener.unregister()
        }
    }

    suspend fun parseFromCache() {
        val json = AppPref.json
        if (json != "{}") {
            decodeFlowInfo(Pair(true, json))
        }
    }

    fun launchTimer():Boolean{
        if (isQueryRunning) return false
        if (ConfigConst.cookie.isEmpty()){
            LogUtil.log(msg = "Cookie为空，启动失败")
            LogUtil.toast("Cookie为空，启动失败")
            return false
        }
        LogUtil.log(msg = "启动成功")
        isQueryRunning = true
        timerFlow.tryEmit(isQueryRunning)
        timerJob = MainScope().launch(Dispatchers.IO){
            while (isActive){
                refreshFlow()
                delay(ConfigConst.queryTime)
                LogUtil.log(msg = "触发自动刷新")
            }
        }
        return true
    }

    fun stopTimer(){
        LogUtil.log(msg = "停止监控")
        isQueryRunning = false
        timerFlow.tryEmit(isQueryRunning)
        timerJob?.cancel()
    }

    private suspend fun doNetwork(requestResult:(Pair<Boolean,String>)->Unit) = withContext(Dispatchers.IO) {
        if (isInNetwork || ConfigConst.cookie.isEmpty()) return@withContext
        LogUtil.log(msg = "进行网络请求")
        isInNetwork = true
        networkFlow.emit(isInNetwork)
        try {
            val url =
                URL("https://m.client.10010.com/servicequerybusiness/operationservice/queryOcsPackageFlowLeftContentRevisedInJune")
            val httpsURLConnection = url.openConnection() as HttpsURLConnection
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.addRequestProperty(
                "content-type",
                "application/x-www-form-urlencoded"
            )
            httpsURLConnection.addRequestProperty("origin", "https://img.client.10010.com")
            httpsURLConnection.addRequestProperty(
                "referer",
                "https://img.client.10010.com/yuliangchaxunsf/index.html"
            )
            httpsURLConnection.addRequestProperty("cookie", ConfigConst.cookie)

            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true
            httpsURLConnection.useCaches = false
            val responseCode = httpsURLConnection.responseCode
            val flowStr = if (responseCode == 200) {
                val reader = InputStreamReader(httpsURLConnection.inputStream)
                val result = reader.readText()
                Log.e("TAG", "doNet: $result")
                Pair(true, result)
            } else {
                Pair(false, "responseCode:$responseCode")
            }
            LogUtil.log(msg = "请求结果:${flowStr.second}")
            requestResult.invoke(flowStr)
        }catch (e:Throwable){
            LogUtil.logAndToast("刷新异常：${e.message}")
        }finally {
            isInNetwork = false
            networkFlow.emit(isInNetwork)
        }

    }

    private suspend fun getIpInfo() = withContext(Dispatchers.IO) {
        LogUtil.log(msg = "尝试获取ip")
        try {
            val url = URL("http://ip-api.com/json/?fields=61439")
            val httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.requestMethod = "GET"
            httpURLConnection.doInput = true
            httpURLConnection.useCaches = false
            val responseCode = httpURLConnection.responseCode
            val ipStr = if (responseCode == 200) {
                val reader = InputStreamReader(httpURLConnection.inputStream)
                val result = reader.readText()
                LogUtil.log(msg = "获取ip成功")
                result
            } else {
                LogUtil.log(msg = "获取ip失败:$responseCode")
                "{}"
            }

            val ipJson = JSONObject(ipStr)
            Pair(ipJson.optString("query", ""), ipJson.optString("countryCode", ""))
        } catch (e: Exception) {
            LogUtil.log(msg = "获取ip失败:${e.message}")
            Pair("", "")
        }
    }

    private suspend fun decodeFlowInfo(pair: Pair<Boolean, String>?) {
        if (pair == null) {
            return
        }
        LogUtil.log(msg = "json解析")
        if (pair.first) {
            AppPref.json = pair.second
            try {
                val rootObj = JSONObject(pair.second)
                val pkgName = rootObj.optString("packageName", "未知")
                val usedTotal = rootObj.optString("sum", "0") //总使用
                val usedTotalRes = rootObj.optString("sumresource", "0") //套餐内使用

                var useMlFlow = 0f
                var usedFlowRes = 0f
                var remindFlowRes = 0f
                val freeList = mutableListOf<FlowBean>()
                val resList = mutableListOf<FlowBean>()

                //自行计算总用，用于核对
                var calTotalUse = 0f

                //免流流量解析
                val mlArr = rootObj.optJSONArray("MlResources")
                mlArr?.let {
                    for (first in 0 until it.length()) {
                        val subMlObj = it.optJSONObject(first)
                        if (subMlObj.optString("type", "") != "MlFlowdetailsList") {
                            continue
                        }
                        val userResource = subMlObj.optString("userResource", "0").toFloatOrNull() ?: 0f
                        calTotalUse += userResource
                        useMlFlow += userResource
                        val subMlArr = subMlObj.optJSONArray("details")
                        subMlArr?.let { subArr ->
                            for (second in 0 until subArr.length()) {
                                val subObj = subArr.optJSONObject(second)
                                val flowBean = FlowBean(
                                    isFree = true,
                                    name = subObj.optString("feePolicyName", "未知"),
                                    total = "0",
                                    used = subObj.optString("use", "0"),
                                    remind = "0",
                                    id = subObj.optString("feePolicyId", "")
                                            + subObj.optString("addupItemCode", "")
                                            + subObj.optString("feePolicyName", "未知")
                                )

                                if (AppPref.userNormalMoveIds.contains("#${flowBean.id}#")){
                                    resList.add(flowBean.copy(isUserMove = true))
                                    val fUsed = flowBean.used.toFloat()
                                    val fRemind = flowBean.remind.toFloat()
                                    useMlFlow  -= fUsed
                                    remindFlowRes += fRemind
                                    usedFlowRes += fUsed
                                } else {
                                    freeList.add(flowBean)
                                }
                            }
                        }
                    }
                }

                //流量包解析
                val flowArr = rootObj.optJSONArray("resources")
                flowArr?.let {
                    for (first in 0 until it.length()) {
                        val subFlowObj = it.optJSONObject(first)
                        if (subFlowObj.optString("type", "") != "flow") {
                            continue
                        }
                        val userResource = subFlowObj.optString("userResource", "0").toFloatOrNull() ?: 0f
                        calTotalUse += userResource
                        usedFlowRes += userResource
                        remindFlowRes += subFlowObj.optString("remainResource", "0").toFloatOrNull()
                            ?: 0f
                        val subFlowArr = subFlowObj.optJSONArray("details")
                        subFlowArr?.let { subArr ->
                            for (second in 0 until subArr.length()) {
                                val subObj = subArr.optJSONObject(second)
                                val flowBean = FlowBean(
                                    isFree = false,
                                    name = subObj.optString(
                                        "feePolicyName",
                                        "未知"
                                    ) + " - " + subObj.optString("addUpItemName", ""),
                                    total = subObj.optString("total", "0"),
                                    used = subObj.optString("use", "0"),
                                    remind = subObj.optString("remain", "0"),
                                    id = subObj.optString("feePolicyId", "")
                                            + subObj.optString("addupItemCode", "")
                                            + subObj.optString("feePolicyName", "未知")
                                )
                                if (AppPref.userFreeMoveIds.contains("#${flowBean.id}#")){
                                    freeList.add(flowBean.copy(isUserMove = true))
                                    val fUsed = flowBean.used.toFloat()
                                    val fRemind = flowBean.remind.toFloat()
                                    usedFlowRes  -= fUsed
                                    remindFlowRes -= fRemind
                                    useMlFlow += fUsed
                                } else {
                                    resList.add(flowBean)
                                }

                            }
                        }
                    }
                }

                //套餐外流量包解析--默认总值1G
                val twFlowArr = rootObj.optJSONArray("TwResources")
                twFlowArr?.let {
                    for (first in 0 until it.length()) {
                        val twFlowObj = it.optJSONObject(first)
                        if (twFlowObj.optString("type", "") != "flow") {
                            continue
                        }
                        val userResource = twFlowObj.optString("userResource", "0").toFloatOrNull()
                            ?: 0f
                        calTotalUse += userResource
                        usedFlowRes += userResource
                        remindFlowRes -= usedFlowRes
                        val subFlowArr = twFlowObj.optJSONArray("details")
                        subFlowArr?.let { subArr ->
                            for (second in 0 until subArr.length()) {
                                val subObj = subArr.optJSONObject(second)
                                val flowBean = FlowBean(
                                    isFree = false,
                                    name = subObj.optString(
                                        "feePolicyName",
                                        "未知"
                                    ) + " - " + subObj.optString("addUpItemName", ""),
                                    total = subObj.optString("total", "0"),
                                    used = subObj.optString("use", "0"),
                                    remind = subObj.optString("remain", "0"),
                                    id = subObj.optString("feePolicyId", "")
                                            + subObj.optString("addupItemCode", "")
                                            + subObj.optString("feePolicyName", "未知")
                                )
                                if (AppPref.userFreeMoveIds.contains("#${flowBean.id}#")){
                                    freeList.add(flowBean.copy(isUserMove = true))
                                    val fUsed = flowBean.used.toFloat()
                                    val fRemind = flowBean.remind.toFloat()
                                    usedFlowRes  -= fUsed
                                    remindFlowRes -= fRemind
                                    useMlFlow += fUsed
                                } else {
                                    resList.add(flowBean)
                                }

                            }
                        }
                    }
                }

                //unshare解析 - 智能识别流量属性
                val unShareArr = rootObj.optJSONArray("unshared")
                unShareArr?.let {
                    for (first in 0 until it.length()) {
                        val subUnShareObj = it.optJSONObject(first)
                        if (subUnShareObj.optString("type", "") != "unsharedFlowList") {
                            continue
                        }
                        val userResource = subUnShareObj.optString("userResource", "0").toFloatOrNull()
                            ?: 0f
                        calTotalUse += userResource
                        val subUnShareArr = subUnShareObj.optJSONArray("details")
                        subUnShareArr?.let { subArr ->
                            for (second in 0 until subArr.length()) {
                                val subObj = subArr.optJSONObject(second)
                                val use =  subObj.optString("use", "0").toFloatOrNull()?:0f
                                val remian = subObj.optString("remain", "0").toFloatOrNull()?:0f
                                val total = subObj.optString("total", "0").toFloatOrNull()?:0f
                                val isFree = total == 0f
                                if (isFree){
                                    useMlFlow += use//
                                    usedFlowRes -= use
                                }else {
                                    remindFlowRes += remian
                                }
                                val flowBean = FlowBean(
                                    isFree = isFree,
                                    name = subObj.optString(
                                        "feePolicyName",
                                        "未知"
                                    ) + " - " + subObj.optString("addUpItemName", ""),
                                    total = total.toString(),
                                    used = use.toString(),
                                    remind = if (remian > 0f) remian.toString() else "0",
                                    id = subObj.optString("feePolicyId", "")
                                            + subObj.optString("addupItemCode", "")
                                            + subObj.optString("feePolicyName", "未知")
                                )
                                if (isFree) {
                                    if (AppPref.userNormalMoveIds.contains("#${flowBean.id}#")) {
                                        resList.add(flowBean.copy(isUserMove = true))
                                        val fUsed = flowBean.used.toFloat()
                                        val fRemind = flowBean.remind.toFloat()
                                        useMlFlow -= fUsed
                                        remindFlowRes += fRemind
                                        usedFlowRes += fUsed
                                    } else {
                                        freeList.add(flowBean)
                                    }
                                } else {
                                    if (AppPref.userFreeMoveIds.contains("#${flowBean.id}#")){
                                        freeList.add(flowBean.copy(isUserMove = true))
                                        val fUsed = flowBean.used.toFloat()
                                        val fRemind = flowBean.remind.toFloat()
                                        usedFlowRes  -= fUsed
                                        remindFlowRes -= fRemind
                                        useMlFlow += fUsed
                                    } else {
                                        resList.add(flowBean)
                                    }
                                }
                            }
                        }
                    }
                }

                //计算非常规流量
                val unknownFlow = (usedTotal.toFloatOrNull()?:0f) - calTotalUse
                if (unknownFlow > 0.1f){
                    useMlFlow += unknownFlow
                    val unknownBean = FlowBean(
                        isFree = true,
                        name = "特殊流量-套餐特有",
                        total = "0",
                        used = unknownFlow.toString(),
                        remind = "0",
                        id = "特殊流量-套餐特有"
                    )
                    freeList.add(unknownBean)
                }

                val bean = InfoBean(
                    pkgName = pkgName,
                    mlFlow = freeList,
                    normalFlow = resList,
                    remindFlow = remindFlowRes.toString(),
                    useMlFlow = useMlFlow.toString(),
                    useResFlow = usedFlowRes.toString(),
                    useTotalFlow = usedTotal,
                )

                if (AppPref.autoClearUsed){
                    if (!ParseUtil.isSameDay(AppPref.usedFlowClearTime)){
                        AppPref.usedFlow = "0"
                        AppPref.usedFlowClearTime = System.currentTimeMillis()
                    }
                }
                var useData = 0f
                if (mFlowInfoBean != null && (bean.useResFlow.toFloatOrNull()
                        ?: 0f) > (mFlowInfoBean?.useResFlow?.toFloatOrNull() ?: 0f)
                ) {
                    useData = (bean.useResFlow.toFloatOrNull() ?: 0f) - (mFlowInfoBean?.useResFlow?.toFloatOrNull() ?: 0f)
                    NotificationHelper.showNormalNotification(
                        title = "通用流量发生消耗",
                        desc = "本次刷新期间，您的通用流量消耗了$useData Mb",
                        smallIcon = R.mipmap.ic_launcher_round,
                        clickIntent = Intent(App.getApp(), MainActivity::class.java),
                        bigIcon = null
                    )
                }
                mFlowInfoBean = bean
                withContext(Dispatchers.Main){
                    mUpdateListener?.invoke(bean)
                }
                val used = AppPref.usedFlow
                val jump = if (used == "0") {
                    AppPref.usedFlow = bean.useResFlow
                    "0"
                } else {
                    ((bean.useResFlow.toFloatOrNull() ?: 0f) - used.toFloat()).toString()
                }


                val ipInfoStr = if (mIpInfo== null || mIpInfo?.first == null){
                    ""
                } else{
                    " - ${mIpInfo?.first} (${mIpInfo?.second})"
                }
                mNotifyListener?.invoke(
                    "剩：${ParseUtil.parseFlowUnit(bean.remindFlow)} | 免：${
                        ParseUtil.parseFlowUnit(
                            bean.useMlFlow
                        )
                    } | 跳：${ParseUtil.parseFlowUnit(jump)}", "更新：${getCurrentTime()}$ipInfoStr"
                )

                //判断刷新时间内使用的通用流量是否超过10mb，超过则关闭数据
                if (useData >= 5f) {
                    LogUtil.log(msg = "流量消耗超过使用阈值，关闭数据")
                    SZKUtil.runCmd("svc data disable").let {
                        LogUtil.log(msg = "关闭情况$it")
                    }
                } else {
                    LogUtil.log(msg = "流量消耗未超过使用阈值")
                }
                LogUtil.log(msg = "消耗流量$useData")

                (FloatWindowManager.getFirstView() as? FloatView)?.updateView(bean.useTotalFlow,bean.useResFlow,bean.useMlFlow,bean.remindFlow,jump)
            } catch (e: Exception) {
                LogUtil.toast("获取失败，请更新cookie(${e.message})")
                e.printStackTrace()
                LogUtil.log(msg = "json解析异常:${e.message}")
                return
            }
        } else {
            LogUtil.toast("获取流量信息失败:${pair.second}")
        }
    }


    fun getFlowBean():InfoBean?{
        return mFlowInfoBean
    }
}