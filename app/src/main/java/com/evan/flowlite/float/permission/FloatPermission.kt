package com.archer.floatwindow.permission

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.evan.flowlite.float.util.appName
import java.lang.reflect.InvocationTargetException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val OP_SYSTEM_ALERT_WINDOW = 24

/**
 * Desc: 请求浮窗权限
 * <p>
 * Author: linjiaqiang
 * Date: 2021/10/28
 */
suspend fun requestOverlaysPermission(fragmentActivity: FragmentActivity) {
    if (!canDrawOverlays(fragmentActivity)) {
        suspendCoroutine<Unit?> {
            showPermissionDeniedDialog(
                activity = fragmentActivity,
                permissionDeniedMessage = String.format("你已禁止授予%s 显示悬浮窗 权限，可能会造成功能不可用，如需使用请到设置里授予权限", fragmentActivity.appName),
                onDenied = { message ->
                    it.resumeWithException(IllegalAccessException(message))
                },
                gotoSettings = {
                    gotoOverlaysPermissionSettings(fragmentActivity)
                },
                requestPermission = {
                    if (canDrawOverlays(fragmentActivity)) {
                        it.resume(null)
                    } else {
                        it.resumeWithException(IllegalAccessException("没有授予弹窗权限"))
                    }
                }
            )
        }
    }
}

/**
 * Desc: 权限被拒绝弹窗（禁止和授予）
 * @param permissionDeniedMessage 弹窗显示的信息，用于提示用户需要给予权限
 * @param onDenied 用于拒绝授予权限
 * @param gotoSettings 跳转权限设置页面
 * @param requestPermission 用于跳转权限设置页面后，返回app，需要重新申请权限
 */
private fun showPermissionDeniedDialog(
    activity: FragmentActivity,
    permissionDeniedMessage: String,
    onDenied: (message: String) -> Unit,
    gotoSettings: () -> Boolean,
    requestPermission: () -> Unit
) {
    val dialog = AlertDialog.Builder(activity)
        .setMessage(permissionDeniedMessage)
        .setCancelable(false)
        .setNegativeButton("取消") { dialog, _ ->
            dialog.dismiss()
            onDenied.invoke("取消申请弹窗权限")
        }
        .setPositiveButton("授权") { dialog, _ ->
            dialog.dismiss()
            if (gotoSettings()) {
                whenNextResume(activity) {
                    requestPermission.invoke()
                }
            } else {
                onDenied.invoke("跳转权限设置页面失败")
            }
        }
        .create()
    dialog.show()
    dialog.setOnCancelListener {
        onDenied.invoke("关闭申请弹窗权限弹窗")
    }
}

/**
 * Desc: 当从后台返回时，再次检查权限
 */
private fun whenNextResume(fragmentActivity: FragmentActivity, onResume: () -> Unit) {
    val observer = object : DefaultLifecycleObserver {
        private var isStop = false

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            isStop = true
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            if (!isStop) {
                return
            }
            onResume.invoke()
            fragmentActivity.lifecycle.removeObserver(this)
        }
    }
    fragmentActivity.lifecycle.addObserver(observer)
}

/**
 * Desc: 检查是否有浮窗权限
 * <p>
 * Author: linjiaqiang
 * Date: 2021/10/27
 */
fun canDrawOverlays(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else checkOp(context, OP_SYSTEM_ALERT_WINDOW)
}

/**
 * Desc: 申请浮窗权限
 * <p>
 * Author: linjiaqiang
 * Date: 2021/10/28
 */
@SuppressLint("QueryPermissionsNeeded")
fun gotoOverlaysPermissionSettings(context: Context): Boolean {
    val intent = Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + context.packageName))
    if (context !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Log.e("FloatWindow","No activity to handle intent")
        return false
    }
    return true
}

@SuppressLint("DiscouragedPrivateApi")
private fun checkOp(context: Context, op: Int): Boolean {
    val manager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val clazz: Class<*> = AppOpsManager::class.java
    try {
        val method = clazz.getDeclaredMethod("checkOp", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java)
        return AppOpsManager.MODE_ALLOWED == method.invoke(manager, op, Process.myUid(), context.packageName) as Int
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    }
    return true
}