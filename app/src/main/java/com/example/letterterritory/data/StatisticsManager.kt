package com.example.letterterritory.data

import android.content.Context
import android.content.SharedPreferences
import com.example.letterterritory.model.GameResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

data class GameStatistics(
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
    val bestTerritory: Int = 0,
    val bestTerritoryMargin: Int = 0,
    val longestWord: Int = 0,
    val bestAccuracy: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val perfectClaims: Int = 0,
    val totalWords: Int = 0,
    val totalValidWords: Int = 0,
    val totalInvalidWords: Int = 0,
    val totalPasses: Int = 0,
    val totalTimeouts: Int = 0,
    val totalCellsCaptured: Int = 0
)

class StatisticsManager(private val prefs: SharedPreferences) {
    constructor(context: Context) : this(
        context.getSharedPreferences("letter_territory_statistics", Context.MODE_PRIVATE)
    )

    private val _statistics = MutableStateFlow(loadStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    private fun loadStatistics(): GameStatistics {
        return GameStatistics(
            gamesPlayed = prefs.getInt(KEY_GAMES_PLAYED, 0),
            wins = prefs.getInt(KEY_WINS, 0),
            losses = prefs.getInt(KEY_LOSSES, 0),
            draws = prefs.getInt(KEY_DRAWS, 0),
            bestTerritory = prefs.getInt(KEY_BEST_TERRITORY, 0),
            bestTerritoryMargin = prefs.getInt(KEY_BEST_MARGIN, 0),
            longestWord = prefs.getInt(KEY_LONGEST_WORD, 0),
            bestAccuracy = prefs.getInt(KEY_BEST_ACCURACY, 0),
            currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0),
            bestStreak = prefs.getInt(KEY_BEST_STREAK, 0),
            perfectClaims = prefs.getInt(KEY_PERFECT_CLAIMS, 0),
            totalWords = prefs.getInt(KEY_TOTAL_WORDS, 0),
            totalValidWords = prefs.getInt(KEY_TOTAL_VALID_WORDS, 0),
            totalInvalidWords = prefs.getInt(KEY_TOTAL_INVALID_WORDS, 0),
            totalPasses = prefs.getInt(KEY_TOTAL_PASSES, 0),
            totalTimeouts = prefs.getInt(KEY_TOTAL_TIMEOUTS, 0),
            totalCellsCaptured = prefs.getInt(KEY_TOTAL_CELLS_CAPTURED, 0)
        )
    }

    /**
     * Records completed game result and updates lifetime statistics.
     */
    fun recordMatch(
        result: GameResult,
        playerTerritory: Int,
        cpuTerritory: Int,
        playerWords: List<String>,
        playerInvalidAttempts: Int,
        playerPasses: Int,
        playerTimeouts: Int,
        playerAccuracy: Int,
        cellsCaptured: Int
    ) {
        val current = _statistics.value

        val isWin = result == GameResult.PLAYER_WIN || result == GameResult.PERFECT_CLAIM
        val isLoss = result == GameResult.CPU_WIN
        val isDraw = result == GameResult.DRAW

        val newStreak = if (isWin) current.currentStreak + 1 else if (isLoss) 0 else current.currentStreak
        val bestStreak = max(current.bestStreak, newStreak)

        val margin = playerTerritory - cpuTerritory
        val newBestMargin = max(current.bestTerritoryMargin, margin)
        val newBestTerritory = max(current.bestTerritory, playerTerritory)

        val longestInGame = playerWords.maxOfOrNull { it.length } ?: 0
        val newLongestWord = max(current.longestWord, longestInGame)

        val newBestAccuracy = max(current.bestAccuracy, playerAccuracy)
        val newPerfectClaims = if (result == GameResult.PERFECT_CLAIM) current.perfectClaims + 1 else current.perfectClaims

        val updated = current.copy(
            gamesPlayed = current.gamesPlayed + 1,
            wins = if (isWin) current.wins + 1 else current.wins,
            losses = if (isLoss) current.losses + 1 else current.losses,
            draws = if (isDraw) current.draws + 1 else current.draws,
            bestTerritory = newBestTerritory,
            bestTerritoryMargin = newBestMargin,
            longestWord = newLongestWord,
            bestAccuracy = newBestAccuracy,
            currentStreak = newStreak,
            bestStreak = bestStreak,
            perfectClaims = newPerfectClaims,
            totalWords = current.totalWords + playerWords.size,
            totalValidWords = current.totalValidWords + playerWords.size,
            totalInvalidWords = current.totalInvalidWords + playerInvalidAttempts,
            totalPasses = current.totalPasses + playerPasses,
            totalTimeouts = current.totalTimeouts + playerTimeouts,
            totalCellsCaptured = current.totalCellsCaptured + cellsCaptured
        )

        prefs.edit().apply {
            putInt(KEY_GAMES_PLAYED, updated.gamesPlayed)
            putInt(KEY_WINS, updated.wins)
            putInt(KEY_LOSSES, updated.losses)
            putInt(KEY_DRAWS, updated.draws)
            putInt(KEY_BEST_TERRITORY, updated.bestTerritory)
            putInt(KEY_BEST_MARGIN, updated.bestTerritoryMargin)
            putInt(KEY_LONGEST_WORD, updated.longestWord)
            putInt(KEY_BEST_ACCURACY, updated.bestAccuracy)
            putInt(KEY_CURRENT_STREAK, updated.currentStreak)
            putInt(KEY_BEST_STREAK, updated.bestStreak)
            putInt(KEY_PERFECT_CLAIMS, updated.perfectClaims)
            putInt(KEY_TOTAL_WORDS, updated.totalWords)
            putInt(KEY_TOTAL_VALID_WORDS, updated.totalValidWords)
            putInt(KEY_TOTAL_INVALID_WORDS, updated.totalInvalidWords)
            putInt(KEY_TOTAL_PASSES, updated.totalPasses)
            putInt(KEY_TOTAL_TIMEOUTS, updated.totalTimeouts)
            putInt(KEY_TOTAL_CELLS_CAPTURED, updated.totalCellsCaptured)
            apply()
        }

        _statistics.value = updated
    }

    fun resetStatistics() {
        prefs.edit().clear().apply()
        _statistics.value = GameStatistics()
    }

    companion object {
        private const val KEY_GAMES_PLAYED = "games_played"
        private const val KEY_WINS = "wins"
        private const val KEY_LOSSES = "losses"
        private const val KEY_DRAWS = "draws"
        private const val KEY_BEST_TERRITORY = "best_territory"
        private const val KEY_BEST_MARGIN = "best_margin"
        private const val KEY_LONGEST_WORD = "longest_word"
        private const val KEY_BEST_ACCURACY = "best_accuracy"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_PERFECT_CLAIMS = "perfect_claims"
        private const val KEY_TOTAL_WORDS = "total_words"
        private const val KEY_TOTAL_VALID_WORDS = "total_valid_words"
        private const val KEY_TOTAL_INVALID_WORDS = "total_invalid_words"
        private const val KEY_TOTAL_PASSES = "total_passes"
        private const val KEY_TOTAL_TIMEOUTS = "total_timeouts"
        private const val KEY_TOTAL_CELLS_CAPTURED = "total_cells_captured"
    }
}
