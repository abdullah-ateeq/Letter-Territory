package com.example.letterterritory.ui.haptics

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Provides sensory tactile feedback for letter selection, invalid words,
 * territory claims, and win/loss notifications.
 */
class HapticsManager(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun tap() {
        vibrate(20, 50)
    }

    fun valid() {
        vibrate(45, 120)
    }

    fun capture() {
        vibrate(60, 180)
    }

    fun invalid() {
        vibrate(80, 100)
    }

    fun victory() {
        vibrate(120, 220)
    }

    fun defeat() {
        vibrate(150, 150)
    }

    private fun vibrate(durationMs: Long, amplitude: Int) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val clampedAmp = amplitude.coerceIn(1, 255)
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, clampedAmp))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
        }
    }
}
