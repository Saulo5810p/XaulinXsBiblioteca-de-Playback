package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val dataPath: String,
    val albumArtUri: String?,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val dateAdded: Long = System.currentTimeMillis(),
    val codec: String = "FLAC",
    val bitrateKbps: Int = 320,
    val sampleRateHz: Int = 44100,
    val bitsPerSample: Int = 16,
    val lyrics: String? = null,
    val folderPath: String = ""
)
