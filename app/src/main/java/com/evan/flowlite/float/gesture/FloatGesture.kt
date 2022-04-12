package com.evan.flowlite.float.gesture

import android.view.MotionEvent

internal interface FloatGesture {
    fun onTouchEvent(ev: MotionEvent): Boolean
}