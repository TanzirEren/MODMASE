package com.tanzirdev.modmase

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import org.json.JSONObject

class ModmaseWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pending = goAsync()
        Thread {
            try {
                fetchLatest(context)
            } catch (e: Throwable) {
                // keep cached text
            }
            try {
                val views = build(context)
                for (id in appWidgetIds) appWidgetManager.updateAppWidget(id, views)
            } finally {
                pending.finish()
            }
        }.start()
    }

    companion object {
        private fun fetchLatest(ctx: Context) {
            val text = Api.getSync(
                Api.DB + "/posts.json?orderBy=%22createdAt%22&limitToLast=1"
            ) ?: return
            if (text == "null") return
            val o = JSONObject(text)
            val keys = o.keys()
            if (!keys.hasNext()) return
            val p = o.optJSONObject(keys.next()) ?: return
            ctx.getSharedPreferences("modmase_widget", Context.MODE_PRIVATE).edit()
                .putString("title", p.optString("title", ""))
                .putString("link", p.optString("link", ""))
                .apply()
        }

        fun build(ctx: Context): RemoteViews {
            val sp = ctx.getSharedPreferences("modmase_widget", Context.MODE_PRIVATE)
            val title = sp.getString("title", "") ?: ""
            val views = RemoteViews(ctx.packageName, R.layout.widget_modmase)
            views.setTextViewText(
                R.id.w_title,
                if (title.isBlank()) "Open MODMASE for the latest mods" else title
            )

            val open = PendingIntent.getActivity(
                ctx, 1,
                Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.w_root, open)
            views.setOnClickPendingIntent(R.id.w_title, open)

            val join = PendingIntent.getActivity(
                ctx, 2,
                Intent(Intent.ACTION_VIEW, Uri.parse(Links.TELEGRAM)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            views.setOnClickPendingIntent(R.id.w_join, join)
            return views
        }

        fun refresh(ctx: Context) {
            try {
                val mgr = AppWidgetManager.getInstance(ctx)
                mgr.updateAppWidget(ComponentName(ctx, ModmaseWidget::class.java), build(ctx))
            } catch (e: Throwable) {
                // ignore
            }
        }
    }
}
