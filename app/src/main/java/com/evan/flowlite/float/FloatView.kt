package com.evan.flowlite.float

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.evan.flowlite.R
import com.evan.flowlite.utils.AppPref
import com.evan.flowlite.utils.ParseUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class FloatView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var textView:TextView? = null
    init {
        LayoutInflater.from(context).inflate(R.layout.float_view,this,true)
        setBackgroundResource(R.drawable.widget_background)
    }

    fun updateView(
        useTotalFlow: String,
        useResFlow: String,
        useMlFlow: String,
        remindFlow: String,
        jump: String
    ) {
        MainScope().launch(Dispatchers.Main){
            if (textView == null){
                textView = findViewById(R.id.text)
            }
            val content =
                AppPref.floatContent.replace("[行]","\n")
                .replace("[总]",ParseUtil.parseFlowUnit(useTotalFlow))
                .replace("[通]",ParseUtil.parseFlowUnit(useResFlow))
                .replace("[剩]",ParseUtil.parseFlowUnit(remindFlow))
                .replace("[免]",ParseUtil.parseFlowUnit(useMlFlow))
                .replace("[跳]",ParseUtil.parseFlowUnit(jump))
            alpha = AppPref.floatAlpha / 255f
            textView?.text = content
            textView?.textSize = AppPref.floatTextSize.toFloat()
            textView?.setTextColor(Color.parseColor(AppPref.floatTextColor))
        }

    }
}