package com.evan.flowlite.float.window

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager

/**
 * Desc: 悬浮弹窗window实现
 */
internal class FloatWindowImpl(context: Context) : FloatWindow {

    /**
     * 窗口管理器
     */
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    /**
     * 窗口参数
     */
    private val layoutParams = WindowManager.LayoutParams().apply {
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        format = PixelFormat.TRANSLUCENT
        flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        windowAnimations = 0
        type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        gravity = Gravity.TOP or Gravity.START
    }

    override fun setView(floatView: View, width: Int?, height: Int?) {
        layoutParams.width = width ?: WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = height ?: WindowManager.LayoutParams.WRAP_CONTENT
        windowManager.addView(floatView, layoutParams)
    }

    override fun removeView(floatView: View) {
        windowManager.removeView(floatView)
    }

    override fun updateView(floatView: View, x: Int, y: Int) {
        layoutParams.x = x
        layoutParams.y = y
        windowManager.updateViewLayout(floatView, layoutParams)
    }

}