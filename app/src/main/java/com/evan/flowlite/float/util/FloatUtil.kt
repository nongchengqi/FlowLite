package com.evan.flowlite.float.util

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.core.view.ViewPropertyAnimatorListener
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Desc: 协程调用动画
 */
suspend fun ViewPropertyAnimatorCompat.await() {
    suspendCoroutine<Unit?> {
        var isResume = false
        setListener(object : ViewPropertyAnimatorListener {
            override fun onAnimationStart(view: View?) {
            }

            override fun onAnimationEnd(view: View?) {
                if (isResume) return
                isResume = true
                it.resume(null)
            }

            override fun onAnimationCancel(view: View?) {
                if (isResume) return
                isResume = true
                it.resume(null)
            }
        }).start()
    }
}

/**
 * Desc: 等待View测量完以获取宽高
 */
suspend fun View.awaitLayout() {
    if (width > 0) return
    suspendCoroutine<Unit?> {
        post {
            it.resume(null)
        }
    }
}

/**
 * Desc: 获取Activity的根布局
 */
val Activity.rootView: View
    get() = findViewById(android.R.id.content)

val Activity.decorView: View
    get() = window.decorView

private var sPoint: Point? = null

fun getScreenWidth(context: Context): Int {
    if (sPoint == null) {
        sPoint = Point()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getSize(sPoint)
    }
    return sPoint!!.x
}

fun getScreenHeight(context: Context): Int {
    if (sPoint == null) {
        sPoint = Point()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getSize(sPoint)
    }
    return sPoint!!.y
}

/**
 * Desc: 获取虚拟按键的高度
 */
fun getNavigationBarHeight(context: Context): Int {
    var result = 0
    val res = context.resources
    val resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = res.getDimensionPixelSize(resourceId)
    }
    return result
}

/**
 * Desc: 获取状态栏高度
 */
fun getStatusBarHeight(context: Context): Int {
    var result = 0
    val resId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resId > 0) {
        result = context.resources.getDimensionPixelOffset(resId)
    }
    return result
}

val Context.appName
    get() = packageManager.getPackageInfo(
        packageName,
        0
    )?.applicationInfo?.loadLabel(packageManager).toString()