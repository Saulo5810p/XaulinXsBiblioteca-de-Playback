package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainPanelScreen
import com.example.ui.NowPlayingScreen
import com.example.ui.SplashScreen
import com.example.ui.dialogs.EqualizerDialog
import com.example.ui.dialogs.LyricsDialog
import com.example.ui.dialogs.SleepTimerDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PlaybackViewModel

import androidx.compose.runtime.saveable.rememberSaveable

enum class ScreenState {
    SPLASH,
    MAIN,
    NOW_PLAYING
}

class MainActivity : ComponentActivity() {

    private val viewModel: PlaybackViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkPermissions()

        setContent {
            MyApplicationTheme {
                val initialScreen = if (hasSplashBeenShown || savedInstanceState != null) ScreenState.MAIN else ScreenState.SPLASH
                var currentScreen by rememberSaveable { mutableStateOf(initialScreen) }

                var showEqualizerDialog by remember { mutableStateOf(false) }
                var showSleepTimerDialog by remember { mutableStateOf(false) }
                var showLyricsDialog by remember { mutableStateOf(false) }

                val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
                val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
                val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
                val playlists by viewModel.playlists.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

                val equalizerBands by viewModel.equalizerBands.collectAsStateWithLifecycle()
                val equalizerPresets by viewModel.equalizerPresets.collectAsStateWithLifecycle()

                com.example.ui.effects.GlobalCinematicScreenTransition(
                    targetState = currentScreen,
                    modifier = Modifier.fillMaxSize()
                ) { screen ->
                    when (screen) {
                        ScreenState.SPLASH -> {
                            SplashScreen(
                                onSplashFinished = {
                                    hasSplashBeenShown = true
                                    currentScreen = ScreenState.MAIN
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        ScreenState.MAIN -> {
                            val selectedPlaylist by viewModel.selectedPlaylist.collectAsStateWithLifecycle()
                            val selectedPlaylistTracks by viewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()
                            MainPanelScreen(
                                state = playbackState,
                                allTracks = allTracks,
                                favoriteTracks = favoriteTracks,
                                playlists = playlists,
                                selectedPlaylist = selectedPlaylist,
                                selectedPlaylistTracks = selectedPlaylistTracks,
                                onOpenPlaylist = { viewModel.openPlaylist(it) },
                                onClosePlaylist = { viewModel.closePlaylist() },
                                searchQuery = searchQuery,
                                onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                                onTrackSelected = { track ->
                                    viewModel.playTrack(track)
                                    currentScreen = ScreenState.NOW_PLAYING
                                },
                                onToggleFavorite = { viewModel.toggleFavorite(it) },
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.playNextTrack() },
                                onPrevious = { viewModel.playPreviousTrack() },
                                onSeekTo = { viewModel.seekTo(it) },
                                onOpenNowPlaying = { currentScreen = ScreenState.NOW_PLAYING },
                                onCreatePlaylist = { viewModel.createPlaylist(it) },
                                onOpenEqualizer = { showEqualizerDialog = true },
                                onPlayFile = { file ->
                                    viewModel.playFile(file)
                                    currentScreen = ScreenState.NOW_PLAYING
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        ScreenState.NOW_PLAYING -> {
                            NowPlayingScreen(
                                state = playbackState,
                                onBackClick = { currentScreen = ScreenState.MAIN },
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.playNextTrack() },
                                onPrevious = { viewModel.playPreviousTrack() },
                                onSeekTo = { viewModel.seekTo(it) },
                                onVolumeChange = { viewModel.setVolume(it) },
                                onToggleFavorite = {
                                    playbackState.currentTrack?.let { viewModel.toggleFavorite(it) }
                                },
                                onToggleRepeat = { viewModel.toggleRepeatMode() },
                                onToggleShuffle = { viewModel.toggleShuffle() },
                                onOpenEqualizer = { showEqualizerDialog = true },
                                onOpenSleepTimer = { showSleepTimerDialog = true },
                                onOpenLyrics = { showLyricsDialog = true },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // Modal Dialog Overlays
                if (showEqualizerDialog) {
                    EqualizerDialog(
                        bands = equalizerBands,
                        presets = equalizerPresets,
                        selectedPresetName = playbackState.selectedPresetName,
                        bassBoost = playbackState.bassBoostStrength,
                        virtualizer = playbackState.virtualizerStrength,
                        onBandLevelChanged = { bandIndex, levelMb -> viewModel.setEqualizerBandLevel(bandIndex, levelMb) },
                        onPresetSelected = { presetIndex, name -> viewModel.selectPreset(presetIndex, name) },
                        onBassBoostChanged = { viewModel.setBassBoost(it) },
                        onVirtualizerChanged = { viewModel.setVirtualizer(it) },
                        onDismiss = { showEqualizerDialog = false }
                    )
                }

                if (showSleepTimerDialog) {
                    SleepTimerDialog(
                        remainingSeconds = playbackState.sleepTimerRemainingSec,
                        onSetTimer = { viewModel.startSleepTimer(it) },
                        onDismiss = { showSleepTimerDialog = false }
                    )
                }

                if (showLyricsDialog && playbackState.currentTrack != null) {
                    LyricsDialog(
                        track = playbackState.currentTrack!!,
                        onSaveLyrics = { lyrics -> viewModel.saveLyrics(playbackState.currentTrack!!.id, lyrics) },
                        onDismiss = { showLyricsDialog = false }
                    )
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    companion object {
        var hasSplashBeenShown = false
    }
}
