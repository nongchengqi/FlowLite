package com.evan.flowlite.float.window

import android.view.View

/**
 * Desc: 悬浮弹窗window
 */
interface FloatWindow {

    /**
     * Desc: 为悬浮弹窗添加View
     */
    fun setView(floatView: View, width: Int?, height: Int?)

    /**
     * Desc: 移除View
     */
    fun removeView(floatView: View)

    /**
     * Desc: 更新View的位置
     */
    fun updateView(floatView: View, x: Int, y: Int)

}