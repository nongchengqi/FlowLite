package com.evan.flowlite

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
import androidx.appcompat.widget.SwitchCompat

class ConfigActivity : AppCompatActivity() {
    private val powerMgr by lazy { getSystemService(POWER_SERVICE) as PowerManager }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
        checkPower()
        initUi()
    }

    override fun onResume() {
        super.onResume()
        checkPower(true)
    }

    private fun initUi() {
        findViewById<View>(R.id.clear).setOnClickListener {
            AppPref.usedFlow = "0"
            Toast.makeText(this, "已清除，下次刷新生效", Toast.LENGTH_SHORT).show()
        }

        val switchCompat = findViewById<SwitchCompat>(R.id.switchCompat)
        switchCompat.isChecked = AppPref.autoUpdate
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != AppPref.autoUpdate) {
                AppPref.autoUpdate = isChecked
            }
        }
        switchCompat.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != AppPref.autoUpdate) {
                AppPref.autoUpdate = isChecked
            }
        }

        val cookie = findViewById<EditText>(R.id.cookie)
        val cCookie = findViewById<EditText>(R.id.cCookie)
        val time = findViewById<EditText>(R.id.time)

        time.setText(AppPref.updateTime.toString())
        cookie.setText(AppPref.cookie)

        findViewById<View>(R.id.save).setOnClickListener {
            AppPref.updateTime = time.text.toString().toInt()
            ConfigConst.queryTime = AppPref.updateTime * DateUtils.MINUTE_IN_MILLIS
            val cookieStr = cookie.text.toString()
            val cCookieStr = cCookie.text.toString()
            when {
                cookieStr.isNotEmpty() -> {
                    AppPref.cookie = cookieStr
                    ConfigConst.cookie = AppPref.cookie
                }
                cCookieStr.isNotEmpty() -> {
                    AppPref.cookie = cCookieStr.replace("\ncookie: ", "; ").replace("cookie: ","")
                    ConfigConst.cookie = AppPref.cookie
                }
                else -> {
                    Toast.makeText(this, "cookie 为空", Toast.LENGTH_SHORT).show()
                }
            }
            finish()
        }
    }

    @SuppressLint("BatteryLife")
    private fun checkPower(checkMode:Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkMode) {
                findViewById<SwitchCompat>(R.id.powerOpt).isChecked = powerMgr.isIgnoringBatteryOptimizations(packageName)
            } else {
                findViewById<View>(R.id.powerLayout).visibility = View.VISIBLE
                val switchPower = findViewById<SwitchCompat>(R.id.powerOpt)
                switchPower.isChecked = powerMgr.isIgnoringBatteryOptimizations(packageName)
                switchPower.setOnCheckedChangeListener { _,_->
                    if (checkMode){
                        return@setOnCheckedChangeListener
                    }
                    kotlin.runCatching {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                }
            }

        } else {
            findViewById<View>(R.id.powerLayout).visibility = View.GONE
        }
    }
}