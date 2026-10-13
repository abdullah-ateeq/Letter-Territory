package com.example.letterterritory.model

/**
 * Coordinate on the 5x5 board.
 * row: 0..4, column: 0..4
 */
data class Coordinate(
    val row: Int,
    val column: Int
) {
    /**
     * Coordinate label, e.g. A1, C3, E5
     */
    val label: String
        get() {
            val colChar = ('A'.code + column).toChar()
            val rowNum = row + 1
            return "$colChar$rowNum"
        }
}

/**
 * Ownership of a board cell.
 */
enum class CellOwner {
    NEUTRAL,
    PLAYER,
    CPU
}

/**
 * Representation of an individual cell on the grid.
 */
data class LetterCell(
    val id: Int,
    val coordinate: Coordinate,
    val letter: Char,
    val owner: CellOwner = CellOwner.NEUTRAL
)

/**
 * Current phase of the game loop.
 */
enum class GamePhase {
    PLAYER_TURN,
    RESOLVING,
    CPU_TURN,
    GAME_OVER
}

/**
 * Participant side in the match.
 */
enum class Side {
    PLAYER,
    CPU
}

/**
 * Final game outcome.
 */
enum class GameResult {
    PLAYER_WIN,
    CPU_WIN,
    DRAW,
    PERFECT_CLAIM
}

/**
 * Status resulting from attempting a word submission.
 */
enum class SubmissionResult {
    VALID,
    INVALID_WORD,
    WORD_ALREADY_USED,
    INVALID_PATH,
    TOO_SHORT,
    TOO_LONG,
    NO_TERRITORY
}

/**
 * Sequence of selected cells forming a word.
 */
data class WordPath(
    val word: String,
    val cellIds: List<Int>
)

/**
 * Record of a successful word claim move and territory captured.
 */
data class TerritoryMove(
    val word: String,
    val cellIds: List<Int>,
    val capturedCellIds: List<Int>,
    val capturedCount: Int
)

/**
 * CPU difficulty setting.
 */
enum class CpuDifficulty {
    EASY,
    NORMAL,
    HARD
}

/**
 * Turn timer setting.
 */
enum class TurnTimerOption(val seconds: Int) {
    OFF(0),
    TEN_SECONDS(10),
    FIFTEEN_SECONDS(15)
}

/**
 * Immutable state of a Letter Territory match.
 */
data class LetterTerritoryGameState(
    val cells: List<LetterCell>,
    val phase: GamePhase = GamePhase.PLAYER_TURN,
    val currentSide: Side = Side.PLAYER,
    val round: Int = 1,
    val totalRounds: Int = 12,
    val selectedCellIds: List<Int> = emptyList(),
    val currentWord: String = "",
    val usedWords: Set<String> = emptySet(),
    val playerWords: List<TerritoryMove> = emptyList(),
    val cpuWords: List<TerritoryMove> = emptyList(),
    val playerTerritory: Int = 0,
    val cpuTerritory: Int = 0,
    val neutralCells: Int = 25,
    val playerInvalidAttempts: Int = 0,
    val cpuInvalidAttempts: Int = 0,
    val playerPasses: Int = 0,
    val cpuPasses: Int = 0,
    val playerTimeouts: Int = 0,
    val lastWord: String? = null,
    val lastMoveCells: Int = 0,
    val lastMoveValid: Boolean? = null,
    val feedbackMessage: String? = null,
    val newlyCapturedIds: Set<Int> = emptySet(),
    val result: GameResult? = null
)
