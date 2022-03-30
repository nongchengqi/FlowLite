package com.evan.flowlite

import android.app.Activity
import android.content.SharedPreferences
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

object AppPref: PreferenceInterface {
  var json by bindString("json","{}")
  var updateTime by bindInt("update_time",5)
  var cookie by bindString("user_cookie","")
  var usedFlow by bindString("used_flow","0")
  var autoUpdate by bindBool("auto_update",false)
  var userMoveIds by bindString("user_move_ids","")
}