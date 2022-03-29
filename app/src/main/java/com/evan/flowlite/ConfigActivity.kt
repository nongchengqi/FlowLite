package com.evan.flowlite

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat

class ConfigActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)
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
            if (cookieStr.isNotEmpty()) {
                AppPref.cookie = cookieStr
                ConfigConst.cookie = AppPref.cookie
            } else if (cCookieStr.isNotEmpty()) {
                AppPref.cookie = cCookieStr.replace("\ncookie: ", "; ").replace("cookie: ","")
                ConfigConst.cookie = AppPref.cookie
            } else {
                Toast.makeText(this, "cookie 为空", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }
}