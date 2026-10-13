package com.example.letterterritory.ui.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Provides offline audio feedback for game events using ToneGenerator
 * and Android AudioManager sound effects.
 */
class SoundManager(private val context: Context) {
    private var toneGenerator: ToneGenerator? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (_: Exception) {
        }
    }

    fun playTileSelect() {
        audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.4f)
    }

    fun playWordValid() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
        } catch (_: Exception) {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 0.6f)
        }
    }

    fun playWordInvalid() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 140)
        } catch (_: Exception) {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE, 0.6f)
        }
    }

    fun playTerritoryCapture() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 120)
        } catch (_: Exception) {
            audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN, 0.6f)
        }
    }

    fun playVictory() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 300)
        } catch (_: Exception) {
        }
    }

    fun playDefeat() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 250)
        } catch (_: Exception) {
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
