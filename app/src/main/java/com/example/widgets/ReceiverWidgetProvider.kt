package com.example.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.model.Track
import com.example.playback.PlaybackService
import com.example.ui.util.AlbumArtUtil

class ReceiverWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId, null, false)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_WIDGET_PLAY_PAUSE -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_TOGGLE_PLAY_PAUSE
                }
                context.startService(serviceIntent)
            }
            ACTION_WIDGET_NEXT -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_NEXT
                }
                context.startService(serviceIntent)
            }
            ACTION_WIDGET_PREV -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_PREVIOUS
                }
                context.startService(serviceIntent)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_PLAY_PAUSE = "com.example.widgets.ACTION_PLAY_PAUSE"
        const val ACTION_WIDGET_NEXT = "com.example.widgets.ACTION_NEXT"
        const val ACTION_WIDGET_PREV = "com.example.widgets.ACTION_PREV"

        fun updateAllWidgets(context: Context, track: Track?, isPlaying: Boolean) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, ReceiverWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

                for (widgetId in widgetIds) {
                    updateAppWidget(context, appWidgetManager, widgetId, track, isPlaying)
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
            isPlaying: Boolean
        ) {
            try {
                val views = RemoteViews(context.packageName, R.layout.widget_receiver_layout)

                // Open App Pending Intent
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_btn_open, openAppPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_display_container, openAppPendingIntent)

                // Play / Pause Pending Intent
                val playPauseIntent = Intent(context, ReceiverWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_PLAY_PAUSE
                }
                val playPausePendingIntent = PendingIntent.getBroadcast(
                    context, 101, playPauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_btn_play, playPausePendingIntent)

                // Previous Pending Intent
                val prevIntent = Intent(context, ReceiverWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_PREV
                }
                val prevPendingIntent = PendingIntent.getBroadcast(
                    context, 102, prevIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPendingIntent)

                // Next Pending Intent
                val nextIntent = Intent(context, ReceiverWidgetProvider::class.java).apply {
                    action = ACTION_WIDGET_NEXT
                }
                val nextPendingIntent = PendingIntent.getBroadcast(
                    context, 103, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_btn_next, nextPendingIntent)

                // Track & Play State Updates
                if (track != null) {
                    views.setTextViewText(R.id.widget_track_title, track.title)
                    views.setTextViewText(R.id.widget_artist_name, "${track.artist} • ${track.album}")
                    views.setTextViewText(R.id.widget_codec_spec, "${track.codec} • 24-BIT / 192KHZ")

                    val artRes = AlbumArtUtil.getTrackAlbumArtRes(track)
                    views.setImageViewResource(R.id.widget_album_art, artRes)
                } else {
                    views.setTextViewText(R.id.widget_track_title, "ESTÚDIO DE PLAYBACK")
                    views.setTextViewText(R.id.widget_artist_name, "Nenhuma música em execução")
                    views.setTextViewText(R.id.widget_codec_spec, "RECEIVER READY • 24-BIT / 192KHZ")
                    views.setImageViewResource(R.id.widget_album_art, R.mipmap.ic_launcher)
                }

                if (isPlaying) {
                    views.setImageViewResource(R.id.widget_btn_play, R.drawable.ic_widget_pause)
                    views.setTextViewText(R.id.widget_status_tag, "• EM REPRODUÇÃO")
                } else {
                    views.setImageViewResource(R.id.widget_btn_play, R.drawable.ic_widget_play)
                    views.setTextViewText(R.id.widget_status_tag, "• PAUSADO")
                }

                appWidgetManager.updateAppWidget(widgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
