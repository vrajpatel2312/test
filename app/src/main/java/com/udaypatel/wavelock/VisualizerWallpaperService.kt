package com.udaypatel.wavelock

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class VisualizerWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = VisualizerEngine()

    private inner class VisualizerEngine : Engine() {

        private val frameIntervalMs = 33L // ~30fps

        private val handler = Handler(Looper.getMainLooper())
        private val scope = CoroutineScope(Dispatchers.Default)
        private var collectJob: Job? = null
        private var visible = false

        private var latestBands = FloatArray(AudioDataBus.BAND_COUNT)

        private val barPaint = Paint().apply { isAntiAlias = true }
        private val backgroundPaint = Paint().apply { color = Color.BLACK }

        private val drawRunnable = object : Runnable {
            override fun run() {
                draw()
                if (visible) handler.postDelayed(this, frameIntervalMs)
            }
        }

        override fun onVisibilityChanged(isVisible: Boolean) {
            visible = isVisible
            if (isVisible) {
                collectJob = scope.launch {
                    AudioDataBus.bands.collect { latestBands = it }
                }
                handler.post(drawRunnable)
            } else {
                collectJob?.cancel()
                handler.removeCallbacks(drawRunnable)
            }
        }

        private fun draw() {
            val holder: SurfaceHolder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas?.let { render(it) }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
        }

        // TODO: this is where you port the look of window-music-wave —
        // swap this bar-equalizer for whatever wave/particle style you want.
        // `latestBands` is a FloatArray of AudioDataBus.BAND_COUNT magnitudes,
        // updated live off real system playback audio.
        private fun render(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            canvas.drawRect(0f, 0f, width, height, backgroundPaint)

            val bands = latestBands
            val barWidth = width / bands.size
            for (i in bands.indices) {
                val magnitude = (bands[i] * 6f).coerceIn(0f, 1f)
                val barHeight = magnitude * height * 0.6f
                val hue = 200f + (i.toFloat() / bands.size) * 80f
                barPaint.color = Color.HSVToColor(floatArrayOf(hue, 0.8f, 0.9f))
                val left = i * barWidth
                canvas.drawRect(
                    left,
                    height - barHeight,
                    left + barWidth * 0.8f,
                    height,
                    barPaint
                )
            }
        }

        override fun onDestroy() {
            collectJob?.cancel()
            handler.removeCallbacks(drawRunnable)
            super.onDestroy()
        }
    }
}
