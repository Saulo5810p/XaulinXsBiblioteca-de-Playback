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

class FullControlsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId, null, false)
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
            ACTION_REWIND -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_REWIND_10S
                }
                context.startService(serviceIntent)
            }
            ACTION_FFWD -> {
                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = PlaybackService.ACTION_FFWD_10S
                }
                context.startService(serviceIntent)
            }
        }
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.example.widgets.FULL_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.widgets.FULL_NEXT"
        const val ACTION_PREV = "com.example.widgets.FULL_PREV"
        const val ACTION_REWIND = "com.example.widgets.FULL_REWIND"
        const val ACTION_FFWD = "com.example.widgets.FULL_FFWD"

        fun updateAllWidgets(context: Context, track: Track?, isPlaying: Boolean) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, FullControlsWidgetProvider::class.java)
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
                val views = RemoteViews(context.packageName, R.layout.widget_full_controls_layout)

                // Open App Pending Intent
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val openAppPendingIntent = PendingIntent.getActivity(
                    context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                views.setOnClickPendingIntent(R.id.widget_btn_open, openAppPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_display_container, openAppPendingIntent)

                // Control Intents
                views.setOnClickPendingIntent(R.id.widget_btn_play, getPendingBroadcast(context, ACTION_PLAY_PAUSE, 201))
                views.setOnClickPendingIntent(R.id.widget_btn_prev, getPendingBroadcast(context, ACTION_PREV, 202))
                views.setOnClickPendingIntent(R.id.widget_btn_next, getPendingBroadcast(context, ACTION_NEXT, 203))
                views.setOnClickPendingIntent(R.id.widget_btn_rewind, getPendingBroadcast(context, ACTION_REWIND, 204))
                views.setOnClickPendingIntent(R.id.widget_btn_ffwd, getPendingBroadcast(context, ACTION_FFWD, 205))
                views.setOnClickPendingIntent(R.id.widget_btn_favorite, openAppPendingIntent)
                views.setOnClickPendingIntent(R.id.widget_btn_repeat, openAppPendingIntent)

                if (track != null) {
                    views.setTextViewText(R.id.widget_track_title, track.title)
                    views.setTextViewText(R.id.widget_artist_name, "${track.artist} • ${track.album}")
                    views.setTextViewText(R.id.widget_codec_spec, "${track.codec} • ${track.bitrateKbps} KBPS • ${track.sampleRateHz / 1000}KHZ")
                    views.setTextViewText(R.id.widget_extra_info, "DIRECT PCM OUTPUT • TUBE STAGE 1982")

                    val artRes = AlbumArtUtil.getTrackAlbumArtRes(track)
                    views.setImageViewResource(R.id.widget_album_art, artRes)

                    if (track.isFavorite) {
                        views.setImageViewResource(R.id.widget_btn_favorite, R.drawable.ic_widget_favorite)
                    } else {
                        views.setImageViewResource(R.id.widget_btn_favorite, R.drawable.ic_widget_favorite_border)
                    }
                } else {
                    views.setTextViewText(R.id.widget_track_title, "ESTÚDIO HI-FI CONTROLS")
                    views.setTextViewText(R.id.widget_artist_name, "Nenhuma música em execução")
                    views.setTextViewText(R.id.widget_codec_spec, "RECEIVER READY • 24-BIT / 192KHZ")
                    views.setTextViewText(R.id.widget_extra_info, "WAITING FOR AUDIO SIGNAL")
                    views.setImageViewResource(R.id.widget_album_art, R.mipmap.ic_launcher)
                    views.setImageViewResource(R.id.widget_btn_favorite, R.drawable.ic_widget_favorite_border)
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

        private fun getPendingBroadcast(context: Context, actionStr: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, FullControlsWidgetProvider::class.java).apply {
                action = actionStr
            }
            return PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
