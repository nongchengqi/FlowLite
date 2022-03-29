package com.evan.flowlite

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.text.format.DateUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {
    private var mBinder: FlowBinder? = null
    private var menuItem:MenuItem? = null
    private val mConn = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mBinder = (service as FlowBinder)
            mBinder?.setOnUpdateListener { infoBean ->
                infoBean?.let {
                    updateUi(it)
                }
            }
            mBinder?.parseFromCache()
        }
        override fun onServiceDisconnected(name: ComponentName?) {}

    }

    private fun updateUi(infoBean: InfoBean) {
        findViewById<View>(R.id.baseInfoLayout).visibility = View.VISIBLE
        findViewById<TextView>(R.id.pkgNameTv).text = infoBean.pkgName
        findViewById<TextView>(R.id.totalUsedTv).text = infoBean.useTotalFlow
        findViewById<TextView>(R.id.resUsedTv).text = infoBean.useResFlow
        findViewById<TextView>(R.id.resRemindTv).text = infoBean.remindFlow
        findViewById<TextView>(R.id.freeUsedTv).text = infoBean.useMlFlow

        findViewById<View>(R.id.resInfoLayout).visibility =  if (infoBean.normalFlow.isEmpty())View.GONE else View.VISIBLE
        val resContainer = findViewById<LinearLayout>(R.id.resInfoContainer)
        resContainer.removeAllViews()
        infoBean.normalFlow.forEach {
            val info = FlowInfoView(this)
            info.setupDate(it)
            resContainer.addView(info,LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        findViewById<View>(R.id.mlInfoLayout).visibility =  if (infoBean.mlFlow.isEmpty())View.GONE else View.VISIBLE
        val mlContainer = findViewById<LinearLayout>(R.id.mlInfoContainer)
        mlContainer.removeAllViews()
        infoBean.mlFlow.forEach {
            val info = FlowInfoView(this)
            info.setupDate(it,true)
            mlContainer.addView(info,LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT))
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (AppPref.cookie.isEmpty() && ConfigConst.cookie.isEmpty()){
            startActivity(Intent(this,ConfigActivity::class.java))
        }else if (AppPref.cookie.isNotEmpty()) {
            ConfigConst.cookie = AppPref.cookie
            ConfigConst.queryTime = AppPref.updateTime *DateUtils.MINUTE_IN_MILLIS
        }
        bindService(Intent(this,QueryService::class.java),mConn, Service.BIND_AUTO_CREATE)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menuItem = menu.findItem(R.id.start)

            if (mBinder?.isRunning() != true){
                menuItem?.title = "启动"
            }else {
                menuItem?.title = "刷新"
            }
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.config ->{
            startActivity(Intent(this,ConfigActivity::class.java))
            true
        }
        R.id.start ->{
            if (mBinder?.isRunning() != true){
                if (ConfigConst.cookie.isEmpty()){
                    Toast.makeText(this, "cookie 为空", Toast.LENGTH_SHORT).show()
                } else {
                    menuItem?.title = "刷新"
                    startService(Intent(this, QueryService::class.java))
                }
            }else {
                mBinder?.refresh()
            }
            true
        }
        else -> false
    }
}


class FlowInfoView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.flow_item,this,true)
    }
    fun setupDate(info:FlowBean,isFree:Boolean = false){
        findViewById<TextView>(R.id.pkgNameTv).text = info.name
        if (isFree){
            findViewById<TextView>(R.id.pkgUsedTv).text ="已用：${info.used}"
            val process = (((info.used.toFloatOrNull() ?: 1f) / (100 * 1024)) * 100f).toInt()
            findViewById<ProgressBar>(R.id.progress).progress = process
        } else {
            findViewById<TextView>(R.id.pkgUsedTv).text = App.getApp().getString(R.string.pkg_info,info.remind,info.used,info.total)
            val process = ( ((info.used.toFloatOrNull() ?: 1f) / (info.total.toFloatOrNull() ?: 1f)) * 100f).toInt()
            findViewById<ProgressBar>(R.id.progress).progress = process
        }

    }

}