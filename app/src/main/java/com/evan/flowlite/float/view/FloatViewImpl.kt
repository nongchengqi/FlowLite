package com.evan.flowlite.float.view

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.evan.flowlite.float.window.FloatWindow
import com.evan.flowlite.float.gesture.FloatGestureCallback
import com.evan.flowlite.float.gesture.FloatGestureImpl
import com.evan.flowlite.float.util.awaitLayout
import com.evan.flowlite.float.util.getScreenHeight
import com.evan.flowlite.float.util.getScreenWidth
import com.evan.flowlite.float.util.getStatusBarHeight
import kotlin.math.max
import kotlin.math.min

/**
 * Desc: 浮窗View管理
 */
@SuppressLint("ClickableViewAccessibility")
class FloatViewImpl(context: Context) : FloatGestureCallback, View.OnLayoutChangeListener {

    /**
     * 手势
     */
    private val gesture = FloatGestureImpl(context)

    /**
     * window
     */
    private var floatWindow: FloatWindow? = null

    /**
     * View
     */
    private var floatView: View? = null

    private val screenWidth = getScreenWidth(context)

    private val screenHeight = getScreenHeight(context)

    private val statusBarHeight = getStatusBarHeight(context)

    /**
     * 悬浮窗位置
     */
    private var floatX = 0
    private var floatY = 0

    /**
     * 是否开启自动修正位置
     */
    private var fixedEdge = true

    /**
     * 回调
     */
    private var callback: FloatViewCallback? = null

    /**
     * Desc: 添加悬浮窗
     */
    suspend fun setView(
        floatView: View,
        floatWindow: FloatWindow,
        fixedEdge: Boolean,
        floatX: Int,
        floatY: Int,
        width: Int,
        height: Int,
    ) {
        this.floatView = floatView
        this.floatWindow = floatWindow
        this.floatY = max(floatY, statusBarHeight)
        this.fixedEdge = fixedEdge
        floatWindow.setView(floatView, width, height)
        if (fixedEdge) {
            if (floatX < screenWidth / 2) {
                this.floatX = 0
            } else {
                floatView.visibility = View.INVISIBLE
                floatView.awaitLayout()
                floatView.visibility = View.VISIBLE
                this.floatX = screenWidth - floatView.width
            }
        } else {
            this.floatX = floatX
        }
        updateLocation()
        floatView.awaitLayout()
        fixLocation()
        floatView.setOnTouchListener { _, event ->
            gesture.onTouchEvent(event)
            true
        }
        gesture.setGestureCallback(this)
        floatView.removeOnLayoutChangeListener(this)
        floatView.addOnLayoutChangeListener(this)
    }

    /**
     * Desc: 移除悬浮窗
     */
    fun removeView() {
        val view = getView() ?: return
        floatWindow?.removeView(view)
        floatView = null
    }

    /**
     * Desc: 获取悬浮窗View
     */
    fun getView(): View? {
        return floatView
    }

    fun getFloatX(): Int {
        return floatX
    }

    fun getFloatY(): Int {
        return floatY
    }

    /**
     * Desc: 回调
     * <p>
     * Author: linjiaqiang
     * Date: 2021/10/28
     */
    fun setFloatViewCallback(callback: FloatViewCallback) {
        this.callback = callback
    }

    /**
     * Desc: 刷新一下位置
     */
    private fun updateLocation() {
        val view = getView() ?: return
        if (!fixedEdge) {
            floatX = min(max(0, floatX), screenWidth - view.width)
            floatY = min(max(statusBarHeight, floatY), screenHeight + statusBarHeight - view.height)
        }
        callback?.onLocationChange(floatX, floatY)
        floatWindow?.updateView(view, floatX, floatY)
    }

    override fun onMove(dx: Int, dy: Int) {
        floatX += dx
        floatY += dy
        updateLocation()
    }

    override fun onMoveEnd() {
        fixLocation()
    }

    /**
     * Desc: 修正位置
     */
    private fun fixLocation() {
        val floatView = floatView ?: return
        if (!fixedEdge) {
            updateLocation()
            return
        }
        // 临界值判断
        val endX = if (floatX <= screenWidth / 2) 0 else screenWidth - floatView.width
        var endY = floatY
        if (endY < statusBarHeight) {
            endY = statusBarHeight
        }
        if (endY > screenHeight + statusBarHeight - floatView.height) {
            endY = screenHeight + statusBarHeight - floatView.height
        }
        val pvhX = PropertyValuesHolder.ofInt("x", floatX, endX)
        val pvhY = PropertyValuesHolder.ofInt("y", floatY, endY)
        val animator = ObjectAnimator.ofPropertyValuesHolder(pvhX, pvhY)
        animator.addUpdateListener { animation ->
            floatX = animation.getAnimatedValue("x") as Int
            floatY = animation.getAnimatedValue("y") as Int
            updateLocation()
        }
        animator.duration = 300
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    override fun onTapUp() {
        callback?.onTapUp()
    }

    override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
        if (right - left != oldRight - oldLeft || bottom - top != oldBottom - oldTop) {
            fixLocation()
        }
    }

}