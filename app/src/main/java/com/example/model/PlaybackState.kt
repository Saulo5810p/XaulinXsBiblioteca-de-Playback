package com.example.model

enum class RepeatMode {
    OFF, ALL, ONE
}

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val volumeLevel: Float = 0.8f,
    val repeatMode: RepeatMode = RepeatMode.ALL,
    val isShuffle: Boolean = false,
    val audioAmplitudeLeft: Float = 0f,
    val audioAmplitudeRight: Float = 0f,
    val selectedPresetName: String = "Hi-Fi Studio",
    val bassBoostStrength: Short = 0,
    val virtualizerStrength: Short = 0,
    val sleepTimerRemainingSec: Int = 0,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = -1
)
