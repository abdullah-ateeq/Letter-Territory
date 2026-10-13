package com.example.letterterritory.domain

import com.example.letterterritory.model.CellOwner
import com.example.letterterritory.model.CpuDifficulty
import com.example.letterterritory.model.GamePhase
import com.example.letterterritory.model.GameResult
import com.example.letterterritory.model.LetterCell
import com.example.letterterritory.model.LetterTerritoryGameState
import com.example.letterterritory.model.Side
import com.example.letterterritory.model.SubmissionResult
import com.example.letterterritory.model.TerritoryMove
import kotlin.math.roundToInt

/**
 * Pure Kotlin game engine managing state transitions, territory capturing,
 * turn cycles, round progression, and outcome evaluation for Letter Territory.
 */
class LetterTerritoryGameEngine(
    val wordValidator: WordValidator,
    private val boardGenerator: LetterTerritoryBoardGenerator = LetterTerritoryBoardGenerator(wordValidator),
    private val cpuStrategy: LetterTerritoryCpuStrategy = LetterTerritoryCpuStrategy(wordValidator),
    val totalRounds: Int = 12
) {

    private var currentState: LetterTerritoryGameState

    init {
        val initialCells = boardGenerator.generateBoard()
        currentState = LetterTerritoryGameState(
            cells = initialCells,
            totalRounds = totalRounds,
            neutralCells = initialCells.size
        )
    }

    val state: LetterTerritoryGameState
        get() = currentState

    /**
     * Resets match with a newly generated board.
     */
    fun startNewGame(
        diagonalEnabled: Boolean = true,
        customCells: List<LetterCell>? = null
    ): LetterTerritoryGameState {
        val cells = customCells ?: boardGenerator.generateBoard(diagonalEnabled)
        val neutralCount = cells.count { it.owner == CellOwner.NEUTRAL }
        val pCount = cells.count { it.owner == CellOwner.PLAYER }
        val cCount = cells.count { it.owner == CellOwner.CPU }

        currentState = LetterTerritoryGameState(
            cells = cells,
            phase = GamePhase.PLAYER_TURN,
            currentSide = Side.PLAYER,
            round = 1,
            totalRounds = totalRounds,
            selectedCellIds = emptyList(),
            currentWord = "",
            usedWords = emptySet(),
            playerWords = emptyList(),
            cpuWords = emptyList(),
            playerTerritory = pCount,
            cpuTerritory = cCount,
            neutralCells = neutralCount,
            playerInvalidAttempts = 0,
            cpuInvalidAttempts = 0,
            playerPasses = 0,
            cpuPasses = 0,
            playerTimeouts = 0,
            lastWord = null,
            lastMoveCells = 0,
            lastMoveValid = null,
            feedbackMessage = null,
            newlyCapturedIds = emptySet(),
            result = null
        )
        return currentState
    }

    /**
     * Handles tapping a cell on the board during PLAYER_TURN.
     */
    fun selectCell(cellId: Int, diagonalEnabled: Boolean = true): LetterTerritoryGameState {
        if (currentState.phase != GamePhase.PLAYER_TURN) {
            return currentState
        }

        val cellMap = currentState.cells.associateBy { it.id }
        val targetCell = cellMap[cellId] ?: return currentState

        val currentPath = currentState.selectedCellIds

        // Backtracking: if tapping immediately previous cell, pop the last selected cell
        if (currentPath.size >= 2 && currentPath[currentPath.size - 2] == cellId) {
            val newPath = currentPath.dropLast(1)
            val newWord = wordValidator.buildWord(currentState.cells, newPath)
            currentState = currentState.copy(
                selectedCellIds = newPath,
                currentWord = newWord,
                feedbackMessage = null
            )
            return currentState
        }

        // Tapping the current head again does nothing
        if (currentPath.isNotEmpty() && currentPath.last() == cellId) {
            return currentState
        }

        // Duplicate selection earlier in path is rejected
        if (currentPath.contains(cellId)) {
            return currentState
        }

        // Maximum word length limit (6 letters)
        if (currentPath.size >= WordValidator.MAX_WORD_LENGTH) {
            return currentState
        }

        // Adjacency check for subsequent cells
        if (currentPath.isNotEmpty()) {
            val lastCell = cellMap[currentPath.last()] ?: return currentState
            if (!wordValidator.areAdjacent(lastCell.coordinate, targetCell.coordinate, diagonalEnabled)) {
                return currentState
            }
        }

        val newPath = currentPath + cellId
        val newWord = wordValidator.buildWord(currentState.cells, newPath)
        currentState = currentState.copy(
            selectedCellIds = newPath,
            currentWord = newWord,
            feedbackMessage = null
        )
        return currentState
    }

    /**
     * Clears current cell selection.
     */
    fun clearSelection(): LetterTerritoryGameState {
        if (currentState.phase != GamePhase.PLAYER_TURN) return currentState
        currentState = currentState.copy(
            selectedCellIds = emptyList(),
            currentWord = "",
            feedbackMessage = null
        )
        return currentState
    }

    /**
     * Submits player's formed word.
     */
    fun submitPlayerWord(diagonalEnabled: Boolean = true): Pair<SubmissionResult, LetterTerritoryGameState> {
        if (currentState.phase != GamePhase.PLAYER_TURN) {
            return Pair(SubmissionResult.INVALID_PATH, currentState)
        }

        val selectedIds = currentState.selectedCellIds
        val validation = wordValidator.validateSubmission(
            selectedCellIds = selectedIds,
            cells = currentState.cells,
            usedWords = currentState.usedWords,
            diagonalEnabled = diagonalEnabled,
            side = Side.PLAYER
        )

        val word = wordValidator.buildWord(currentState.cells, selectedIds)

        when (validation) {
            SubmissionResult.VALID -> {
                val captured = wordValidator.calculateCapture(selectedIds, currentState.cells, Side.PLAYER)
                val updatedCells = currentState.cells.map { cell ->
                    if (captured.contains(cell.id)) {
                        cell.copy(owner = CellOwner.PLAYER)
                    } else {
                        cell
                    }
                }

                val move = TerritoryMove(
                    word = word,
                    cellIds = selectedIds,
                    capturedCellIds = captured,
                    capturedCount = captured.size
                )

                val newPlayerTerritory = updatedCells.count { it.owner == CellOwner.PLAYER }
                val newCpuTerritory = updatedCells.count { it.owner == CellOwner.CPU }
                val newNeutral = updatedCells.count { it.owner == CellOwner.NEUTRAL }

                val nextPhase = if (newNeutral == 0) GamePhase.GAME_OVER else GamePhase.CPU_TURN
                val finalResult = if (newNeutral == 0) {
                    calculateFinalResult(
                        pTerritory = newPlayerTerritory,
                        cTerritory = newCpuTerritory,
                        pWords = currentState.playerWords + move,
                        cWords = currentState.cpuWords,
                        pInvalid = currentState.playerInvalidAttempts,
                        pPasses = currentState.playerPasses,
                        pTimeouts = currentState.playerTimeouts
                    )
                } else null

                currentState = currentState.copy(
                    cells = updatedCells,
                    phase = nextPhase,
                    currentSide = if (nextPhase == GamePhase.GAME_OVER) Side.PLAYER else Side.CPU,
                    selectedCellIds = emptyList(),
                    currentWord = "",
                    usedWords = currentState.usedWords + word,
                    playerWords = currentState.playerWords + move,
                    playerTerritory = newPlayerTerritory,
                    cpuTerritory = newCpuTerritory,
                    neutralCells = newNeutral,
                    lastWord = word,
                    lastMoveCells = captured.size,
                    lastMoveValid = true,
                    feedbackMessage = "TERRITORY CLAIMED: $word +${captured.size} CELLS",
                    newlyCapturedIds = captured.toSet(),
                    result = finalResult
                )
            }
            SubmissionResult.TOO_SHORT -> {
                // Keep selection, just notify or return
                currentState = currentState.copy(
                    feedbackMessage = "TOO SHORT (MIN 3)"
                )
            }
            SubmissionResult.TOO_LONG -> {
                currentState = currentState.copy(
                    feedbackMessage = "TOO LONG (MAX 6)"
                )
            }
            SubmissionResult.INVALID_WORD -> {
                currentState = currentState.copy(
                    phase = GamePhase.CPU_TURN,
                    currentSide = Side.CPU,
                    selectedCellIds = emptyList(),
                    currentWord = "",
                    playerInvalidAttempts = currentState.playerInvalidAttempts + 1,
                    lastWord = word,
                    lastMoveCells = 0,
                    lastMoveValid = false,
                    feedbackMessage = "NOT A WORD",
                    newlyCapturedIds = emptySet()
                )
            }
            SubmissionResult.WORD_ALREADY_USED -> {
                currentState = currentState.copy(
                    phase = GamePhase.CPU_TURN,
                    currentSide = Side.CPU,
                    selectedCellIds = emptyList(),
                    currentWord = "",
                    playerInvalidAttempts = currentState.playerInvalidAttempts + 1,
                    lastWord = word,
                    lastMoveCells = 0,
                    lastMoveValid = false,
                    feedbackMessage = "WORD ALREADY USED",
                    newlyCapturedIds = emptySet()
                )
            }
            SubmissionResult.NO_TERRITORY -> {
                currentState = currentState.copy(
                    phase = GamePhase.CPU_TURN,
                    currentSide = Side.CPU,
                    selectedCellIds = emptyList(),
                    currentWord = "",
                    playerInvalidAttempts = currentState.playerInvalidAttempts + 1,
                    lastWord = word,
                    lastMoveCells = 0,
                    lastMoveValid = false,
                    feedbackMessage = "NO NEW TERRITORY",
                    newlyCapturedIds = emptySet()
                )
            }
            SubmissionResult.INVALID_PATH -> {
                currentState = currentState.copy(
                    phase = GamePhase.CPU_TURN,
                    currentSide = Side.CPU,
                    selectedCellIds = emptyList(),
                    currentWord = "",
                    playerInvalidAttempts = currentState.playerInvalidAttempts + 1,
                    lastWord = word,
                    lastMoveCells = 0,
                    lastMoveValid = false,
                    feedbackMessage = "INVALID PATH",
                    newlyCapturedIds = emptySet()
                )
            }
        }

        return Pair(validation, currentState)
    }

    /**
     * Player voluntarily passes their turn.
     */
    fun playerPass(): LetterTerritoryGameState {
        if (currentState.phase != GamePhase.PLAYER_TURN) return currentState

        currentState = currentState.copy(
            phase = GamePhase.CPU_TURN,
            currentSide = Side.CPU,
            selectedCellIds = emptyList(),
            currentWord = "",
            playerPasses = currentState.playerPasses + 1,
            feedbackMessage = "PASSED",
            newlyCapturedIds = emptySet()
        )
        return currentState
    }

    /**
     * Turn timer expires for the player.
     */
    fun playerTimeout(): LetterTerritoryGameState {
        if (currentState.phase != GamePhase.PLAYER_TURN) return currentState

        currentState = currentState.copy(
            phase = GamePhase.CPU_TURN,
            currentSide = Side.CPU,
            selectedCellIds = emptyList(),
            currentWord = "",
            playerTimeouts = currentState.playerTimeouts + 1,
            feedbackMessage = "TIME OUT",
            newlyCapturedIds = emptySet()
        )
        return currentState
    }

    /**
     * Executes CPU move for the current round.
     */
    fun executeCpuTurn(
        difficulty: CpuDifficulty = CpuDifficulty.NORMAL,
        diagonalEnabled: Boolean = true
    ): LetterTerritoryGameState {
        if (currentState.phase != GamePhase.CPU_TURN) return currentState

        val move = cpuStrategy.selectMove(
            cells = currentState.cells,
            usedWords = currentState.usedWords,
            diagonalEnabled = diagonalEnabled,
            difficulty = difficulty
        )

        var updatedCells = currentState.cells
        var updatedCpuWords = currentState.cpuWords
        var updatedUsedWords = currentState.usedWords
        var updatedCpuPasses = currentState.cpuPasses
        var feedback = "CPU PASSED"
        var lastWord: String? = null
        var lastMoveCells = 0
        var newlyCaptured = emptySet<Int>()

        if (move != null) {
            val captured = move.capturedCellIds
            updatedCells = currentState.cells.map { cell ->
                if (captured.contains(cell.id)) {
                    cell.copy(owner = CellOwner.CPU)
                } else {
                    cell
                }
            }
            updatedCpuWords = currentState.cpuWords + move
            updatedUsedWords = currentState.usedWords + move.word
            feedback = "CPU CLAIMED: ${move.word} +${captured.size} CELLS"
            lastWord = move.word
            lastMoveCells = captured.size
            newlyCaptured = captured.toSet()
        } else {
            updatedCpuPasses++
        }

        val newPlayerTerritory = updatedCells.count { it.owner == CellOwner.PLAYER }
        val newCpuTerritory = updatedCells.count { it.owner == CellOwner.CPU }
        val newNeutral = updatedCells.count { it.owner == CellOwner.NEUTRAL }

        // Check if game ends: all cells claimed OR reached max rounds
        val isGameOver = newNeutral == 0 || currentState.round >= totalRounds

        val nextPhase = if (isGameOver) GamePhase.GAME_OVER else GamePhase.PLAYER_TURN
        val nextSide = if (isGameOver) Side.PLAYER else Side.PLAYER
        val nextRound = if (isGameOver) currentState.round else currentState.round + 1

        val finalResult = if (isGameOver) {
            calculateFinalResult(
                pTerritory = newPlayerTerritory,
                cTerritory = newCpuTerritory,
                pWords = currentState.playerWords,
                cWords = updatedCpuWords,
                pInvalid = currentState.playerInvalidAttempts,
                pPasses = currentState.playerPasses,
                pTimeouts = currentState.playerTimeouts
            )
        } else null

        currentState = currentState.copy(
            cells = updatedCells,
            phase = nextPhase,
            currentSide = nextSide,
            round = nextRound,
            usedWords = updatedUsedWords,
            cpuWords = updatedCpuWords,
            playerTerritory = newPlayerTerritory,
            cpuTerritory = newCpuTerritory,
            neutralCells = newNeutral,
            cpuPasses = updatedCpuPasses,
            lastWord = lastWord,
            lastMoveCells = lastMoveCells,
            lastMoveValid = move != null,
            feedbackMessage = feedback,
            newlyCapturedIds = newlyCaptured,
            result = finalResult
        )

        return currentState
    }

    /**
     * Determines outcome and tiebreakers according to game rules.
     */
    fun calculateFinalResult(
        pTerritory: Int,
        cTerritory: Int,
        pWords: List<TerritoryMove>,
        cWords: List<TerritoryMove>,
        pInvalid: Int,
        pPasses: Int,
        pTimeouts: Int
    ): GameResult {
        return when {
            pTerritory > cTerritory -> {
                // Perfect claim check: won, no invalid words, no passes, no timeouts
                if (pInvalid == 0 && pPasses == 0 && pTimeouts == 0) {
                    GameResult.PERFECT_CLAIM
                } else {
                    GameResult.PLAYER_WIN
                }
            }
            cTerritory > pTerritory -> {
                GameResult.CPU_WIN
            }
            else -> {
                // Equal territory -> Tiebreaker 1: valid word count
                if (pWords.size > cWords.size) {
                    GameResult.PLAYER_WIN
                } else if (cWords.size > pWords.size) {
                    GameResult.CPU_WIN
                } else {
                    // Tiebreaker 2: total letters used
                    val pLetters = pWords.sumOf { it.word.length }
                    val cLetters = cWords.sumOf { it.word.length }
                    if (pLetters > cLetters) {
                        GameResult.PLAYER_WIN
                    } else if (cLetters > pLetters) {
                        GameResult.CPU_WIN
                    } else {
                        GameResult.DRAW
                    }
                }
            }
        }
    }

    /**
     * Accuracy percentage: valid submissions / total submissions (passes & timeouts excluded).
     */
    fun calculatePlayerAccuracy(): Int {
        val valid = currentState.playerWords.size
        val invalid = currentState.playerInvalidAttempts
        val total = valid + invalid
        if (total == 0) return 100
        return ((valid.toDouble() / total) * 100.0).roundToInt()
    }
}
