package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.components.AudioChannelTestDialog
import com.example.components.AudiophileListeningRoom
import com.example.components.CdBookletEncarteDialog
import com.example.components.CoverFlow3DCarousel
import com.example.components.HiFiLibraryStatsCard
import com.example.components.HiFiMasterReceiverHeader
import com.example.components.MasterControlUnitOverlay
import com.example.components.ReceiverRearPanelConfig
import com.example.components.StudioAlbumCard3D
import com.example.components.StudioRackCabinet
import com.example.components.StudioTapeDeck
import com.example.components.StudioTrackItemCard
import com.example.components.VintageRadioTuner
import com.example.model.PlaybackState
import com.example.model.Playlist
import com.example.model.Track
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.GlobalSpinBlurTransition
import com.example.ui.effects.ScrollCinematicProfile
import com.example.ui.effects.cinematicPressVisuals
import com.example.ui.effects.cinematicScrollItem
import com.example.ui.effects.globalCinematicClickable
import com.example.ui.theme.*
import com.example.ui.util.AlbumArtUtil
import com.example.ui.util.rememberDeviceTilt
import com.example.ui.util.rememberAlbumArtPainter

enum class ReceiverTab(val label: String) {
    COVER_FLOW_3D("COVER FLOW 3D"),
    SALA_ESCUTA("SALA DE ESCUTA"),
    ALBUMS_3D("ÁLBUNS ENORMES"),
    CABINETE("CABINETE HIFI"),
    ARTISTAS_TAPE("REEL-TO-REEL"),
    RADIO_VINTAGE("RÁDIO VINTAGE"),
    BIBLIOTECA("BIBLIOTECA"),
    PLAYLISTS("PLAYLISTS"),
    FAVORITOS("FAVORITOS"),
    GERENCIADOR("FILE MANAGER"),
    PESQUISA("PESQUISA")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPanelScreen(
    state: PlaybackState,
    allTracks: List<Track>,
    favoriteTracks: List<Track>,
    playlists: List<Playlist>,
    selectedPlaylist: Playlist? = null,
    selectedPlaylistTracks: List<Track> = emptyList(),
    onOpenPlaylist: (Playlist) -> Unit = {},
    onClosePlaylist: () -> Unit = {},
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onTrackSelected: (Track) -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit = {},
    onSeekTo: (Long) -> Unit = {},
    onOpenNowPlaying: () -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onOpenEqualizer: () -> Unit,
    onPlayFile: (java.io.File) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ReceiverTab.COVER_FLOW_3D) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var showControlOverlay by remember { mutableStateOf(false) }
    var showRearPanelDialog by remember { mutableStateOf(false) }
    var showAudioTestDialog by remember { mutableStateOf(false) }
    var selectedEncarteAlbum by remember { mutableStateOf<String?>(null) }
    var themeOverrideColor by remember { mutableStateOf<Color?>(null) }
    var newPlaylistName by remember { mutableStateOf("") }

    val haptic = LocalHapticFeedback.current
    val (tiltX, tiltY) = rememberDeviceTilt().value

    // Easter egg query triggers (TECHNICS, VINYL, AMBER, VALVULA)
    val searchUpper = searchQuery.uppercase().trim()
    val easterEggColor = when {
        searchUpper.contains("TECHNICS") -> Color(0xFF00E5FF)
        searchUpper.contains("VINYL") -> BrassGold
        searchUpper.contains("AMBER") -> VintageAmber
        searchUpper.contains("VALVULA") -> Color(0xFFFF6D00)
        else -> themeOverrideColor
    }

    val activeStudioColor = easterEggColor ?: AlbumArtUtil.getTrackStudioColor(state.currentTrack)
    val animatedStudioAccent by animateColorAsState(
        targetValue = activeStudioColor,
        animationSpec = tween(1000),
        label = "studioColor"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MetalDarkBackground,
        bottomBar = {
            if (state.currentTrack != null) {
                MiniPlayerModule(
                    state = state,
                    onOpenNowPlaying = onOpenNowPlaying,
                    onTogglePlayPause = onTogglePlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeekTo = onSeekTo
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            animatedStudioAccent.copy(alpha = 0.15f),
                            MetalDarkBackground,
                            Color(0xFF07080A)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Hi-Fi Master Receiver Front Header
                HiFiMasterReceiverHeader(
                    state = state,
                    selectedTab = selectedTab,
                    trackCount = allTracks.size,
                    accentColor = animatedStudioAccent,
                    tiltX = tiltX,
                    onOpenEqualizer = onOpenEqualizer,
                    onOpenControlUnit = { showControlOverlay = true },
                    modifier = Modifier.fillMaxWidth()
                )

                // Studio Section Mode Selectors Bar
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(ReceiverTab.values()) { tab ->
                        val isSelected = tab == selectedTab
                        Box(
                            modifier = Modifier
                                .globalCinematicClickable(
                                    profile = com.example.ui.effects.CinematicProfile.TAB_TRANSITION,
                                    onClick = {
                                        selectedTab = tab
                                    }
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) animatedStudioAccent.copy(alpha = 0.25f) else MetalCardSurface)
                                .border(
                                    1.5.dp,
                                    if (isSelected) animatedStudioAccent else MetallicBorder,
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) animatedStudioAccent else MetalBevelDark)
                                )
                                Text(
                                    text = tab.label,
                                    color = if (isSelected) animatedStudioAccent else TextMetallicLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                // Search Bar Field
                AnimatedVisibility(visible = selectedTab == ReceiverTab.PESQUISA) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChanged,
                        placeholder = { Text("Pesquisar título, artista, álbum...", color = TextMetallicMuted) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = animatedStudioAccent) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = animatedStudioAccent,
                            unfocusedBorderColor = MetallicBorder,
                            focusedTextColor = TextMetallicLight,
                            unfocusedTextColor = TextMetallicLight,
                            focusedContainerColor = MetalCardSurface,
                            unfocusedContainerColor = MetalCardSurface
                        ),
                        singleLine = true
                    )
                }

                // Filtered Tracks
                val displayedTracks = when (selectedTab) {
                    ReceiverTab.BIBLIOTECA -> allTracks
                    ReceiverTab.FAVORITOS -> favoriteTracks
                    ReceiverTab.PESQUISA -> {
                        if (searchQuery.isBlank()) allTracks else allTracks.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                                    it.artist.contains(searchQuery, ignoreCase = true) ||
                                    it.album.contains(searchQuery, ignoreCase = true)
                        }
                    }
                    else -> allTracks
                }

                // Main Studio View Switcher - Occupying Full Space with Rack Module Slide Animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    GlobalSpinBlurTransition(
                        targetState = selectedTab,
                        modifier = Modifier.fillMaxSize()
                    ) { tab ->
                        when (tab) {
                            ReceiverTab.SALA_ESCUTA -> {
                                AudiophileListeningRoom(
                                    state = state,
                                    onTogglePlayPause = onTogglePlayPause,
                                    onNext = onNext,
                                    onPrevious = onPrevious,
                                    onClose = { selectedTab = ReceiverTab.COVER_FLOW_3D },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            ReceiverTab.RADIO_VINTAGE -> {
                                VintageRadioTuner(
                                    allTracks = allTracks,
                                    onPlayTrack = onTrackSelected,
                                    accentColor = animatedStudioAccent,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            ReceiverTab.COVER_FLOW_3D -> {
                                CoverFlow3DCarousel(
                                    tracks = allTracks,
                                    currentTrack = state.currentTrack,
                                    isPlaying = state.isPlaying,
                                    onTrackSelected = onTrackSelected,
                                    onOpenNowPlaying = onOpenNowPlaying,
                                    onTogglePlayPause = onTogglePlayPause,
                                    onToggleFavorite = onToggleFavorite,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            ReceiverTab.ALBUMS_3D -> {
                                if (allTracks.isEmpty()) {
                                    EmptyMusicBox()
                                } else {
                                    val groupedByAlbum = allTracks.groupBy { it.album }
                                    val albumKeys = groupedByAlbum.keys.toList()
                                    val albumsListState = rememberLazyListState()
                                    LazyColumn(
                                        state = albumsListState,
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        itemsIndexed(albumKeys) { index, albumName ->
                                            val tracksForAlbum = groupedByAlbum[albumName] ?: emptyList()
                                            StudioAlbumCard3D(
                                                modifier = Modifier.cinematicScrollItem(
                                                    lazyListState = albumsListState,
                                                    index = index,
                                                    profile = ScrollCinematicProfile.INSANE
                                                ),
                                                albumName = albumName,
                                                tracksInAlbum = tracksForAlbum,
                                                currentTrack = state.currentTrack,
                                                isPlaying = state.isPlaying,
                                                onTrackSelected = onTrackSelected,
                                                onTogglePlayPause = onTogglePlayPause,
                                                onToggleFavorite = onToggleFavorite,
                                                onOpenEncarte = { selectedEncarteAlbum = albumName }
                                            )
                                        }
                                    }
                                }
                            }

                            ReceiverTab.CABINETE -> {
                                StudioRackCabinet(
                                    allTracks = allTracks,
                                    currentTrack = state.currentTrack,
                                    isPlaying = state.isPlaying,
                                    onTrackSelected = onTrackSelected,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            ReceiverTab.ARTISTAS_TAPE -> {
                                StudioTapeDeck(
                                    allTracks = allTracks,
                                    currentTrack = state.currentTrack,
                                    isPlaying = state.isPlaying,
                                    onTrackSelected = onTrackSelected,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            ReceiverTab.GERENCIADOR -> {
                                FileManagerModule(
                                    onPlayTrackFromFile = onPlayFile,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            ReceiverTab.PLAYLISTS -> {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "PLAYLISTS DO ESTÚDIO",
                                            color = TextMetallicLight,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        val newPlaylistInteraction = remember { MutableInteractionSource() }
                                        Button(
                                            onClick = { showCreatePlaylistDialog = true },
                                            modifier = Modifier.cinematicPressVisuals(
                                                interactionSource = newPlaylistInteraction,
                                                profile = com.example.ui.effects.CinematicProfile.MEDIA_CONTROL
                                            ),
                                            interactionSource = newPlaylistInteraction,
                                            colors = ButtonDefaults.buttonColors(containerColor = animatedStudioAccent, contentColor = MetalDarkBackground),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("NOVA PLAYLIST", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    val playlistsListState = rememberLazyListState()
                                    LazyColumn(
                                        state = playlistsListState,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        itemsIndexed(playlists) { index, pl ->
                                            Box(
                                                modifier = Modifier
                                                    .cinematicScrollItem(
                                                        lazyListState = playlistsListState,
                                                        index = index,
                                                        profile = ScrollCinematicProfile.INSANE
                                                    )
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(MetalCardSurface)
                                                    .border(1.dp, MetallicBorder, RoundedCornerShape(6.dp))
                                                    .globalCinematicClickable(
                                                        profile = com.example.ui.effects.CinematicProfile.LIST_ITEM,
                                                        onClick = { onOpenPlaylist(pl) }
                                                    )
                                                    .padding(12.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Icon(Icons.Default.QueueMusic, contentDescription = null, tint = animatedStudioAccent)
                                                    Column {
                                                        Text(pl.name, color = TextMetallicLight, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                        Text("Sessão Personalizada do Estúdio", color = TextMetallicMuted, fontSize = 12.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                // Standard List View (Biblioteca / Favoritos / Pesquisa)
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (selectedTab == ReceiverTab.BIBLIOTECA && allTracks.isNotEmpty()) {
                                        HiFiLibraryStatsCard(
                                            allTracks = allTracks,
                                            accentColor = animatedStudioAccent,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    if (displayedTracks.isEmpty()) {
                                        val emptyTitle = when (selectedTab) {
                                            ReceiverTab.FAVORITOS -> "Sem Músicas Favoritas"
                                            ReceiverTab.PESQUISA -> "Nenhuma Música Encontrada"
                                            else -> "Sem Músicas"
                                        }
                                        val emptySub = when (selectedTab) {
                                            ReceiverTab.FAVORITOS -> "Você ainda não favoritou nenhuma faixa."
                                            ReceiverTab.PESQUISA -> "Nenhum resultado para \"$searchQuery\"."
                                            else -> "Nenhuma música local baixada no dispositivo."
                                        }
                                        EmptyMusicBox(message = emptyTitle, subMessage = emptySub)
                                    } else {
                                        val tracksListState = rememberLazyListState()
                                        LazyColumn(
                                            state = tracksListState,
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            itemsIndexed(displayedTracks) { index, track ->
                                                val isCurrentTrack = state.currentTrack?.id == track.id
                                                val trackStudioColor = AlbumArtUtil.getTrackStudioColor(track)

                                                StudioTrackItemCard(
                                                    modifier = Modifier.cinematicScrollItem(
                                                        lazyListState = tracksListState,
                                                        index = index,
                                                        profile = ScrollCinematicProfile.INSANE
                                                    ),
                                                    track = track,
                                                    trackIndex = index,
                                                    isCurrentTrack = isCurrentTrack,
                                                    isPlaying = state.isPlaying,
                                                    trackStudioColor = trackStudioColor,
                                                    onTrackSelected = onTrackSelected,
                                                    onToggleFavorite = onToggleFavorite
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Master Control Unit Overlay Dialog
    if (showControlOverlay) {
        MasterControlUnitOverlay(
            onDismiss = { showControlOverlay = false },
            onOpenEqualizer = onOpenEqualizer,
            accentColor = animatedStudioAccent,
            onOpenAudioTest = { showAudioTestDialog = true },
            onOpenRearPanel = { showRearPanelDialog = true }
        )
    }

    // Rear Panel Receiver Configuration Dialog
    if (showRearPanelDialog) {
        ReceiverRearPanelConfig(
            accentColor = animatedStudioAccent,
            onSelectThemePreset = { preset ->
                themeOverrideColor = when (preset) {
                    "TECHNICS BLUE" -> Color(0xFF00E5FF)
                    "EMERALD GREEN" -> Color(0xFF00E676)
                    else -> VintageAmber
                }
            },
            onDismiss = { showRearPanelDialog = false }
        )
    }

    // Audio Channel Test Generator Dialog
    if (showAudioTestDialog) {
        AudioChannelTestDialog(
            accentColor = animatedStudioAccent,
            onDismiss = { showAudioTestDialog = false }
        )
    }

    // Physical CD Booklet / Encarte Dialog
    selectedEncarteAlbum?.let { album ->
        val tracksForAlbum = allTracks.filter { it.album == album }
        CdBookletEncarteDialog(
            albumName = album,
            tracksInAlbum = tracksForAlbum,
            currentTrack = state.currentTrack,
            isPlaying = state.isPlaying,
            onTrackSelected = onTrackSelected,
            onDismiss = { selectedEncarteAlbum = null }
        )
    }

    // Create Playlist Dialog Modal
    if (showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Criar Nova Playlist no Estúdio", color = TextMetallicLight, fontFamily = FontFamily.Monospace) },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Nome da Playlist") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = animatedStudioAccent,
                        unfocusedBorderColor = MetallicBorder,
                        focusedTextColor = TextMetallicLight,
                        unfocusedTextColor = TextMetallicLight
                    )
                )
            },
            confirmButton = {
                val savePlaylistInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onCreatePlaylist(newPlaylistName)
                            newPlaylistName = ""
                            showCreatePlaylistDialog = false
                        }
                    },
                    modifier = Modifier.cinematicPressVisuals(
                        interactionSource = savePlaylistInteraction,
                        profile = com.example.ui.effects.CinematicProfile.MEDIA_CONTROL
                    ),
                    interactionSource = savePlaylistInteraction,
                    colors = ButtonDefaults.buttonColors(containerColor = animatedStudioAccent, contentColor = MetalDarkBackground)
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                val cancelPlaylistInteraction = remember { MutableInteractionSource() }
                TextButton(
                    onClick = { showCreatePlaylistDialog = false },
                    modifier = Modifier.cinematicPressVisuals(
                        interactionSource = cancelPlaylistInteraction,
                        profile = com.example.ui.effects.CinematicProfile.LIST_ITEM
                    ),
                    interactionSource = cancelPlaylistInteraction
                ) {
                    Text("Cancelar", color = TextMetallicMuted)
                }
            },
            containerColor = MetalPanelSurface
        )
    }

    // Detalhe de Playlist — abre ao clicar em um card na aba Playlists
    if (selectedPlaylist != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = onClosePlaylist,
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MetalDarkBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = selectedPlaylist.name,
                            color = TextMetallicLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${selectedPlaylistTracks.size} faixa(s)",
                            color = TextMetallicMuted,
                            fontSize = 13.sp
                        )
                    }
                    com.example.ui.effects.SpinBlurIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Fechar",
                        profile = com.example.ui.effects.CinematicProfile.HERO_TRANSITION,
                        tint = TextMetallicMuted,
                        onClick = onClosePlaylist
                    )
                }

                if (selectedPlaylistTracks.isEmpty()) {
                    Box(modifier = Modifier.weight(1f)) {
                        EmptyMusicBox(
                            message = "Playlist vazia",
                            subMessage = "Adicione faixas a esta playlist pela biblioteca"
                        )
                    }
                } else {
                    val playlistDetailListState = rememberLazyListState()
                    LazyColumn(
                        state = playlistDetailListState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(selectedPlaylistTracks) { index, track ->
                            val isCurrentTrack = state.currentTrack?.id == track.id
                            val trackStudioColor = AlbumArtUtil.getTrackStudioColor(track)

                            StudioTrackItemCard(
                                modifier = Modifier.cinematicScrollItem(
                                    lazyListState = playlistDetailListState,
                                    index = index,
                                    profile = ScrollCinematicProfile.INSANE
                                ),
                                track = track,
                                trackIndex = index,
                                isCurrentTrack = isCurrentTrack,
                                isPlaying = state.isPlaying,
                                trackStudioColor = trackStudioColor,
                                onTrackSelected = {
                                    onTrackSelected(it)
                                    onClosePlaylist()
                                    onOpenNowPlaying()
                                },
                                onToggleFavorite = onToggleFavorite
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMusicBox(
    message: String = "Sem Músicas",
    subMessage: String = "Nenhuma música local encontrada no dispositivo."
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .background(MetalCardSurface)
                .border(2.dp, MetallicBorder, RoundedCornerShape(12.dp))
                .padding(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MetalPanelSurface)
                    .border(1.dp, MetallicBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = VintageAmber,
                    modifier = Modifier.size(44.dp)
                )
            }
            Text(
                text = message,
                color = TextMetallicLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subMessage,
                color = TextMetallicMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

