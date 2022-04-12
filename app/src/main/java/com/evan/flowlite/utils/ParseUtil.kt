package com.evan.flowlite.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

object ParseUtil {
    private val simpleDateFormat by lazy { SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()) }

    fun parseFlowUnit(flow:String):String{
        val ff = flow.toFloatOrNull()?:0f
       return if (ff > AppPref.unitChangeValue){
           "${((ff / 1024f) * 100f).toInt() / 100f}G"
        }else {
           "${(ff * 1000).toInt() / 1000}M"
        }
    }

    fun getCurrentTime():String{
        return simpleDateFormat.format(System.currentTimeMillis())
    }
    fun getFormatTime(time:Long):String{
        return simpleDateFormat.format(time)
    }

    fun isSameDay(time:Long) = DateUtils.isToday(time)
}