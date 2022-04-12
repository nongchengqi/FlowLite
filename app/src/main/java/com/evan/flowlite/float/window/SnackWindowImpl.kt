package com.archer.floatwindow.window

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.evan.flowlite.float.window.FloatWindow

/**
 * Desc: 悬浮在单activity之上的弹窗window实现，不需要权限
 */
internal class SnackWindowImpl(private val activity: Activity) : FloatWindow {

    private val layoutParams = FrameLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)

    override fun setView(floatView: View, width: Int?, height: Int?) {
        layoutParams.width = width ?: WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = height ?: WindowManager.LayoutParams.WRAP_CONTENT
        (activity.window.decorView as ViewGroup).addView(floatView, layoutParams)
    }

    override fun removeView(floatView: View) {
        (activity.window.decorView as ViewGroup).removeView(floatView)
    }

    override fun updateView(floatView: View, x: Int, y: Int) {
        layoutParams.leftMargin = x
        layoutParams.topMargin = y
        floatView.layoutParams = layoutParams
    }
}