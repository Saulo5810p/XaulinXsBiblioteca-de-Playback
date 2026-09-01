package com.example.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.R
import com.example.model.Track
import com.example.ui.theme.*

object AlbumArtUtil {

    fun getTrackAlbumArtRes(track: Track?): Int {
        if (track == null) return R.drawable.album_analog_test_1785694745318
        return when (track.id) {
            9001L -> R.drawable.album_analog_test_1785694745318
            9002L -> R.drawable.album_jazz_lounge_1785694817014
            9003L -> R.drawable.album_tokyo_synth_1785694886945
            9004L -> R.drawable.album_vacuum_tube_1785694946393
            else -> {
                val hash = track.title.hashCode() % 4
                when (Math.abs(hash)) {
                    0 -> R.drawable.album_analog_test_1785694745318
                    1 -> R.drawable.album_jazz_lounge_1785694817014
                    2 -> R.drawable.album_tokyo_synth_1785694886945
                    else -> R.drawable.album_vacuum_tube_1785694946393
                }
            }
        }
    }

    fun getTrackAlbumArtBitmap(context: Context, track: Track?): Bitmap {
        if (track != null) {
            track.albumArtUri?.let { uriStr ->
                try {
                    val uri = Uri.parse(uriStr)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        if (bitmap != null) return bitmap
                    }
                } catch (e: Exception) {
                    // ignore and try next source
                }
            }

            if (track.albumArtUri.isNullOrBlank() && track.dataPath.isNotBlank() && java.io.File(track.dataPath).exists()) {
                // Safely check if file is an image rather than audio before attempting MMR
                if (track.dataPath.endsWith(".jpg", true) || track.dataPath.endsWith(".png", true) || track.dataPath.endsWith(".webp", true)) {
                    val bitmap = BitmapFactory.decodeFile(track.dataPath)
                    if (bitmap != null) return bitmap
                }
            }
        }

        val resId = getTrackAlbumArtRes(track)
        val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
        return BitmapFactory.decodeResource(context.resources, resId, opts)
            ?: BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
    }

    fun getTrackStudioColor(track: Track?): Color {
        if (track == null) return VintageAmber
        return when (track.id) {
            9001L -> VintageAmber // Orange Gold Tube
            9002L -> Color(0xFFFF9500) // Deep Amber Jazz
            9003L -> Color(0xFF00E5FF) // Neon Cyan Synth
            9004L -> Color(0xFFFF2D55) // Crimson Vacuum
            else -> {
                val hash = track.artist.hashCode() % 4
                when (Math.abs(hash)) {
                    0 -> VintageAmber
                    1 -> Color(0xFF30D158) // Fluorite Green
                    2 -> Color(0xFFBF5AF2) // Electric Purple
                    else -> BrassGold
                }
            }
        }
    }

    fun getTrackStudioGlowGradient(track: Track?): List<Color> {
        val primaryColor = getTrackStudioColor(track)
        return listOf(
            primaryColor.copy(alpha = 0.35f),
            primaryColor.copy(alpha = 0.10f),
            Color.Transparent
        )
    }
}

@Composable
fun rememberAlbumArtPainter(track: Track?): Painter {
    val fallbackRes = AlbumArtUtil.getTrackAlbumArtRes(track)
    val fallbackPainter = painterResource(id = fallbackRes)

    val dataModel: Any? = when {
        !track?.albumArtUri.isNullOrBlank() -> track?.albumArtUri
        !track?.dataPath.isNullOrBlank() && (track.dataPath.endsWith(".jpg", true) || track.dataPath.endsWith(".png", true) || track.dataPath.endsWith(".webp", true)) -> track.dataPath
        else -> null
    }

    if (dataModel == null) return fallbackPainter

    return rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(dataModel)
            .crossfade(true)
            .error(fallbackRes)
            .placeholder(fallbackRes)
            .build()
    )
}

