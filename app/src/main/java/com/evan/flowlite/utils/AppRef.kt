package com.evan.flowlite.utils

import android.app.Activity
import android.content.SharedPreferences
import com.evan.flowlite.App
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface PreferenceInterface {
    val spName: String
        get() = "base_app_preference"

    val sharePreference: SharedPreferences
        get() = App.getApp().getSharedPreferences(spName, Activity.MODE_PRIVATE)

    fun bindBool(key: String, default: Boolean = false) = object :
        ReadWriteProperty<PreferenceInterface, Boolean> {
        private var field = sharePreference.getBoolean(key, default)

        override fun getValue(thisRef: PreferenceInterface, property: KProperty<*>): Boolean = field

        override fun setValue(thisRef: PreferenceInterface, property: KProperty<*>, value: Boolean) {
            field = value
            thisRef.sharePreference.edit().putBoolean(key, value).apply()
        }
    }

    fun bindInt(key: String, default: Int = 0) = object :
        ReadWriteProperty<PreferenceInterface, Int> {
        private var field = sharePreference.getInt(key, default)

        override fun getValue(thisRef: PreferenceInterface, property: KProperty<*>): Int = field

        override fun setValue(thisRef: PreferenceInterface, property: KProperty<*>, value: Int) {
            field = value
            thisRef.sharePreference.edit().putInt(key, value).apply()
        }
    }

    fun bindLong(key: String, default: Long = 0) = object :
        ReadWriteProperty<PreferenceInterface, Long> {
        private var field = sharePreference.getLong(key, default)
        override fun getValue(thisRef: PreferenceInterface, property: KProperty<*>): Long = field

        override fun setValue(thisRef: PreferenceInterface, property: KProperty<*>, value: Long) {
            field = value
            thisRef.sharePreference.edit().putLong(key, value).apply()
        }
    }

    fun bindString(key: String, default: String = "") = object :
        ReadWriteProperty<PreferenceInterface, String> {
        private var field = sharePreference.getString(key, default) ?: default
        override fun getValue(thisRef: PreferenceInterface, property: KProperty<*>): String = field

        override fun setValue(thisRef: PreferenceInterface, property: KProperty<*>, value: String) {
            field = value
            thisRef.sharePreference.edit().putString(key, value).apply()
        }
    }

    fun bindStringSet(key: String, default: Set<String> = emptySet()) =
        object : ReadWriteProperty<PreferenceInterface, Set<String>> {
            override fun getValue(thisRef: PreferenceInterface, property: KProperty<*>): Set<String> {
                return thisRef.sharePreference.getStringSet(key, default) ?: default
            }

            override fun setValue(thisRef: PreferenceInterface, property: KProperty<*>, value: Set<String>) {
                thisRef.sharePreference.edit().putStringSet(key, value).apply()
            }
        }
}

object AppPref : PreferenceInterface {
    var json by bindString("json", "{}")
    var updateTime by bindInt("update_time", 5)
    var cookie by bindString("user_cookie", "")
    var usedFlow by bindString("used_flow", "0")
    var usedFlowClearTime by bindLong("used_flow_clear_time", System.currentTimeMillis())
    var autoUpdate by bindBool("auto_update", false)
    var autoClearUsed by bindBool("auto_clear_used", false)
    var userNormalMoveIds by bindString("user_normal_move_ids", "")
    var userFreeMoveIds by bindString("user_free_move_ids", "")

    var mainColor by bindLong("main_color", 0xff09CF7E)

    var unitChangeValue by bindInt("flow_unit_change", 1024)

    //float
    var floatPosition by bindString("float_w_position", "0,0")
    var floatSize by bindString("float_w_size", "500,100")
    var floatBgColor by bindString("float_w_bg_color", "#00000000")
    var floatTextColor by bindString("float_w_text_color", "#000000")
    var floatTextSize by bindInt("float_w_text_size", 14)
    var floatContent by bindString("float_w_content", "剩:[剩] | 跳:[跳]")
    var floatAlpha by bindInt("float_w_alpha", 255)
    var floatSwitch by bindBool("float_w_switch", false)
    var floatAndNotifySwitch by bindBool("float_w_f_n_n_switch", true)
}