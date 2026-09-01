package com.example.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.model.Track
import com.example.playback.PlaybackService
import com.example.ui.util.AlbumArtUtil

class QueueWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId, null, false, emptyList())
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_PLAY_PAUSE -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_TOGGLE_PLAY_PAUSE
                }
                context.startService(serviceIntent)
            }
            ACTION_NEXT -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_NEXT
                }
                context.startService(serviceIntent)
            }
            ACTION_PREV -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_PREVIOUS
                }
                context.startService(serviceIntent)
            }
        }
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.widgets.QUEUE_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.widgets.QUEUE_NEXT"
        const val ACTION_PREV = "com.example.widgets.QUEUE_PREV"

        fun updateAllWidgets(context: Context, track: Track?, isPlaying: Boolean, queue: List<Track> = emptyList()) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, QueueWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

                for (widgetId in widgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId, track, isPlaying, queue)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int,
            track: Track?,
            isPlaying: Boolean,
            queue: List<Track>
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_queue_layout)

                // Open App Pending Intent
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_btn_open, openAppPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_display_container, openAppPendingIntent)

                // Controls
                views.setOnClickPendingIntent(R.id.widget_btn_play, getPendingBroadcast(context, ACTION_PLAY_PAUSE, 301))
                views.setOnClickPendingIntent(R.id.widget_btn_prev, getPendingBroadcast(context, ACTION_PREV, 302))
                views.setOnClickPendingIntent(R.id.widget_btn_next, getPendingBroadcast(context, ACTION_NEXT, 303))

                if (track != null) {
                    views.setTextViewText(R.id.widget_track_title, track.title)
                    views.setTextViewText(R.id.widget_artist_name, "${track.artist} • ${track.album}")

                    val artRes = AlbumArtUtil.getTrackAlbumArtRes(track)
                    views.setImageViewResource(R.id.widget_album_art, artRes)
                } else {
                    views.setTextViewText(R.id.widget_track_title, "BIBLIOTECA DE PLAYBACK")
                    views.setTextViewText(R.id.widget_artist_name, "Nenhuma música em execução")
                    views.setImageViewResource(R.id.widget_album_art, R.mipmap.ic_launcher)
                }

                if (isPlaying) {
                    views.setImageViewResource(R.id.widget_btn_play, R.drawable.ic_widget_pause)
                    views.setTextViewText(R.id.widget_status_tag, "• EM REPRODUÇÃO")
                } else {
                    views.setImageViewResource(R.id.widget_btn_play, R.drawable.ic_widget_play)
                    views.setTextViewText(R.id.widget_status_tag, "• PAUSADO")
                }

                // Populate Up Next Queue Items
                val upcomingTracks = if (track != null && queue.isNotEmpty()) {
                    val currentIndex = queue.indexOfFirst { it.id == track.id }
                    if (currentIndex >= 0 && currentIndex < queue.size - 1) {
                        queue.subList(currentIndex + 1, queue.size)
                    } else {
                        queue.filter { it.id != track.id }
                    }
                } else {
                    queue
                }

                // Slot 1
                if (upcomingTracks.size > 0) {
                    val t1 = upcomingTracks[0]
                    views.setTextViewText(R.id.widget_q1_title, t1.title)
                    views.setTextViewText(R.id.widget_q1_artist, t1.artist)
                    views.setTextViewText(R.id.widget_q1_spec, t1.codec)
                    views.setViewVisibility(R.id.widget_q1_container, View.VISIBLE)
                    views.setOnClickPendingIntent(R.id.widget_q1_container, openAppPendingIntent)
                } else {
                    views.setViewVisibility(R.id.widget_q1_container, View.GONE)
                }

                // Slot 2
                if (upcomingTracks.size > 1) {
                    val t2 = upcomingTracks[1]
                    views.setTextViewText(R.id.widget_q2_title, t2.title)
                    views.setTextViewText(R.id.widget_q2_artist, t2.artist)
                    views.setTextViewText(R.id.widget_q2_spec, t2.codec)
                    views.setViewVisibility(R.id.widget_q2_container, View.VISIBLE)
                    views.setOnClickPendingIntent(R.id.widget_q2_container, openAppPendingIntent)
                } else {
                    views.setViewVisibility(R.id.widget_q2_container, View.GONE)
                }

                // Slot 3
                if (upcomingTracks.size > 2) {
                    val t3 = upcomingTracks[2]
                    views.setTextViewText(R.id.widget_q3_title, t3.title)
                    views.setTextViewText(R.id.widget_q3_artist, t3.artist)
                    views.setTextViewText(R.id.widget_q3_spec, t3.codec)
                    views.setViewVisibility(R.id.widget_q3_container, View.VISIBLE)
                    views.setOnClickPendingIntent(R.id.widget_q3_container, openAppPendingIntent)
                } else {
                    views.setViewVisibility(R.id.widget_q3_container, View.GONE)
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun getPendingBroadcast(context: Context, actionStr: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, QueueWidgetProvider::class.java).apply {
                action = actionStr
            }
            return PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
