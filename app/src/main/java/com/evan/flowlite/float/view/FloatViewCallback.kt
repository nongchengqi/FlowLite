package com.evan.flowlite.float.view

/**
 * Desc: 浮窗回调
 */
interface FloatViewCallback {

    /**
     * Desc: 位置更新
     */
    fun onLocationChange(x: Int, y: Int)

    /**
     * Desc: 点击
     */
    fun onTapUp()
}