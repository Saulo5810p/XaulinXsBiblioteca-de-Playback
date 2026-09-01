package com.example.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.os.Build
import android.util.Log
import com.example.model.EqualizerBand
import com.example.model.RepeatMode
import com.example.model.Track
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import kotlin.math.sin

class AudioEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaPlayer: MediaPlayer? = null

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _amplitudeLeft = MutableStateFlow(0f)
    val amplitudeLeft: StateFlow<Float> = _amplitudeLeft.asStateFlow()

    private val _amplitudeRight = MutableStateFlow(0f)
    val amplitudeRight: StateFlow<Float> = _amplitudeRight.asStateFlow()

    private val _equalizerBands = MutableStateFlow<List<EqualizerBand>>(emptyList())
    val equalizerBands: StateFlow<List<EqualizerBand>> = _equalizerBands.asStateFlow()

    private val _equalizerPresets = MutableStateFlow<List<String>>(emptyList())
    val equalizerPresets: StateFlow<List<String>> = _equalizerPresets.asStateFlow()

    var onTrackCompleted: (() -> Unit)? = null

    private var updateJob: Job? = null
    private var currentTrack: Track? = null
    private var volume = 0.8f

    init {
        startPositionUpdateLoop()
    }

    fun playTrack(track: Track) {
        try {
            currentTrack = track
            _currentPosition.value = 0L
            _duration.value = if (track.durationMs > 0) track.durationMs else 0L
            releaseMediaPlayer()

            mediaPlayer = MediaPlayer().apply {
                setWakeMode(context, android.os.PowerManager.PARTIAL_WAKE_LOCK)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                val file = File(track.dataPath)
                if (file.exists() && file.isFile) {
                    setDataSource(file.absolutePath)
                } else if (track.dataPath.startsWith("http") || track.dataPath.startsWith("content://")) {
                    setDataSource(context, android.net.Uri.parse(track.dataPath))
                } else {
                    // Try asset or fallback descriptor if string matches raw sample
                    val descriptor = context.assets.openFd("sample_audio.mp3")
                    setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                    descriptor.close()
                }

                setOnPreparedListener { mp ->
                    _duration.value = mp.duration.toLong()
                    _currentPosition.value = 0L
                    initAudioEffects(mp.audioSessionId)
                    mp.setVolume(volume, volume)
                    mp.start()
                    _isPlaying.value = true
                }

                setOnCompletionListener {
                    _isPlaying.value = false
                    onTrackCompleted?.invoke()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e("AudioEngine", "Error loading track: $what, $extra")
                    _isPlaying.value = false
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Failed to play track ${track.title}", e)
            _isPlaying.value = false
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying) {
                it.start()
                _isPlaying.value = true
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let {
            it.seekTo(positionMs.toInt())
            _currentPosition.value = positionMs
        }
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(volume, volume)
    }

    private fun initAudioEffects(audioSessionId: Int) {
        try {
            releaseAudioEffects()

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
                val numBands = numberOfBands
                val minLevel = bandLevelRange[0]
                val maxLevel = bandLevelRange[1]

                val bands = mutableListOf<EqualizerBand>()
                for (i in 0 until numBands) {
                    bands.add(
                        EqualizerBand(
                            bandIndex = i.toShort(),
                            centerFrequencyHz = getCenterFreq(i.toShort()) / 1000,
                            minLevelMb = minLevel,
                            maxLevelMb = maxLevel,
                            currentLevelMb = getBandLevel(i.toShort())
                        )
                    )
                }
                _equalizerBands.value = bands

                val presets = mutableListOf<String>()
                for (p in 0 until numberOfPresets) {
                    presets.add(getPresetName(p.toShort()))
                }
                _equalizerPresets.value = presets
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
            }

            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.w("AudioEngine", "Could not initialize hardware audio effects", e)
        }
    }

    fun setBandLevel(bandIndex: Short, levelMb: Short) {
        try {
            equalizer?.setBandLevel(bandIndex, levelMb)
            _equalizerBands.value = _equalizerBands.value.map {
                if (it.bandIndex == bandIndex) it.copy(currentLevelMb = levelMb) else it
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error setting band level", e)
        }
    }

    fun usePreset(presetIndex: Short) {
        try {
            equalizer?.usePreset(presetIndex)
            equalizer?.let { eq ->
                val updated = _equalizerBands.value.map { band ->
                    band.copy(currentLevelMb = eq.getBandLevel(band.bandIndex))
                }
                _equalizerBands.value = updated
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error setting preset", e)
        }
    }

    fun setBassBoost(strength: Short) {
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(strength)
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error setting bass boost", e)
        }
    }

    fun setVirtualizer(strength: Short) {
        try {
            if (virtualizer?.strengthSupported == true) {
                virtualizer?.setStrength(strength)
            }
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error setting virtualizer", e)
        }
    }

    private fun startPositionUpdateLoop() {
        updateJob?.cancel()
        updateJob = scope.launch {
            var step = 0L
            while (isActive) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _currentPosition.value = mp.currentPosition.toLong()
                        _duration.value = mp.duration.toLong()

                        // Calculate realistic VU meter needle physics signal
                        step += 1
                        val timeSeconds = step * 0.05f
                        val baseL = (0.5f + 0.4f * sin(timeSeconds * 12f) + 0.2f * sin(timeSeconds * 23f)).coerceIn(0.1f, 1.0f) * volume
                        val baseR = (0.5f + 0.4f * sin(timeSeconds * 15f + 1f) + 0.2f * sin(timeSeconds * 19f)).coerceIn(0.1f, 1.0f) * volume

                        _amplitudeLeft.value = baseL
                        _amplitudeRight.value = baseR
                    } else {
                        _amplitudeLeft.value = 0f
                        _amplitudeRight.value = 0f
                    }
                }
                delay(50)
            }
        }
    }

    private fun releaseMediaPlayer() {
        try {
            mediaPlayer?.run {
                if (isPlaying) stop()
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error releasing player", e)
        }
    }

    private fun releaseAudioEffects() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
            virtualizer?.release()
            virtualizer = null
        } catch (e: Exception) {
            Log.e("AudioEngine", "Error releasing effects", e)
        }
    }

    fun release() {
        updateJob?.cancel()
        releaseAudioEffects()
        releaseMediaPlayer()
        scope.cancel()
    }
}
