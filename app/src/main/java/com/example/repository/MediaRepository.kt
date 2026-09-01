package com.example.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.database.AppDatabase
import com.example.database.PlaylistEntity
import com.example.database.PlaylistTrackCrossRef
import com.example.database.TrackEntity
import com.example.model.Playlist
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MediaRepository(private val context: Context) {

    private val dao = AppDatabase.getInstance(context).playbackDao()
    private val sessionPrefs = context.getSharedPreferences("retroplayer_session", Context.MODE_PRIVATE)

    val allTracks: Flow<List<Track>> = dao.getAllTracks().map { list ->
        list.map { it.toDomainModel() }
    }

    val favoriteTracks: Flow<List<Track>> = dao.getFavoriteTracks().map { list ->
        list.map { it.toDomainModel() }
    }

    val allPlaylists: Flow<List<Playlist>> = dao.getAllPlaylists().map { list ->
        list.map { it.toDomainModel() }
    }

    /**
     * Persiste a última faixa tocada e a posição de reprodução — sem isso o
     * app sempre reabria na primeira faixa da biblioteca, ignorando o que
     * estava tocando antes de fechar (incluindo o serviço reiniciado pelo
     * widget de home screen).
     */
    fun saveLastPlaybackPosition(trackId: Long, positionMs: Long) {
        sessionPrefs.edit()
            .putLong("last_track_id", trackId)
            .putLong("last_position_ms", positionMs)
            .apply()
    }

    suspend fun getLastPlayedTrack(): Track? = withContext(Dispatchers.IO) {
        val lastId = sessionPrefs.getLong("last_track_id", -1L)
        if (lastId == -1L) return@withContext null
        dao.getTrackById(lastId)?.toDomainModel()
    }

    fun getLastPlaybackPositionMs(): Long = sessionPrefs.getLong("last_position_ms", 0L)

    suspend fun scanLocalMediaAndSeed() = withContext(Dispatchers.IO) {
        val scannedTracks = mutableListOf<TrackEntity>()

        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Faixa Desconhecida"
                    val artist = cursor.getString(artistCol) ?: "Artista Desconhecido"
                    val album = cursor.getString(albumCol) ?: "Álbum Desconhecido"
                    val duration = cursor.getLong(durCol)
                    val dataPath = cursor.getString(dataCol) ?: ""
                    val albumId = cursor.getLong(albumIdCol)

                    val albumArtUri = if (albumId > 0) {
                        ContentUris.withAppendedId(
                            android.net.Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    } else null

                    val folder = dataPath.substringBeforeLast('/', "Interno")

                    // Preserva estado do usuário (favorito, contagem de reprodução,
                    // letra salva) entre re-scans — sem isso, reabrir o app apagava
                    // os favoritos porque o MediaStore não conhece esses campos.
                    val existing = dao.getTrackById(id)

                    scannedTracks.add(
                        TrackEntity(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = if (duration > 0) duration else 210000L,
                            dataPath = dataPath,
                            albumArtUri = albumArtUri,
                            folderPath = folder,
                            isFavorite = existing?.isFavorite ?: false,
                            playCount = existing?.playCount ?: 0,
                            lyrics = existing?.lyrics,
                            dateAdded = existing?.dateAdded ?: System.currentTimeMillis(),
                            codec = when {
                                dataPath.endsWith(".flac", ignoreCase = true) -> "FLAC"
                                dataPath.endsWith(".aac", ignoreCase = true) -> "AAC"
                                dataPath.endsWith(".wav", ignoreCase = true) -> "WAV"
                                dataPath.endsWith(".m4a", ignoreCase = true) -> "M4A"
                                else -> "MP3"
                            },
                            bitrateKbps = if (dataPath.endsWith(".flac")) 960 else 320,
                            sampleRateHz = 44100,
                            bitsPerSample = 16
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Error querying MediaStore", e)
        }

        // Clean up any previously stored fake demo tracks
        dao.deleteDemoTracks()

        if (scannedTracks.isNotEmpty()) {
            dao.insertTracks(scannedTracks)
        } else {
            // No local audio files found on device -> ensure DB has no stale tracks
            dao.deleteAllTracks()
        }

        // Seed default playlists if empty
        val defaultPlaylists = listOf(
            PlaylistEntity(id = 1, name = "Favoritos Hi-Fi"),
            PlaylistEntity(id = 2, name = "Sessão Noturna Vinyl"),
            PlaylistEntity(id = 3, name = "Master Tape Audio")
        )
        for (pl in defaultPlaylists) {
            dao.insertPlaylist(pl)
        }

        // Importa playlists .m3u/.m3u8 criadas por outros players (Poweramp,
        // VLC, foobar2000, etc) encontradas nos diretórios padrão do device.
        importM3uPlaylists()
    }

    /**
     * Varre o armazenamento em busca de arquivos .m3u/.m3u8, faz o parse de
     * cada um, e resolve cada entrada contra as faixas já escaneadas via
     * MediaStore (por caminho exato primeiro, por nome de arquivo como
     * fallback — útil quando o M3U foi gerado em outro dispositivo/pasta).
     * Cria (ou reaproveita) uma Playlist no banco com o mesmo nome do
     * arquivo .m3u e popula com as faixas resolvidas.
     */
    suspend fun importM3uPlaylists() = withContext(Dispatchers.IO) {
        try {
            val m3uFiles = com.example.util.M3uParser.findM3uFiles()
            if (m3uFiles.isEmpty()) return@withContext

            for (m3uFile in m3uFiles) {
                val parsed = com.example.util.M3uParser.parse(m3uFile) ?: continue
                if (parsed.entries.isEmpty()) continue

                // Reaproveita a playlist se já existir uma com o mesmo nome
                // (evita duplicar a cada novo scan de mídia).
                val existing = dao.getPlaylistByName(parsed.name)
                val playlistId = existing?.id ?: dao.insertPlaylist(PlaylistEntity(name = parsed.name))

                for (entry in parsed.entries) {
                    val resolvedFile = com.example.util.M3uParser.resolveEntryFile(entry, m3uFile)
                    val exactMatch = dao.getTrackByPath(resolvedFile.absolutePath)

                    val matchedTrack = exactMatch ?: run {
                        val byName = dao.getTracksByFileName(resolvedFile.name)
                        byName.firstOrNull()
                    }

                    if (matchedTrack != null) {
                        dao.addTrackToPlaylist(
                            PlaylistTrackCrossRef(playlistId = playlistId, trackId = matchedTrack.id)
                        )
                    } else {
                        Log.w(
                            "MediaRepository",
                            "M3U '${parsed.name}': faixa não encontrada no dispositivo -> ${entry.rawPath}"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MediaRepository", "Erro ao importar playlists M3U", e)
        }
    }

    suspend fun toggleFavorite(trackId: Long, currentFav: Boolean) {
        dao.setFavorite(trackId, !currentFav)
    }

    suspend fun updateLyrics(trackId: Long, lyrics: String) {
        dao.updateLyrics(trackId, lyrics)
    }

    suspend fun createPlaylist(name: String) {
        dao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        dao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId = playlistId, trackId = trackId))
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<Track>> {
        return dao.getTracksForPlaylist(playlistId).map { list -> list.map { it.toDomainModel() } }
    }

    private fun TrackEntity.toDomainModel(): Track {
        return Track(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationMs = durationMs,
            dataPath = dataPath,
            albumArtUri = albumArtUri,
            isFavorite = isFavorite,
            playCount = playCount,
            dateAdded = dateAdded,
            codec = codec,
            bitrateKbps = bitrateKbps,
            sampleRateHz = sampleRateHz,
            bitsPerSample = bitsPerSample,
            lyrics = lyrics,
            folderPath = folderPath
        )
    }

    private fun PlaylistEntity.toDomainModel(): Playlist {
        return Playlist(
            id = id,
            name = name,
            createdAt = createdAt
        )
    }
}
