package com.udaypatel.wavelock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * In-memory bridge between AudioCaptureService and VisualizerWallpaperService.
 * Both run in the same app process, so a simple singleton StateFlow works —
 * no IPC needed.
 */
object AudioDataBus {
    const val BAND_COUNT = 32

    private val _bands = MutableStateFlow(FloatArray(BAND_COUNT))
    val bands: StateFlow<FloatArray> = _bands

    fun publish(newBands: FloatArray) {
        _bands.value = newBands
    }

    fun clear() {
        _bands.value = FloatArray(BAND_COUNT)
    }
}
