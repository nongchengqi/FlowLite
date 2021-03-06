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
            LogUtil.log(msg = "Cookie?????????????????????")
            LogUtil.toast("Cookie?????????????????????")
            return false
        }
        LogUtil.log(msg = "????????????")
        isQueryRunning = true
        timerFlow.tryEmit(isQueryRunning)
        timerJob = MainScope().launch(Dispatchers.IO){
            while (isActive){
                refreshFlow()
                delay(ConfigConst.queryTime)
                LogUtil.log(msg = "??????????????????")
            }
        }
        return true
    }

    fun stopTimer(){
        LogUtil.log(msg = "????????????")
        isQueryRunning = false
        timerFlow.tryEmit(isQueryRunning)
        timerJob?.cancel()
    }

    private suspend fun doNetwork(requestResult:(Pair<Boolean,String>)->Unit) = withContext(Dispatchers.IO) {
        if (isInNetwork || ConfigConst.cookie.isEmpty()) return@withContext
        LogUtil.log(msg = "??????????????????")
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
            LogUtil.log(msg = "????????????:${flowStr.second}")
            requestResult.invoke(flowStr)
        }catch (e:Throwable){
            LogUtil.logAndToast("???????????????${e.message}")
        }finally {
            isInNetwork = false
            networkFlow.emit(isInNetwork)
        }

    }

    private suspend fun getIpInfo() = withContext(Dispatchers.IO) {
        LogUtil.log(msg = "????????????ip")
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
                LogUtil.log(msg = "??????ip??????")
                result
            } else {
                LogUtil.log(msg = "??????ip??????:$responseCode")
                "{}"
            }

            val ipJson = JSONObject(ipStr)
            Pair(ipJson.optString("query", ""), ipJson.optString("countryCode", ""))
        } catch (e: Exception) {
            LogUtil.log(msg = "??????ip??????:${e.message}")
            Pair("", "")
        }
    }

    private suspend fun decodeFlowInfo(pair: Pair<Boolean, String>?) {
        if (pair == null) {
            return
        }
        LogUtil.log(msg = "json??????")
        if (pair.first) {
            AppPref.json = pair.second
            try {
                val rootObj = JSONObject(pair.second)
                val pkgName = rootObj.optString("packageName", "??????")
                val usedTotal = rootObj.optString("sum", "0") //?????????
                val usedTotalRes = rootObj.optString("sumresource", "0") //???????????????

                var useMlFlow = 0f
                var usedFlowRes = 0f
                var remindFlowRes = 0f
                val freeList = mutableListOf<FlowBean>()
                val resList = mutableListOf<FlowBean>()

                //?????????????????????????????????
                var calTotalUse = 0f

                //??????????????????
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
                                    name = subObj.optString("feePolicyName", "??????"),
                                    total = "0",
                                    used = subObj.optString("use", "0"),
                                    remind = "0",
                                    id = subObj.optString("feePolicyId", "")
                                            + subObj.optString("addupItemCode", "")
                                            + subObj.optString("feePolicyName", "??????")
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

                //???????????????
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
                                        "??????"
                                    ) + " - " + subObj.optString("addUpItemName", ""),
                                    total = subObj.optString("total", "0"),
                                    used = subObj.optString("use", "0"),
                                    remind = subObj.optString("remain", "0"),
                                    id = subObj.optString("feePolicyId", "")
                                            + subObj.optString("addupItemCode", "")
                                            + subObj.optString("feePolicyName", "??????")
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

                //????????????????????????--????????????1G
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
                                        "??????"
                                    ) + " - " + subObj.optString("addUpItemName", ""),
                                    total = subObj.optString("total", "0"),
                                    used = subObj.optString("use", "0"),
                                    remind = subObj.optString("remain", "0"),
                                    id = subObj.optString("feePolicyId", "")
                                            + subObj.optString("addupItemCode", "")
                                            + subObj.optString("feePolicyName", "??????")
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

                //unshare?????? - ????????????????????????
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
                                        "??????"
                                    ) + " - " + subObj.optString("addUpItemName", ""),
                                    total = total.toString(),
                                    used = use.toString(),
                                    remind = if (remian > 0f) remian.toString() else "0",
                                    id = subObj.optString("feePolicyId", "")
                                            + subObj.optString("addupItemCode", "")
                                            + subObj.optString("feePolicyName", "??????")
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

                //?????????????????????
                val unknownFlow = (usedTotal.toFloatOrNull()?:0f) - calTotalUse
                if (unknownFlow > 0.1f){
                    useMlFlow += unknownFlow
                    val unknownBean = FlowBean(
                        isFree = true,
                        name = "????????????-????????????",
                        total = "0",
                        used = unknownFlow.toString(),
                        remind = "0",
                        id = "????????????-????????????"
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

                if (mFlowInfoBean != null && (bean.useResFlow.toFloatOrNull()
                        ?: 0f) > (mFlowInfoBean?.useResFlow?.toFloatOrNull() ?: 0f)
                ) {
                    NotificationHelper.showNormalNotification(
                        title = "????????????????????????",
                        desc = "????????????????????????????????????????????????${(bean.useResFlow.toFloatOrNull() ?: 0f) - (mFlowInfoBean?.useResFlow?.toFloatOrNull() ?: 0f)}Mb",
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
                    "??????${ParseUtil.parseFlowUnit(bean.remindFlow)} | ??????${
                        ParseUtil.parseFlowUnit(
                            bean.useMlFlow
                        )
                    } | ??????${ParseUtil.parseFlowUnit(jump)}", "?????????${getCurrentTime()}$ipInfoStr"
                )

                (FloatWindowManager.getFirstView() as? FloatView)?.updateView(bean.useTotalFlow,bean.useResFlow,bean.useMlFlow,bean.remindFlow,jump)
            } catch (e: Exception) {
                LogUtil.toast("????????????????????????cookie(${e.message})")
                e.printStackTrace()
                LogUtil.log(msg = "json????????????:${e.message}")
                return
            }
        } else {
            LogUtil.toast("????????????????????????:${pair.second}")
        }
    }


    fun getFlowBean():InfoBean?{
        return mFlowInfoBean
    }
}