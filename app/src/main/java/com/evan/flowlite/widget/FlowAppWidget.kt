package com.evan.flowlite.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import com.evan.flowlite.R
import com.evan.flowlite.ui.MainActivity
import com.evan.flowlite.utils.ParseUtil
import com.evan.flowlite.utils.RequestTimer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Implementation of a list app widget.
 */
class FlowAppWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
    }

    companion object {
        private const val REQUEST_CODE_OPEN_ACTIVITY = 1

        @SuppressLint("RemoteViewLayout")
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val activityIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val appOpenIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_OPEN_ACTIVITY,
                activityIntent,
                // API level 31 requires specifying either of
                // PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_MUTABLE
                // See https://developer.android.com/about/versions/12/behavior-changes-12#pending-intent-mutability
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            fun constructRemoteViews(showPkgName: Boolean
            ) = RemoteViews(context.packageName, R.layout.flow_widget_layout).apply {
                setTextViewText(R.id.pkgName, RequestTimer.getFlowBean()?.pkgName)
                setTextViewText(
                    R.id.remindFlow,
                    ParseUtil.parseFlowUnit(RequestTimer.getFlowBean()?.remindFlow ?: "0")
                )
                setTextViewText(
                    R.id.usedFlow,
                    ParseUtil.parseFlowUnit(RequestTimer.getFlowBean()?.useTotalFlow ?: "0")
                )
                setTextViewText(R.id.updateTime, ParseUtil.getCurrentTime())
                setOnClickPendingIntent(R.id.flowRoot, appOpenIntent)
                setViewVisibility(R.id.pkgName,if (showPkgName) View.VISIBLE else View.GONE)

            }
            MainScope().launch {
                RequestTimer.refreshFlow {
                    val remoteViews = constructRemoteViews(false)
                    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
                }
            }

        }
    }
}
