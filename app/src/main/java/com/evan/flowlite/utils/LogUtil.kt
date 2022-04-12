package com.evan.flowlite.utils

import android.util.Log
import android.widget.Toast
import com.evan.flowlite.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.StringBuilder

object LogUtil {
    private var isRecord = false
    private var logSb:StringBuilder = StringBuilder()

    fun log(tag:String = "LiteFlow",msg:Any?){
        Log.e(tag, "$msg")
        if (isRecord){
            logSb.append(ParseUtil.getCurrentTime())
            logSb.append(" ")
            logSb.append("$msg")
            logSb.append("\n")
        }
    }
    fun getLog():String{
        return logSb.toString()
    }
    fun openLogRecord(){
        isRecord = true
    }

    fun closeLogRecord(){
        logSb.clear()
        isRecord = false
    }

    fun isRecord():Boolean{
        return isRecord
    }

    fun toast(msg:Any?){
        MainScope().launch(Dispatchers.Main){
            Toast.makeText(App.getApp(), "$msg", Toast.LENGTH_SHORT).show()
        }
    }

    fun logAndToast(msg:Any?){
        log(msg = msg)
        toast(msg)
    }

}