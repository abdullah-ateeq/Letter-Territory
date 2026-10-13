package com.example.letterterritory.data

import android.content.Context
import android.content.SharedPreferences
import com.example.letterterritory.model.CpuDifficulty
import com.example.letterterritory.model.TurnTimerOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class GameSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val cpuDifficulty: CpuDifficulty = CpuDifficulty.NORMAL,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val animationsEnabled: Boolean = true,
    val diagonalMovement: Boolean = true,
    val turnTimer: TurnTimerOption = TurnTimerOption.OFF,
    val showCoordinates: Boolean = false,
    val showWordFeedback: Boolean = true,
    val confirmRestart: Boolean = true
)

class SettingsManager(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.getSharedPreferences("letter_territory_settings", Context.MODE_PRIVATE)
    )

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private fun loadSettings(): GameSettings {
        val themeStr = prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val diffStr = prefs.getString(KEY_DIFFICULTY, CpuDifficulty.NORMAL.name) ?: CpuDifficulty.NORMAL.name
        val timerStr = prefs.getString(KEY_TIMER, TurnTimerOption.OFF.name) ?: TurnTimerOption.OFF.name

        return GameSettings(
            themeMode = try { ThemeMode.valueOf(themeStr) } catch (_: Exception) { ThemeMode.SYSTEM },
            dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, true),
            cpuDifficulty = try { CpuDifficulty.valueOf(diffStr) } catch (_: Exception) { CpuDifficulty.NORMAL },
            soundEnabled = prefs.getBoolean(KEY_SOUND, true),
            hapticsEnabled = prefs.getBoolean(KEY_HAPTICS, true),
            animationsEnabled = prefs.getBoolean(KEY_ANIMATIONS, true),
            diagonalMovement = prefs.getBoolean(KEY_DIAGONAL, true),
            turnTimer = try { TurnTimerOption.valueOf(timerStr) } catch (_: Exception) { TurnTimerOption.OFF },
            showCoordinates = prefs.getBoolean(KEY_SHOW_COORDINATES, false),
            showWordFeedback = prefs.getBoolean(KEY_SHOW_WORD_FEEDBACK, true),
            confirmRestart = prefs.getBoolean(KEY_CONFIRM_RESTART, true)
        )
    }

    fun updateTheme(themeMode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, themeMode.name).apply()
        _settings.value = _settings.value.copy(themeMode = themeMode)
    }

    fun updateDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _settings.value = _settings.value.copy(dynamicColor = enabled)
    }

    fun updateCpuDifficulty(difficulty: CpuDifficulty) {
        prefs.edit().putString(KEY_DIFFICULTY, difficulty.name).apply()
        _settings.value = _settings.value.copy(cpuDifficulty = difficulty)
    }

    fun updateSound(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND, enabled).apply()
        _settings.value = _settings.value.copy(soundEnabled = enabled)
    }

    fun updateHaptics(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
        _settings.value = _settings.value.copy(hapticsEnabled = enabled)
    }

    fun updateAnimations(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANIMATIONS, enabled).apply()
        _settings.value = _settings.value.copy(animationsEnabled = enabled)
    }

    fun updateDiagonalMovement(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DIAGONAL, enabled).apply()
        _settings.value = _settings.value.copy(diagonalMovement = enabled)
    }

    fun updateTurnTimer(option: TurnTimerOption) {
        prefs.edit().putString(KEY_TIMER, option.name).apply()
        _settings.value = _settings.value.copy(turnTimer = option)
    }

    fun updateShowCoordinates(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_COORDINATES, enabled).apply()
        _settings.value = _settings.value.copy(showCoordinates = enabled)
    }

    fun updateShowWordFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_WORD_FEEDBACK, enabled).apply()
        _settings.value = _settings.value.copy(showWordFeedback = enabled)
    }

    fun updateConfirmRestart(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONFIRM_RESTART, enabled).apply()
        _settings.value = _settings.value.copy(confirmRestart = enabled)
    }

    fun resetSettings() {
        prefs.edit().clear().apply()
        _settings.value = GameSettings()
    }

    companion object {
        private const val KEY_THEME = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_DIFFICULTY = "cpu_difficulty"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTICS = "haptics_enabled"
        private const val KEY_ANIMATIONS = "animations_enabled"
        private const val KEY_DIAGONAL = "diagonal_movement"
        private const val KEY_TIMER = "turn_timer"
        private const val KEY_SHOW_COORDINATES = "show_coordinates"
        private const val KEY_SHOW_WORD_FEEDBACK = "show_word_feedback"
        private const val KEY_CONFIRM_RESTART = "confirm_restart"
    }
}
