package com.example.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.MainActivity
import com.example.R
import com.example.model.Track
import com.example.ui.util.AlbumArtUtil
import com.example.widgets.ReceiverWidgetProvider

class PlaybackService : Service() {

    private val binder = LocalBinder()
    var audioEngine: AudioEngine? = null

    private var mediaSession: MediaSessionCompat? = null

    var onTogglePlayPause: (() -> Unit)? = null
    var onNext: (() -> Unit)? = null
    var onPrevious: (() -> Unit)? = null

    private var currentTrack: Track? = null
    private var isPlayingState: Boolean = false

    inner class LocalBinder : Binder() {
        fun getService(): PlaybackService = this@PlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        audioEngine = AudioEngine(this)
        createNotificationChannel()
        initMediaSession()
        showNotification(null, false)
    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(this, "RetroPlaybackStudioSession").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    audioEngine?.resume()
                }

                override fun onPause() {
                    audioEngine?.pause()
                }

                override fun onSkipToNext() {
                    onNext?.invoke()
                }

                override fun onSkipToPrevious() {
                    onPrevious?.invoke()
                }

                override fun onSeekTo(pos: Long) {
                    audioEngine?.seekTo(pos)
                }

                override fun onStop() {
                    audioEngine?.pause()
                    stopForeground(true)
                    stopSelf()
                }
            })
            isActive = true
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                audioEngine?.resume()
            }
            ACTION_PAUSE -> {
                audioEngine?.pause()
            }
            ACTION_TOGGLE_PLAY_PAUSE -> {
                if (audioEngine?.isPlaying?.value == true) {
                    audioEngine?.pause()
                } else {
                    onTogglePlayPause?.invoke() ?: audioEngine?.resume()
                }
            }
            ACTION_NEXT -> onNext?.invoke()
            ACTION_PREVIOUS -> onPrevious?.invoke()
            ACTION_REWIND_10S -> {
                audioEngine?.let { engine ->
                    val current = engine.currentPosition.value
                    engine.seekTo((current - 10000L).coerceAtLeast(0L))
                }
            }
            ACTION_FFWD_10S -> {
                audioEngine?.let { engine ->
                    val current = engine.currentPosition.value
                    val dur = engine.duration.value
                    engine.seekTo((current + 10000L).coerceAtMost(dur))
                }
            }
            ACTION_STOP -> {
                audioEngine?.pause()
                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    fun showNotification(track: Track?, isPlaying: Boolean, queue: List<Track> = emptyList()) {
        currentTrack = track
        isPlayingState = isPlaying

        // Extract album art bitmap for media session and notification
        val albumArtBitmap = try {
            AlbumArtUtil.getTrackAlbumArtBitmap(this, track)
        } catch (e: Exception) {
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        }

        // Update MediaSession state and metadata
        mediaSession?.let { session ->
            val pos = audioEngine?.currentPosition?.value ?: 0L
            val dur = if (track != null && track.durationMs > 0) track.durationMs else (audioEngine?.duration?.value ?: 0L)

            val stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SEEK_TO
                )
                .setState(
                    if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                    pos,
                    if (isPlaying) 1.0f else 0.0f
                )

            session.setPlaybackState(stateBuilder.build())

            val metaBuilder = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track?.title ?: "Estúdio Hi-Fi")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track?.artist ?: "PIONEER HR-9900")
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track?.album ?: "Playback Studio")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, dur)
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArtBitmap)

            session.setMetadata(metaBuilder.build())
        }

        val notificationIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Previous Action
        val prevIntent = Intent(this, PlaybackService::class.java).apply { action = ACTION_PREVIOUS }
        val prevPendingIntent = PendingIntent.getService(
            this, 10, prevIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val prevAction = NotificationCompat.Action(
            android.R.drawable.ic_media_previous, "Anterior", prevPendingIntent
        )

        // Direct Play or Pause Action to prevent toggle race conditions
        val playPauseActionIntent = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        val playPauseIntent = Intent(this, PlaybackService::class.java).apply { action = playPauseActionIntent }
        val playPausePendingIntent = PendingIntent.getService(
            this, 11, playPauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val playPauseAction = NotificationCompat.Action(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
            if (isPlaying) "Pausar" else "Reproduzir",
            playPausePendingIntent
        )

        // Next Action
        val nextIntent = Intent(this, PlaybackService::class.java).apply { action = ACTION_NEXT }
        val nextPendingIntent = PendingIntent.getService(
            this, 12, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextAction = NotificationCompat.Action(
            android.R.drawable.ic_media_next, "Próximo", nextPendingIntent
        )

        val titleText = track?.title ?: "Estúdio de Playback Hi-Fi"
        val artistText = if (track != null) "${track.artist} • ${track.album}" else "Receiver PIONEER HR-9900"
        val subTextStr = if (track != null) "${track.codec} • 24-BIT / 192KHZ" else "RECEIVER READY"

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(titleText)
            .setContentText(artistText)
            .setSubText(subTextStr)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(albumArtBitmap)
            .setContentIntent(pendingIntent)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)
            .setStyle(
                MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notification = notificationBuilder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Sync with home widgets
        com.example.widgets.WidgetUpdateHelper.updateAllWidgets(this, track, isPlaying, queue)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Playback Hi-Fi Receiver Studio",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificações e controles interativos do Estúdio de Playback"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        mediaSession?.release()
        mediaSession = null
        audioEngine?.release()
        audioEngine = null
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "retro_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.ACTION_PAUSE"
        const val ACTION_TOGGLE_PLAY_PAUSE = "com.example.ACTION_TOGGLE_PLAY_PAUSE"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.example.ACTION_PREVIOUS"
        const val ACTION_REWIND_10S = "com.example.ACTION_REWIND_10S"
        const val ACTION_FFWD_10S = "com.example.ACTION_FFWD_10S"
        const val ACTION_STOP = "com.example.ACTION_STOP"
    }
}

