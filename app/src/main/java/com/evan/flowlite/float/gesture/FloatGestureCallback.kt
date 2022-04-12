package com.evan.flowlite.float.gesture

internal interface FloatGestureCallback {

    /**
     *
     * @param dx x偏移
     * @param dy y偏移
     */
    fun onMove(dx: Int, dy: Int)

    /**
     * 开始移动
     */
    fun onMoveStart() {}

    /**
     * 移动结束
     */
    fun onMoveEnd() {}

    /**
     * Desc: 点击
     */
    fun onTapUp() {}
}