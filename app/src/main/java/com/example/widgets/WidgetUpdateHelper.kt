package com.example.widgets

import android.content.Context
import com.example.model.Track

object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context, track: Track?, isPlaying: Boolean, queue: List<Track> = emptyList()) {
        try {
            // Widget 1: 4x2 Non-Expandable Compact Receiver Widget
            ReceiverWidgetProvider.updateAllWidgets(context, track, isPlaying)

            // Widget 2: 4x3 Expandable Full Controls Widget
            FullControlsWidgetProvider.updateAllWidgets(context, track, isPlaying)

            // Widget 3: 4x3 Expandable Queue Widget
            QueueWidgetProvider.updateAllWidgets(context, track, isPlaying, queue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
