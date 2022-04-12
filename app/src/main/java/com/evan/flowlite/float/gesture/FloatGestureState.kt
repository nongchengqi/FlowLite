package com.evan.flowlite.float.gesture

import androidx.annotation.IntDef

/**
 * Desc: 悬浮窗手势状态
 */
@IntDef(FloatGestureState.SETTLING, FloatGestureState.MOVE, FloatGestureState.IDEL)
internal annotation class FloatGestureState {

    companion object {
        /**
         * view正在触摸但未达到触发是否拖拽的状态
         */
        const val SETTLING = 1

        /**
         * 正在移动
         */
        const val MOVE = 2

        /**
         * view未触摸或触发拖拽失败的状态
         */
        const val IDEL = 3
    }
}