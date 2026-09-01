package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.effects.ScrollCinematicProfile
import com.example.ui.effects.cinematicPressVisuals
import com.example.ui.effects.cinematicScrollItem
import com.example.ui.effects.CinematicProfile
import com.example.ui.effects.SpinBlurIconButton
import com.example.ui.effects.globalCinematicClickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Track
import com.example.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerModule(
    onPlayTrackFromFile: (File) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasFullStoragePermission by remember { mutableStateOf(checkStoragePermission(context)) }
    var currentDir by remember { mutableStateOf(Environment.getExternalStorageDirectory() ?: File("/storage/emulated/0")) }

    var selectedFileForMove by remember { mutableStateOf<File?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    // Re-check permission when resuming
    LaunchedEffect(Unit) {
        hasFullStoragePermission = checkStoragePermission(context)
    }

    if (!hasFullStoragePermission) {
        PermissionRequiredBox(
            onRequestPermission = {
                requestFullStoragePermission(context)
            }
        )
        return
    }

    val dirContents = remember(currentDir, hasFullStoragePermission) {
        try {
            val files = currentDir.listFiles() ?: emptyArray()
            files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        } catch (e: Exception) {
            emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Path Header Plate
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MetalPanelSurface,
                            Color(0xFF1B1E26),
                            Color(0xFF111318)
                        )
                    )
                )
                .border(1.5.dp, MetallicBorder, RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = VintageAmber)
                        Text(
                            text = "GERENCIADOR DE ARQUIVOS DE ÁUDIO",
                            color = TextMetallicLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SpinBlurIconButton(
                            icon = Icons.Default.CreateNewFolder,
                            contentDescription = "Nova Pasta",
                            profile = CinematicProfile.MEDIA_CONTROL,
                            tint = BrassGold,
                            iconSize = 18.dp,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MetalCardSurface)
                                .border(1.dp, MetallicBorder, RoundedCornerShape(4.dp)),
                            onClick = { showCreateFolderDialog = true }
                        )
                    }
                }

                // Breadcrumb path display
                Text(
                    text = currentDir.absolutePath,
                    color = TextLcdGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF090D0B))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Back / Parent Dir Action Button
        if (currentDir.parentFile != null && currentDir.absolutePath != "/storage/emulated/0" && currentDir.absolutePath != "/") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MetalCardSurface)
                    .border(1.dp, MetallicBorder, RoundedCornerShape(6.dp))
                    .clickable {
                        currentDir.parentFile?.let { currentDir = it }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Subir nível", tint = VintageAmber)
                    Text(
                        text = ".. (VOLTAR PARA PASTA ANTERIOR)",
                        color = TextMetallicLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Directory Contents List
        if (dirContents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MetalCardSurface)
                    .border(1.dp, MetallicBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pasta vazia ou sem permissão de leitura.",
                    color = TextMetallicMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            val filesListState = rememberLazyListState()
            LazyColumn(
                state = filesListState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(dirContents) { index, file ->
                    val isAudio = isAudioFile(file)
                    val isDirectory = file.isDirectory

                    Box(
                        modifier = Modifier
                            .cinematicScrollItem(
                                lazyListState = filesListState,
                                index = index,
                                profile = ScrollCinematicProfile.INSANE
                            )
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDirectory) MetalCardSurface else MetalPanelSurface)
                            .border(1.dp, if (isAudio) VintageAmber.copy(alpha = 0.5f) else MetallicBorder, RoundedCornerShape(6.dp))
                            .clickable {
                                if (isDirectory) {
                                    currentDir = file
                                } else {
                                    onPlayTrackFromFile(file)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = when {
                                        isDirectory -> Icons.Default.Folder
                                        isAudio -> Icons.Default.AudioFile
                                        else -> Icons.Default.InsertDriveFile
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isDirectory -> BrassGold
                                        isAudio -> FluorescentGreen
                                        else -> TextMetallicMuted
                                    }
                                )

                                Column {
                                    Text(
                                        text = file.name,
                                        color = if (isAudio) FluorescentGreen else TextMetallicLight,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isDirectory) {
                                            val count = file.listFiles()?.size ?: 0
                                            "$count itens"
                                        } else {
                                            formatFileSize(file.length())
                                        },
                                        color = TextMetallicMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (isAudio) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    SpinBlurIconButton(
                                        icon = Icons.Default.DriveFileMove,
                                        contentDescription = "Mover",
                                        profile = CinematicProfile.LIST_ITEM,
                                        tint = VintageAmber,
                                        iconSize = 20.dp,
                                        modifier = Modifier.size(32.dp),
                                        onClick = { selectedFileForMove = file }
                                    )
                                    SpinBlurIconButton(
                                        icon = Icons.Default.PlayArrow,
                                        contentDescription = "Tocar",
                                        profile = CinematicProfile.MEDIA_CONTROL,
                                        tint = FluorescentGreen,
                                        iconSize = 22.dp,
                                        modifier = Modifier.size(32.dp),
                                        onClick = { onPlayTrackFromFile(file) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Move File Dialog
    selectedFileForMove?.let { fileToMove ->
        MoveFileDialog(
            sourceFile = fileToMove,
            currentDir = currentDir,
            onDismiss = { selectedFileForMove = null },
            onMoveConfirmed = { targetFolder ->
                val destFile = File(targetFolder, fileToMove.name)
                val success = fileToMove.renameTo(destFile)
                if (success) {
                    Toast.makeText(context, "Arquivo movido para ${targetFolder.name}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Falha ao mover arquivo", Toast.LENGTH_SHORT).show()
                }
                selectedFileForMove = null
            }
        )
    }

    // Create New Folder Dialog
    if (showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { showCreateFolderDialog = false },
            title = { Text("Criar Nova Pasta", color = TextMetallicLight, fontFamily = FontFamily.Monospace) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Nome da Pasta") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VintageAmber,
                        unfocusedBorderColor = MetallicBorder,
                        focusedTextColor = TextMetallicLight,
                        unfocusedTextColor = TextMetallicLight
                    )
                )
            },
            confirmButton = {
                val createFolderInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            val newDir = File(currentDir, newFolderName)
                            if (!newDir.exists()) {
                                newDir.mkdirs()
                                Toast.makeText(context, "Pasta criada com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                            newFolderName = ""
                            showCreateFolderDialog = false
                        }
                    },
                    modifier = Modifier.cinematicPressVisuals(
                        interactionSource = createFolderInteraction,
                        profile = CinematicProfile.MEDIA_CONTROL
                    ),
                    interactionSource = createFolderInteraction,
                    colors = ButtonDefaults.buttonColors(containerColor = VintageAmber, contentColor = MetalDarkBackground)
                ) {
                    Text("Criar")
                }
            },
            dismissButton = {
                val cancelFolderInteraction = remember { MutableInteractionSource() }
                TextButton(
                    onClick = { showCreateFolderDialog = false },
                    modifier = Modifier.cinematicPressVisuals(
                        interactionSource = cancelFolderInteraction,
                        profile = CinematicProfile.LIST_ITEM
                    ),
                    interactionSource = cancelFolderInteraction
                ) {
                    Text("Cancelar", color = TextMetallicMuted)
                }
            },
            containerColor = MetalPanelSurface
        )
    }
}

@Composable
fun PermissionRequiredBox(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .background(MetalCardSurface)
                .border(2.dp, VintageAmber, RoundedCornerShape(12.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FolderSpecial,
                contentDescription = null,
                tint = VintageAmber,
                modifier = Modifier.size(56.dp)
            )

            Text(
                text = "PERMISSÃO DE ACESSO A TODOS OS ARQUIVOS",
                color = TextMetallicLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Text(
                text = "Para usar o Gerenciador de Arquivos do Estúdio de Playback, navegar pelas pastas originais do dispositivo e mover faixas, autorize o acesso total aos arquivos.",
                color = TextMetallicMuted,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            val permissionInteraction = remember { MutableInteractionSource() }
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.cinematicPressVisuals(
                    interactionSource = permissionInteraction,
                    profile = CinematicProfile.HERO_TRANSITION
                ),
                interactionSource = permissionInteraction,
                colors = ButtonDefaults.buttonColors(containerColor = VintageAmber, contentColor = MetalDarkBackground),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("AUTORIZAR ACESSO AGORA", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun MoveFileDialog(
    sourceFile: File,
    currentDir: File,
    onDismiss: () -> Unit,
    onMoveConfirmed: (File) -> Unit
) {
    var selectedTargetFolder by remember { mutableStateOf(currentDir) }
    val availableFolders = remember(selectedTargetFolder) {
        val root = Environment.getExternalStorageDirectory() ?: File("/storage/emulated/0")
        val subDirs = selectedTargetFolder.listFiles()?.filter { it.isDirectory } ?: emptyList()
        listOfNotNull(if (selectedTargetFolder.parentFile != null && selectedTargetFolder != root) selectedTargetFolder.parentFile else null) + subDirs
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mover Arquivo: ${sourceFile.name}", color = TextMetallicLight, fontSize = 14.sp, fontFamily = FontFamily.Monospace) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Destino atual: ${selectedTargetFolder.absolutePath}",
                    color = TextLcdGreen,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                LazyColumn(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MetalDarkBackground)
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableFolders) { folder ->
                        val isParent = folder == selectedTargetFolder.parentFile
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(MetalPanelSurface)
                                .clickable { selectedTargetFolder = folder }
                                .padding(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isParent) Icons.Default.ArrowUpward else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = BrassGold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isParent) ".. (SUBIR PARA ${folder.name})" else folder.name,
                                    color = TextMetallicLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val moveInteraction = remember { MutableInteractionSource() }
            Button(
                onClick = { onMoveConfirmed(selectedTargetFolder) },
                modifier = Modifier.cinematicPressVisuals(
                    interactionSource = moveInteraction,
                    profile = CinematicProfile.MEDIA_CONTROL
                ),
                interactionSource = moveInteraction,
                colors = ButtonDefaults.buttonColors(containerColor = FluorescentGreen, contentColor = MetalDarkBackground)
            ) {
                Text("MOVER AQUI", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            val cancelMoveInteraction = remember { MutableInteractionSource() }
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.cinematicPressVisuals(
                    interactionSource = cancelMoveInteraction,
                    profile = CinematicProfile.LIST_ITEM
                ),
                interactionSource = cancelMoveInteraction
            ) {
                Text("Cancelar", color = TextMetallicMuted)
            }
        },
        containerColor = MetalPanelSurface
    )
}

private fun isAudioFile(file: File): Boolean {
    if (!file.isFile) return false
    val ext = file.extension.lowercase()
    
    val audioExtensions = setOf(
        "mp3", "flac", "wav", "m4a", "aac", "ogg", "oga", "wma", "opus", "3gp", "3gpp",
        "amr", "mp4", "m4r", "m4b", "aiff", "aif", "alac", "webm", "mid", "midi", "ape",
        "wv", "m4p", "ts", "mp2", "mp1", "ac3", "dts"
    )
    if (audioExtensions.contains(ext)) return true

    val mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
    return mimeType != null && (mimeType.startsWith("audio/") || mimeType.startsWith("video/"))
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format("%.1f MB", mb)
    } else {
        String.format("%.0f KB", kb)
    }
}

private fun checkStoragePermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true
    }
}

private fun requestFullStoragePermission(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:" + context.packageName)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            context.startActivity(intent)
        }
    }
}
