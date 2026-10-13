package com.example.letterterritory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.letterterritory.data.GameSettings
import com.example.letterterritory.data.GameStatistics
import com.example.letterterritory.data.SettingsManager
import com.example.letterterritory.data.StatisticsManager
import com.example.letterterritory.data.ThemeMode
import com.example.letterterritory.domain.LetterTerritoryGameEngine
import com.example.letterterritory.domain.WordValidator
import com.example.letterterritory.model.CpuDifficulty
import com.example.letterterritory.model.GamePhase
import com.example.letterterritory.model.GameResult
import com.example.letterterritory.model.LetterTerritoryGameState
import com.example.letterterritory.model.SubmissionResult
import com.example.letterterritory.model.TurnTimerOption
import com.example.letterterritory.ui.audio.SoundManager
import com.example.letterterritory.ui.haptics.HapticsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating game state, user interactions, CPU execution,
 * turn timers, sound/haptic feedback, and lifetime statistics.
 */
class LetterTerritoryViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val wordValidator = WordValidator.getOrCreate(application)
    private val gameEngine = LetterTerritoryGameEngine(wordValidator)
    val settingsManager = SettingsManager(application)
    val statisticsManager = StatisticsManager(application)
    private val soundManager = SoundManager(application)
    private val hapticsManager = HapticsManager(application)

    private val _gameState = MutableStateFlow(gameEngine.state)
    val gameState: StateFlow<LetterTerritoryGameState> = _gameState.asStateFlow()

    val settings: StateFlow<GameSettings> = settingsManager.settings
    val statistics: StateFlow<GameStatistics> = statisticsManager.statistics

    private val _isCpuThinking = MutableStateFlow(false)
    val isCpuThinking: StateFlow<Boolean> = _isCpuThinking.asStateFlow()

    private val _isProcessingInput = MutableStateFlow(false)
    val isProcessingInput: StateFlow<Boolean> = _isProcessingInput.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private var timerJob: Job? = null
    private var isMatchRecorded = false

    init {
        startTurnTimerIfNeeded()
    }

    /**
     * Cell tap event from UI.
     */
    fun onCellClicked(cellId: Int) {
        if (_isProcessingInput.value || _isCpuThinking.value) return
        if (_gameState.value.phase != GamePhase.PLAYER_TURN) return

        val prevSize = _gameState.value.selectedCellIds.size
        val updated = gameEngine.selectCell(
            cellId = cellId,
            diagonalEnabled = settings.value.diagonalMovement
        )
        _gameState.value = updated

        if (updated.selectedCellIds.size != prevSize) {
            if (settings.value.soundEnabled) soundManager.playTileSelect()
            if (settings.value.hapticsEnabled) hapticsManager.tap()
        }
    }

    /**
     * Clear selection button.
     */
    fun onClearClicked() {
        if (_isProcessingInput.value || _isCpuThinking.value) return
        if (_gameState.value.phase != GamePhase.PLAYER_TURN) return

        _gameState.value = gameEngine.clearSelection()
        if (settings.value.soundEnabled) soundManager.playTileSelect()
    }

    /**
     * Pass turn button.
     */
    fun onPassClicked() {
        if (_isProcessingInput.value || _isCpuThinking.value) return
        if (_gameState.value.phase != GamePhase.PLAYER_TURN) return

        stopTimer()
        _isProcessingInput.value = true
        _gameState.value = gameEngine.playerPass()
        if (settings.value.soundEnabled) soundManager.playTileSelect()

        scheduleCpuTurn()
    }

    /**
     * Claim word button.
     */
    fun onClaimClicked() {
        if (_isProcessingInput.value || _isCpuThinking.value) return
        if (_gameState.value.phase != GamePhase.PLAYER_TURN) return
        if (_gameState.value.selectedCellIds.size < WordValidator.MIN_WORD_LENGTH) return

        stopTimer()
        _isProcessingInput.value = true

        val (result, updated) = gameEngine.submitPlayerWord(
            diagonalEnabled = settings.value.diagonalMovement
        )
        _gameState.value = updated

        when (result) {
            SubmissionResult.VALID -> {
                if (settings.value.soundEnabled) {
                    soundManager.playWordValid()
                    soundManager.playTerritoryCapture()
                }
                if (settings.value.hapticsEnabled) {
                    hapticsManager.valid()
                    hapticsManager.capture()
                }

                if (updated.phase == GamePhase.GAME_OVER) {
                    handleGameOver(updated)
                    _isProcessingInput.value = false
                } else {
                    scheduleCpuTurn()
                }
            }
            SubmissionResult.INVALID_WORD,
            SubmissionResult.WORD_ALREADY_USED,
            SubmissionResult.NO_TERRITORY,
            SubmissionResult.INVALID_PATH -> {
                if (settings.value.soundEnabled) soundManager.playWordInvalid()
                if (settings.value.hapticsEnabled) hapticsManager.invalid()

                scheduleCpuTurn()
            }
            SubmissionResult.TOO_SHORT,
            SubmissionResult.TOO_LONG -> {
                _isProcessingInput.value = false
                startTurnTimerIfNeeded()
            }
        }
    }

    private fun scheduleCpuTurn() {
        viewModelScope.launch {
            _isCpuThinking.value = true
            val animDelay = if (settings.value.animationsEnabled) 600L else 200L
            delay(animDelay)

            val updated = gameEngine.executeCpuTurn(
                difficulty = settings.value.cpuDifficulty,
                diagonalEnabled = settings.value.diagonalMovement
            )
            _gameState.value = updated
            _isCpuThinking.value = false
            _isProcessingInput.value = false

            if (updated.lastMoveValid == true && updated.lastMoveCells > 0) {
                if (settings.value.soundEnabled) soundManager.playTerritoryCapture()
            }

            if (updated.phase == GamePhase.GAME_OVER) {
                handleGameOver(updated)
            } else {
                startTurnTimerIfNeeded()
            }
        }
    }

    private fun handleGameOver(state: LetterTerritoryGameState) {
        stopTimer()
        if (isMatchRecorded) return
        isMatchRecorded = true

        val result = state.result ?: GameResult.DRAW
        val pAccuracy = gameEngine.calculatePlayerAccuracy()
        val pCaptured = state.playerWords.sumOf { it.capturedCount }

        statisticsManager.recordMatch(
            result = result,
            playerTerritory = state.playerTerritory,
            cpuTerritory = state.cpuTerritory,
            playerWords = state.playerWords.map { it.word },
            playerInvalidAttempts = state.playerInvalidAttempts,
            playerPasses = state.playerPasses,
            playerTimeouts = state.playerTimeouts,
            playerAccuracy = pAccuracy,
            cellsCaptured = pCaptured
        )

        val isWin = result == GameResult.PLAYER_WIN || result == GameResult.PERFECT_CLAIM
        if (isWin) {
            if (settings.value.soundEnabled) soundManager.playVictory()
            if (settings.value.hapticsEnabled) hapticsManager.victory()
        } else if (result == GameResult.CPU_WIN) {
            if (settings.value.soundEnabled) soundManager.playDefeat()
            if (settings.value.hapticsEnabled) hapticsManager.defeat()
        }
    }

    private fun startTurnTimerIfNeeded() {
        stopTimer()
        val timerOption = settings.value.turnTimer
        if (timerOption == TurnTimerOption.OFF || _gameState.value.phase != GamePhase.PLAYER_TURN) {
            _timerSeconds.value = 0
            return
        }

        _timerSeconds.value = timerOption.seconds
        timerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0) {
                delay(1000L)
                _timerSeconds.value -= 1
            }
            // Timer expired!
            if (_gameState.value.phase == GamePhase.PLAYER_TURN) {
                _isProcessingInput.value = true
                val updated = gameEngine.playerTimeout()
                _gameState.value = updated
                if (settings.value.soundEnabled) soundManager.playWordInvalid()
                if (settings.value.hapticsEnabled) hapticsManager.invalid()
                scheduleCpuTurn()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    /**
     * Resets game with a brand new board.
     */
    fun restartGame() {
        stopTimer()
        isMatchRecorded = false
        _isCpuThinking.value = false
        _isProcessingInput.value = false
        _gameState.value = gameEngine.startNewGame(
            diagonalEnabled = settings.value.diagonalMovement
        )
        startTurnTimerIfNeeded()
    }

    // Settings adjustments
    fun setCpuDifficulty(difficulty: CpuDifficulty) {
        settingsManager.updateCpuDifficulty(difficulty)
    }

    fun setTheme(themeMode: ThemeMode) {
        settingsManager.updateTheme(themeMode)
    }

    fun setDynamicColor(enabled: Boolean) {
        settingsManager.updateDynamicColor(enabled)
    }

    fun setSound(enabled: Boolean) {
        settingsManager.updateSound(enabled)
    }

    fun setHaptics(enabled: Boolean) {
        settingsManager.updateHaptics(enabled)
    }

    fun setAnimations(enabled: Boolean) {
        settingsManager.updateAnimations(enabled)
    }

    fun setDiagonalMovement(enabled: Boolean) {
        settingsManager.updateDiagonalMovement(enabled)
    }

    fun setTurnTimer(option: TurnTimerOption) {
        settingsManager.updateTurnTimer(option)
        startTurnTimerIfNeeded()
    }

    fun setShowCoordinates(enabled: Boolean) {
        settingsManager.updateShowCoordinates(enabled)
    }

    fun setShowWordFeedback(enabled: Boolean) {
        settingsManager.updateShowWordFeedback(enabled)
    }

    fun setConfirmRestart(enabled: Boolean) {
        settingsManager.updateConfirmRestart(enabled)
    }

    fun resetSettings() {
        settingsManager.resetSettings()
    }

    fun resetStatistics() {
        statisticsManager.resetStatistics()
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
        soundManager.release()
    }
}
