package com.evan.flowlite

import android.text.format.DateUtils
import com.evan.flowlite.utils.AppPref

object ConfigConst {
     var cookie =""
     var queryTime = 5 * DateUtils.MINUTE_IN_MILLIS
     val mainColor get() = AppPref.mainColor
}