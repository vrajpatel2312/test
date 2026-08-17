package com.udaypatel.wavelock

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Minimal iterative radix-2 FFT for turning a block of PCM samples into
 * magnitude bands suitable for a bar-style visualizer.
 */
object SimpleFFT {

    /**
     * @param samples PCM samples, length MUST be a power of two.
     * @param outBands number of magnitude bands to collapse the spectrum into.
     */
    fun magnitudeBands(samples: FloatArray, outBands: Int): FloatArray {
        val n = samples.size
        val real = samples.copyOf()
        val imag = FloatArray(n)

        // Bit-reversal permutation
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                val tr = real[i]; real[i] = real[j]; real[j] = tr
            }
            var m = n shr 1
            while (m in 1..j) {
                j -= m
                m = m shr 1
            }
            j += m
        }

        // Iterative Cooley-Tukey
        var size = 2
        while (size <= n) {
            val half = size / 2
            val angleStep = -2.0 * PI / size
            var i = 0
            while (i < n) {
                for (k in 0 until half) {
                    val angle = angleStep * k
                    val wr = cos(angle).toFloat()
                    val wi = sin(angle).toFloat()
                    val evenIndex = i + k
                    val oddIndex = i + k + half
                    val tr = real[oddIndex] * wr - imag[oddIndex] * wi
                    val ti = real[oddIndex] * wi + imag[oddIndex] * wr
                    real[oddIndex] = real[evenIndex] - tr
                    imag[oddIndex] = imag[evenIndex] - ti
                    real[evenIndex] += tr
                    imag[evenIndex] += ti
                }
                i += size
            }
            size = size shl 1
        }

        val half = n / 2
        val magnitudes = FloatArray(half) { i ->
            sqrt(real[i] * real[i] + imag[i] * imag[i])
        }

        // Collapse into log-spaced bands so bass isn't crowded out by treble
        val bands = FloatArray(outBands)
        for (b in 0 until outBands) {
            val startFrac = Math.pow(b.toDouble() / outBands, 2.0)
            val endFrac = Math.pow((b + 1).toDouble() / outBands, 2.0)
            val start = (startFrac * half).toInt().coerceIn(0, half - 1)
            val end = (endFrac * half).toInt().coerceIn(start + 1, half)
            var sum = 0f
            for (i in start until end) sum += magnitudes[i]
            bands[b] = sum / (end - start)
        }
        return bands
    }
}
