package com.evan.flowlite.float.gesture

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

internal class FloatGestureImpl(context: Context) : FloatGesture {

    /**
     * 触摸起点X
     */
    private var touchStartX = 0

    /**
     * 触摸起点Y
     */
    private var touchStartY = 0

    /**
     * 上一次触摸X
     */
    private var lastX = 0

    /**
     * 上一次触摸Y
     */
    private var lastY = 0

    /**
     * 大于这个距离，视为拖拽
     */
    private val slopDistance = ViewConfiguration.get(context).scaledTouchSlop

    /**
     * 当前状态
     */
    private var state = FloatGestureState.IDEL

    /**
     * 回调
     */
    private var callback: FloatGestureCallback? = null

    /**
     * Desc: 设置回调
     */
    fun setGestureCallback(callback: FloatGestureCallback?) {
        this.callback = callback
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val x = ev.rawX.toInt()
        val y = ev.rawY.toInt()
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = x
                touchStartY = y
                lastX = x
                lastY = y
                state = FloatGestureState.SETTLING
            }
            MotionEvent.ACTION_MOVE -> {
                // 未达到移动阙值
                if (abs(x - touchStartX) < slopDistance && abs(y - touchStartY) < slopDistance) {
                    if (state != FloatGestureState.MOVE) {
                        return true
                    }
                } else if (state == FloatGestureState.SETTLING) {
                    // 开始移动
                    state = FloatGestureState.MOVE
                    callback?.onMoveStart()
                }
                callback?.onMove(x - lastX, y - lastY)
                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_UP -> {
                // 未达到移动阙值，视为点击
                if (state == FloatGestureState.SETTLING) {
                    callback?.onTapUp()
                }
                if (state == FloatGestureState.MOVE) {
                    callback?.onMoveEnd()
                }
                state = FloatGestureState.IDEL
            }
            MotionEvent.ACTION_CANCEL -> {
                if (state == FloatGestureState.MOVE) {
                    callback?.onMoveEnd()
                }
                state = FloatGestureState.IDEL
            }
        }
        return state == FloatGestureState.MOVE
    }
}