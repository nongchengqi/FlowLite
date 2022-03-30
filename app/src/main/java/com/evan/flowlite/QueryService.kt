package com.evan.flowlite

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.*
import android.util.Log
import android.widget.Toast
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection

class QueryService : Service() {
    private var mFlowInfoBean: InfoBean? = null
    private var mUpdateListener: ((InfoBean?) -> Unit)? = null
    private var isDestroy = false
    private var isQueryRunning = false
    private var isInNetwork = false
    private val mThreadHandle by lazy {
        Handler(HandlerThread("refresh").apply { start() }.looper) { msg ->
            if (msg.what == 10010) {
                decodeFlowInfo(msg.obj as? Pair<Boolean, String>)
                true
            } else {
                false
            }
        }
    }
    private val simpleDateFormat by lazy { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()) }
    private val mMainHandler = Handler(Looper.getMainLooper())
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            if (isQueryRunning && AppPref.autoUpdate){
               refresh()
            }
        }
    }

    fun refresh(){
        if (!isInNetwork) {
            Thread {
                doNet()
            }.start()
        }
    }

    private fun decodeFlowInfo(pair: Pair<Boolean, String>?) {
        if (pair == null) {
            return
        }
        if (pair.first) {
            AppPref.json = pair.second
            try {
                val rootObj = JSONObject(pair.second)
                val pkgName = rootObj.optString("packageName", "未知")
                val usedTotal = rootObj.optString("sum", "-1") //总使用
                val usedTotalRes = rootObj.optString("sumresource", "-1") //套餐内使用

                //免流流量解析
                val mlArr = rootObj.optJSONArray("MlResources")
                val mlList = mutableListOf<FlowBean>()
                var useMlFlow = 0f
                mlArr?.let {
                    for (first in 0 until it.length()) {
                        val subMlObj = it.optJSONObject(first)
                        if (subMlObj.optString("type", "") != "MlFlowdetailsList") {
                            continue
                        }
                        useMlFlow += subMlObj.optString("userResource", "0").toFloatOrNull() ?: 0f
                        val subMlArr = subMlObj.optJSONArray("details")
                        subMlArr?.let { subArr ->
                            for (second in 0 until subArr.length()) {
                                val subObj = subArr.optJSONObject(second)
                                mlList.add(
                                    FlowBean(
                                        name = subObj.optString("feePolicyName", "未知"),
                                        total = "-1",
                                        used = subObj.optString("use", "-1"),
                                        remind = "-1",
                                        id = subObj.optString("feePolicyId", ""),
                                    )
                                )
                            }
                        }
                    }
                }

                //流量包解析
                val flowArr = rootObj.optJSONArray("resources")
                val flowList = mutableListOf<FlowBean>()
                var usedFlowRes = 0f
                var remindFlowRes = 0f
                flowArr?.let {
                    for (first in 0 until it.length()) {
                        val subFlowObj = it.optJSONObject(first)
                        if (subFlowObj.optString("type", "") != "flow") {
                            continue
                        }
                        usedFlowRes += subFlowObj.optString("userResource", "0").toFloatOrNull()
                            ?: 0f
                        remindFlowRes += subFlowObj.optString("remainResource", "0").toFloatOrNull()
                            ?: 0f
                        val subFlowArr = subFlowObj.optJSONArray("details")
                        subFlowArr?.let { subArr ->
                            for (second in 0 until subArr.length()) {
                                val subObj = subArr.optJSONObject(second)
                                val flowBean = FlowBean(
                                    name = subObj.optString(
                                        "feePolicyName",
                                        "未知"
                                    ) + " - " + subObj.optString("addUpItemName", ""),
                                    total = subObj.optString("total", "-1"),
                                    used = subObj.optString("use", "-1"),
                                    remind = subObj.optString("remain", "-1"),
                                    id = subObj.optString("feePolicyId", "")
                                )
                                if (AppPref.userMoveIds.contains("#${flowBean.id}#")){
                                    mlList.add(flowBean.copy(isUserMove = true))
                                    val fUsed = flowBean.used.toFloat()
                                    val fRemind = flowBean.remind.toFloat()
                                    usedFlowRes  -= fUsed
                                    remindFlowRes -= fRemind
                                    useMlFlow += fUsed
                                } else {
                                    flowList.add(flowBean)
                                }

                            }
                        }
                    }
                }

                val bean = InfoBean(
                    pkgName = pkgName,
                    mlFlow = mlList,
                    normalFlow = flowList,
                    remindFlow = remindFlowRes.toString(),
                    useMlFlow = useMlFlow.toString(),
                    useResFlow = usedFlowRes.toString(),
                    useTotalFlow = usedTotal,
                )

                if (mFlowInfoBean != null && (bean.useResFlow.toFloatOrNull()
                        ?: 0f) > (mFlowInfoBean?.useResFlow?.toFloatOrNull() ?: 0f)
                ) {
                    NotificationHelper.showNormalNotification(
                        title = "通用流量发生消耗",
                        desc = "本次刷新期间，您的通用流量消耗了${(bean.useResFlow.toFloatOrNull() ?: 0f) - (mFlowInfoBean?.useResFlow?.toFloatOrNull() ?: 0f)}Mb",
                        smallIcon = R.mipmap.ic_launcher_round,
                        clickIntent = Intent(this, MainActivity::class.java),
                        bigIcon = null
                    )
                }
                mFlowInfoBean = bean
                mMainHandler.post {
                    mUpdateListener?.invoke(mFlowInfoBean)
                }
                val used = AppPref.usedFlow
                val jump = if (used == "0") {
                    AppPref.usedFlow = bean.useResFlow
                    "0"
                } else {
                    ((bean.useResFlow.toFloatOrNull() ?: 0f) - used.toFloat()).toString()
                }
                startForeground(
                    10010, NotificationHelper.getNotification(
                        title = "剩：${bean.remindFlow} | 免：${bean.useMlFlow} | 跳：$jump",
                        desc = "更新于：${simpleDateFormat.format(System.currentTimeMillis())}",
                        clickIntent = Intent(this, MainActivity::class.java),
                        smallIcon = R.mipmap.ic_launcher_round
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(this, "获取失败，请更新cookie(${e.message})", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                return
            }
        } else {
            Toast.makeText(this, "获取流量信息失败:${pair.second}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(
            10010, NotificationHelper.getNotification(
                title = "正在等待信息更新",
                desc = "如果没配置Cookie，请配置好并启动服务",
                clickIntent = Intent(this, MainActivity::class.java),
                smallIcon = R.mipmap.ic_launcher_round
            )
        )
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.registerNetworkCallback(NetworkRequest.Builder().build(),networkCallback)
    }

    override fun onBind(intent: Intent): IBinder {
        return FlowBinder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isQueryRunning = true
        mThreadHandle.post(object :Runnable{
            override fun run() {
                refresh()
                Log.e("TAG", "refresh: $isDestroy  ${ConfigConst.queryTime}" )
                if (!isDestroy) {
                    mThreadHandle.postDelayed(this, ConfigConst.queryTime)
                }
            }
        })
        return super.onStartCommand(intent, flags, startId)
    }



    private fun doNet() {
        isInNetwork = true
        val url =
            URL("https://m.client.10010.com/servicequerybusiness/operationservice/queryOcsPackageFlowLeftContentRevisedInJune")
        val httpsURLConnection = url.openConnection() as HttpsURLConnection
        httpsURLConnection.requestMethod = "POST"
        httpsURLConnection.addRequestProperty("content-type", "application/x-www-form-urlencoded")
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
            Log.e("TAG", "doNet: $result" )
            Pair(true, result)
        } else {
            Pair(false, "responseCode:$responseCode")
        }
        mThreadHandle.sendMessage(mThreadHandle.obtainMessage().apply {
            what = 10010
            obj = flowStr
        })
        isInNetwork = false
    }

    override fun onDestroy() {
        isDestroy = true
        super.onDestroy()
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connMgr.unregisterNetworkCallback(networkCallback)
    }

    fun isRunning() = isQueryRunning

    fun setOnUpdateListener(cb: (InfoBean?) -> Unit) {
        mUpdateListener = cb
    }

    fun parseFromCache() {
        val json = AppPref.json
        if (json != "{}") {
            decodeFlowInfo(Pair(true, json))
        }
    }

}

class FlowBinder(private val service: QueryService) : Binder() {
    fun setOnUpdateListener(cb: (InfoBean?) -> Unit) {
        service.setOnUpdateListener(cb)
    }

    fun isRunning():Boolean{
        return service.isRunning()
    }

    fun parseFromCache(){
        service.parseFromCache()
    }

    fun refresh(){
        service.refresh()
    }
}