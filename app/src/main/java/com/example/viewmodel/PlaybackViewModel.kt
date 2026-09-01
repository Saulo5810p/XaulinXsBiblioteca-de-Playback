package com.example.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.EqualizerBand
import com.example.model.PlaybackState
import com.example.model.Playlist
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.playback.PlaybackService
import com.example.repository.MediaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlaybackViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MediaRepository(application)
    private var playbackService: PlaybackService? = null
    private var isServiceBound = false

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _allTracks = MutableStateFlow<List<Track>>(emptyList())
    val allTracks: StateFlow<List<Track>> = _allTracks.asStateFlow()

    private val _favoriteTracks = MutableStateFlow<List<Track>>(emptyList())
    val favoriteTracks: StateFlow<List<Track>> = _favoriteTracks.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _selectedPlaylistTracks = MutableStateFlow<List<Track>>(emptyList())
    val selectedPlaylistTracks: StateFlow<List<Track>> = _selectedPlaylistTracks.asStateFlow()

    private var playlistTracksJob: Job? = null

    fun openPlaylist(playlist: Playlist) {
        _selectedPlaylist.value = playlist
        playlistTracksJob?.cancel()
        playlistTracksJob = viewModelScope.launch {
            repository.getTracksForPlaylist(playlist.id).collectLatest { tracks ->
                _selectedPlaylistTracks.value = tracks
            }
        }
    }

    fun closePlaylist() {
        playlistTracksJob?.cancel()
        _selectedPlaylist.value = null
        _selectedPlaylistTracks.value = emptyList()
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _equalizerBands = MutableStateFlow<List<EqualizerBand>>(emptyList())
    val equalizerBands: StateFlow<List<EqualizerBand>> = _equalizerBands.asStateFlow()

    private val _equalizerPresets = MutableStateFlow<List<String>>(emptyList())
    val equalizerPresets: StateFlow<List<String>> = _equalizerPresets.asStateFlow()

    private var sleepTimerJob: Job? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlaybackService.LocalBinder
            val srv = binder.getService()
            playbackService = srv
            isServiceBound = true

            srv.onTogglePlayPause = { togglePlayPause() }
            srv.onNext = { playNextTrack() }
            srv.onPrevious = { playPreviousTrack() }

            setupAudioEngineObservers()
            updateServiceNotification()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playbackService = null
            isServiceBound = false
        }
    }

    init {
        bindPlaybackService()
        observeRepositoryData()
    }

    private fun bindPlaybackService() {
        val intent = Intent(getApplication(), PlaybackService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }
        getApplication<Application>().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun observeRepositoryData() {
        viewModelScope.launch {
            repository.scanLocalMediaAndSeed()
        }

        viewModelScope.launch {
            repository.allTracks.collectLatest { tracks ->
                _allTracks.value = tracks
                if (_playbackState.value.queue.isEmpty() && tracks.isNotEmpty()) {
                    val lastPlayed = repository.getLastPlayedTrack()
                    val resumedTrack = lastPlayed?.let { saved -> tracks.find { it.id == saved.id } } ?: tracks.first()
                    val resumedIndex = tracks.indexOf(resumedTrack).coerceAtLeast(0)
                    _playbackState.value = _playbackState.value.copy(
                        queue = tracks,
                        currentTrack = resumedTrack,
                        queueIndex = resumedIndex,
                        currentPositionMs = if (resumedTrack.id == lastPlayed?.id) repository.getLastPlaybackPositionMs() else 0L
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.favoriteTracks.collectLatest { favs ->
                _favoriteTracks.value = favs
            }
        }

        viewModelScope.launch {
            repository.allPlaylists.collectLatest { pls ->
                _playlists.value = pls
            }
        }
    }

    private fun setupAudioEngineObservers() {
        playbackService?.audioEngine?.let { engine ->
            engine.onTrackCompleted = {
                val current = _playbackState.value
                when (current.repeatMode) {
                    RepeatMode.ONE -> {
                        current.currentTrack?.let { playTrack(it) }
                    }
                    RepeatMode.ALL -> {
                        playNextTrack()
                    }
                    RepeatMode.OFF -> {
                        if (current.queueIndex < current.queue.size - 1) {
                            playNextTrack()
                        } else {
                            // End of queue: stop playback and reset position
                            playbackService?.audioEngine?.pause()
                            playbackService?.audioEngine?.seekTo(0)
                        }
                    }
                }
            }

            viewModelScope.launch {
                engine.isPlaying.collectLatest { playing ->
                    _playbackState.value = _playbackState.value.copy(isPlaying = playing)
                    updateServiceNotification()
                }
            }

            viewModelScope.launch {
                engine.currentPosition.collectLatest { pos ->
                    _playbackState.value = _playbackState.value.copy(currentPositionMs = pos)
                    _playbackState.value.currentTrack?.let { track ->
                        repository.saveLastPlaybackPosition(track.id, pos)
                    }
                }
            }

            viewModelScope.launch {
                engine.duration.collectLatest { dur ->
                    _playbackState.value = _playbackState.value.copy(totalDurationMs = dur)
                    updateServiceNotification()
                }
            }

            viewModelScope.launch {
                engine.amplitudeLeft.collectLatest { ampL ->
                    _playbackState.value = _playbackState.value.copy(audioAmplitudeLeft = ampL)
                }
            }

            viewModelScope.launch {
                engine.amplitudeRight.collectLatest { ampR ->
                    _playbackState.value = _playbackState.value.copy(audioAmplitudeRight = ampR)
                }
            }

            viewModelScope.launch {
                engine.equalizerBands.collectLatest { bands ->
                    _equalizerBands.value = bands
                }
            }

            viewModelScope.launch {
                engine.equalizerPresets.collectLatest { presets ->
                    _equalizerPresets.value = presets
                }
            }
        }
    }

    fun playTrack(track: Track, queueList: List<Track> = _allTracks.value) {
        val index = queueList.indexOfFirst { it.id == track.id }
        _playbackState.value = _playbackState.value.copy(
            currentTrack = track,
            currentPositionMs = 0L,
            totalDurationMs = if (track.durationMs > 0) track.durationMs else 0L,
            queue = queueList,
            queueIndex = if (index >= 0) index else 0
        )
        repository.saveLastPlaybackPosition(track.id, 0L)
        playbackService?.audioEngine?.playTrack(track)
        updateServiceNotification()
    }

    fun playFile(file: java.io.File) {
        var duration = 0L
        var title = file.nameWithoutExtension
        var artist = "Pasta Local"
        var album = file.parentFile?.name ?: "Memória Interna"

        try {
            val mmr = android.media.MediaMetadataRetriever()
            mmr.setDataSource(file.absolutePath)
            val durStr = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (!durStr.isNullOrEmpty()) {
                duration = durStr.toLongOrNull() ?: 0L
            }
            val metaTitle = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_TITLE)
            if (!metaTitle.isNullOrBlank()) title = metaTitle
            val metaArtist = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST)
            if (!metaArtist.isNullOrBlank()) artist = metaArtist
            val metaAlbum = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_ALBUM)
            if (!metaAlbum.isNullOrBlank()) album = metaAlbum
            mmr.release()
        } catch (e: Exception) {
            // Fallback to basic file info if metadata retrieval fails
        }

        val track = Track(
            id = file.hashCode().toLong(),
            title = title,
            artist = artist,
            album = album,
            durationMs = duration,
            dataPath = file.absolutePath,
            albumArtUri = null,
            folderPath = file.parentFile?.absolutePath ?: "",
            codec = if (file.extension.isNotBlank()) file.extension.uppercase() else "AUDIO"
        )
        playTrack(track, listOf(track))
    }

    fun togglePlayPause() {
        val state = _playbackState.value
        val engine = playbackService?.audioEngine ?: return

        if (state.isPlaying) {
            engine.pause()
        } else {
            if (state.currentTrack != null) {
                engine.resume()
            } else if (_allTracks.value.isNotEmpty()) {
                playTrack(_allTracks.value.first())
            }
        }
    }

    fun playNextTrack() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        var nextIndex = state.queueIndex + 1
        if (state.isShuffle) {
            nextIndex = (0 until state.queue.size).random()
        } else if (nextIndex >= state.queue.size) {
            nextIndex = 0
        }

        val nextTrack = state.queue[nextIndex]
        _playbackState.value = state.copy(currentTrack = nextTrack, queueIndex = nextIndex)
        playbackService?.audioEngine?.playTrack(nextTrack)
        updateServiceNotification()
    }

    fun playPreviousTrack() {
        val state = _playbackState.value
        if (state.queue.isEmpty()) return

        var prevIndex = state.queueIndex - 1
        if (prevIndex < 0) {
            prevIndex = state.queue.size - 1
        }

        val prevTrack = state.queue[prevIndex]
        _playbackState.value = state.copy(currentTrack = prevTrack, queueIndex = prevIndex)
        playbackService?.audioEngine?.playTrack(prevTrack)
        updateServiceNotification()
    }

    fun seekTo(positionMs: Long) {
        playbackService?.audioEngine?.seekTo(positionMs)
    }

    fun setVolume(volume: Float) {
        _playbackState.value = _playbackState.value.copy(volumeLevel = volume)
        playbackService?.audioEngine?.setVolume(volume)
    }

    fun toggleShuffle() {
        _playbackState.value = _playbackState.value.copy(isShuffle = !_playbackState.value.isShuffle)
    }

    fun toggleRepeatMode() {
        val nextMode = when (_playbackState.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _playbackState.value = _playbackState.value.copy(repeatMode = nextMode)
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id, track.isFavorite)
            val updatedTrack = track.copy(isFavorite = !track.isFavorite)
            if (_playbackState.value.currentTrack?.id == track.id) {
                _playbackState.value = _playbackState.value.copy(currentTrack = updatedTrack)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setEqualizerBandLevel(bandIndex: Short, levelMb: Short) {
        playbackService?.audioEngine?.setBandLevel(bandIndex, levelMb)
    }

    fun selectPreset(presetIndex: Short, presetName: String) {
        _playbackState.value = _playbackState.value.copy(selectedPresetName = presetName)
        playbackService?.audioEngine?.usePreset(presetIndex)
    }

    fun setBassBoost(strength: Short) {
        _playbackState.value = _playbackState.value.copy(bassBoostStrength = strength)
        playbackService?.audioEngine?.setBassBoost(strength)
    }

    fun setVirtualizer(strength: Short) {
        _playbackState.value = _playbackState.value.copy(virtualizerStrength = strength)
        playbackService?.audioEngine?.setVirtualizer(strength)
    }

    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _playbackState.value = _playbackState.value.copy(sleepTimerRemainingSec = 0)
            return
        }

        var remaining = minutes * 60
        _playbackState.value = _playbackState.value.copy(sleepTimerRemainingSec = remaining)

        sleepTimerJob = viewModelScope.launch {
            while (remaining > 0) {
                delay(1000)
                remaining -= 1
                _playbackState.value = _playbackState.value.copy(sleepTimerRemainingSec = remaining)
            }
            playbackService?.audioEngine?.pause()
            _playbackState.value = _playbackState.value.copy(sleepTimerRemainingSec = 0)
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun reimportM3uPlaylists() {
        viewModelScope.launch {
            repository.importM3uPlaylists()
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun saveLyrics(trackId: Long, lyrics: String) {
        viewModelScope.launch {
            repository.updateLyrics(trackId, lyrics)
            if (_playbackState.value.currentTrack?.id == trackId) {
                _playbackState.value = _playbackState.value.copy(
                    currentTrack = _playbackState.value.currentTrack?.copy(lyrics = lyrics)
                )
            }
        }
    }

    private fun updateServiceNotification() {
        val state = _playbackState.value
        val track = state.currentTrack
        val currentQueue = if (state.queue.isNotEmpty()) state.queue else _allTracks.value
        playbackService?.showNotification(
            track = track,
            isPlaying = state.isPlaying,
            queue = currentQueue
        )
    }

    override fun onCleared() {
        if (isServiceBound) {
            getApplication<Application>().unbindService(serviceConnection)
            isServiceBound = false
        }
        super.onCleared()
    }
}
