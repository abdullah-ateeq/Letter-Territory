package com.example.letterterritory

import com.example.letterterritory.data.GameSettings
import com.example.letterterritory.data.SettingsManager
import com.example.letterterritory.data.StatisticsManager
import com.example.letterterritory.data.ThemeMode
import com.example.letterterritory.domain.LetterTerritoryBoardGenerator
import com.example.letterterritory.domain.LetterTerritoryCpuStrategy
import com.example.letterterritory.domain.LetterTerritoryGameEngine
import com.example.letterterritory.domain.TerritoryWordSearchEngine
import com.example.letterterritory.domain.WordValidator
import com.example.letterterritory.model.CellOwner
import com.example.letterterritory.model.Coordinate
import com.example.letterterritory.model.CpuDifficulty
import com.example.letterterritory.model.GamePhase
import com.example.letterterritory.model.GameResult
import com.example.letterterritory.model.LetterCell
import com.example.letterterritory.model.Side
import com.example.letterterritory.model.SubmissionResult
import com.example.letterterritory.model.TerritoryMove
import com.example.letterterritory.model.TurnTimerOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

class LetterTerritoryGameTest {

    private lateinit var wordValidator: WordValidator
    private lateinit var boardGenerator: LetterTerritoryBoardGenerator
    private lateinit var searchEngine: TerritoryWordSearchEngine
    private lateinit var cpuStrategy: LetterTerritoryCpuStrategy

    @Before
    fun setUp() {
        // Initialize WordValidator with test dictionary stream or fallback words
        wordValidator = WordValidator.getOrCreate(null)
        if (wordValidator.wordCount == 0) {
            wordValidator.addWords(listOf(
                "CAT", "CAR", "CARE", "CARD", "CART", "ROAD", "TREE", "PLANT", "STONE",
                "PLAY", "GAME", "CLAIM", "WORD", "GRID", "BOLD", "BLUE", "RED", "GOLD",
                "WALK", "TALK", "LINE", "PINE", "LION", "BEAR", "DEER", "DUCK", "HAND"
            ))
        }
        boardGenerator = LetterTerritoryBoardGenerator(wordValidator, Random(42))
        searchEngine = TerritoryWordSearchEngine(wordValidator)
        cpuStrategy = LetterTerritoryCpuStrategy(wordValidator, searchEngine, Random(42))
    }

    private fun createCustomBoard(letterGrid: String): List<LetterCell> {
        val clean = letterGrid.replace(" ", "").replace("\n", "").uppercase()
        require(clean.length == 25) { "Board must be exactly 25 characters" }
        return (0 until 25).map { index ->
            LetterCell(
                id = index,
                coordinate = Coordinate(index / 5, index % 5),
                letter = clean[index],
                owner = CellOwner.NEUTRAL
            )
        }
    }

    // 1. Board generation
    @Test
    fun test01_boardGeneration() {
        val board = boardGenerator.generateBoard()
        assertNotNull(board)
        assertTrue(board.isNotEmpty())
    }

    // 2. Exactly 25 cells
    @Test
    fun test02_exactly25Cells() {
        val board = boardGenerator.generateBoard()
        assertEquals(25, board.size)
    }

    // 3. Unique IDs
    @Test
    fun test03_uniqueIds() {
        val board = boardGenerator.generateBoard()
        val ids = board.map { it.id }
        assertEquals(25, ids.distinct().size)
        assertEquals((0..24).toList(), ids.sorted())
    }

    // 4. Unique coordinates
    @Test
    fun test04_uniqueCoordinates() {
        val board = boardGenerator.generateBoard()
        val coords = board.map { it.coordinate }
        assertEquals(25, coords.distinct().size)
        for (c in coords) {
            assertTrue(c.row in 0..4)
            assertTrue(c.column in 0..4)
        }
    }

    // 5. Valid letters
    @Test
    fun test05_validLetters() {
        val board = boardGenerator.generateBoard()
        for (cell in board) {
            assertTrue(cell.letter in 'A'..'Z')
        }
    }

    // 6. Weighted generation
    @Test
    fun test06_weightedGeneration() {
        // High frequency vowels should appear in reasonable proportions across generated boards
        val board = boardGenerator.generateBoard()
        val vowels = setOf('A', 'E', 'I', 'O', 'U')
        val vowelCount = board.count { it.letter in vowels }
        assertTrue("Vowels should be between 4 and 15, was $vowelCount", vowelCount in 4..15)
    }

    // 7. Board playability
    @Test
    fun test07_boardPlayability() {
        val board = boardGenerator.generateBoard()
        val isValid = boardGenerator.validateBoard(board, diagonalEnabled = true)
        assertTrue(isValid)
        val wordCount = searchEngine.countDiscoverableWords(board, diagonalEnabled = true)
        assertTrue("Board must contain at least 10 words, found $wordCount", wordCount >= 10)
    }

    // 8. Dictionary loading
    @Test
    fun test08_dictionaryLoading() {
        assertTrue("Dictionary should have loaded entries", wordValidator.wordCount > 0)
        assertTrue(wordValidator.isWordInDictionary("CAT"))
        assertTrue(wordValidator.isWordInDictionary("ROAD"))
        assertTrue(wordValidator.isWordInDictionary("STONE"))
    }

    // 9. Word normalization
    @Test
    fun test09_wordNormalization() {
        assertEquals("CAT", wordValidator.normalizeWord("  cat  "))
        assertEquals("STONE", wordValidator.normalizeWord("sTonE"))
        assertTrue(wordValidator.isWordInDictionary("cat"))
    }

    // 10. 3-letter word
    @Test
    fun test10_threeLetterWord() {
        assertTrue(wordValidator.isValidLength("CAT"))
        assertTrue(wordValidator.isWordInDictionary("CAT"))
    }

    // 11. 4-letter word
    @Test
    fun test11_fourLetterWord() {
        assertTrue(wordValidator.isValidLength("ROAD"))
        assertTrue(wordValidator.isWordInDictionary("ROAD"))
    }

    // 12. 5-letter word
    @Test
    fun test12_fiveLetterWord() {
        assertTrue(wordValidator.isValidLength("PLANT"))
        assertTrue(wordValidator.isWordInDictionary("PLANT"))
    }

    // 13. 6-letter word
    @Test
    fun test13_sixLetterWord() {
        assertTrue(wordValidator.isValidLength("PLAYER"))
        assertTrue(wordValidator.isValidLength("GROUND"))
    }

    // 14. Too-short word
    @Test
    fun test14_tooShortWord() {
        assertFalse(wordValidator.isValidLength("AT"))
        assertFalse(wordValidator.isValidLength("A"))
        assertFalse(wordValidator.isValidLength(""))
    }

    // 15. Too-long word
    @Test
    fun test15_tooLongWord() {
        assertFalse(wordValidator.isValidLength("TERRITORY"))
        assertFalse(wordValidator.isValidLength("PLAYERS"))
    }

    // 16. Invalid dictionary word
    @Test
    fun test16_invalidDictionaryWord() {
        assertFalse(wordValidator.isWordInDictionary("CAX"))
        assertFalse(wordValidator.isWordInDictionary("ZZQX"))
    }

    // 17. Used word
    @Test
    fun test17_usedWord() {
        val board = createCustomBoard(
            """
            C A T S S
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val usedWords = setOf("CAT")
        val result = wordValidator.validateSubmission(
            selectedCellIds = listOf(0, 1, 2),
            cells = board,
            usedWords = usedWords,
            diagonalEnabled = true
        )
        assertEquals(SubmissionResult.WORD_ALREADY_USED, result)
    }

    // 18. Valid adjacency
    @Test
    fun test18_validAdjacency() {
        val c1 = Coordinate(1, 1)
        val cRight = Coordinate(1, 2)
        val cDown = Coordinate(2, 1)
        assertTrue(wordValidator.areAdjacent(c1, cRight, diagonalEnabled = false))
        assertTrue(wordValidator.areAdjacent(c1, cDown, diagonalEnabled = false))
    }

    // 19. Diagonal adjacency
    @Test
    fun test19_diagonalAdjacency() {
        val c1 = Coordinate(0, 0)
        val cDiag = Coordinate(1, 1)
        assertTrue(wordValidator.areAdjacent(c1, cDiag, diagonalEnabled = true))
        assertFalse(wordValidator.areAdjacent(c1, cDiag, diagonalEnabled = false))
    }

    // 20. Non-adjacent cells
    @Test
    fun test20_nonAdjacentCells() {
        val c1 = Coordinate(0, 0)
        val cFar = Coordinate(0, 2)
        assertFalse(wordValidator.areAdjacent(c1, cFar, diagonalEnabled = true))
        assertFalse(wordValidator.areAdjacent(c1, cFar, diagonalEnabled = false))
    }

    // 21. Duplicate cell rejection
    @Test
    fun test21_duplicateCellRejection() {
        val board = createCustomBoard(
            """
            C A T S S
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        // Path: 0 -> 1 -> 0 (C -> A -> C)
        assertFalse(wordValidator.isValidPath(listOf(0, 1, 0), board, diagonalEnabled = true))
    }

    // 22. Backtracking
    @Test
    fun test22_backtracking() {
        val board = createCustomBoard(
            """
            C A T S S
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)

        engine.selectCell(0) // C
        engine.selectCell(1) // A
        engine.selectCell(2) // T
        assertEquals("CAT", engine.state.currentWord)

        // Tap cell 1 (A) immediately previous to T -> backtracks to "CA"
        engine.selectCell(1)
        assertEquals("CA", engine.state.currentWord)
        assertEquals(listOf(0, 1), engine.state.selectedCellIds)
    }

    // 23. Maximum path length
    @Test
    fun test23_maximumPathLength() {
        val board = createCustomBoard(
            """
            P L A N E
            T S O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)

        // Select 6 letters along adjacent path: 0 -> 1 -> 2 -> 3 -> 4 -> 9
        val path6 = listOf(0, 1, 2, 3, 4, 9)
        for (id in path6) {
            engine.selectCell(id)
        }
        assertEquals(6, engine.state.selectedCellIds.size)

        // 7th selection (cell 8 is adjacent to cell 9) should be rejected
        engine.selectCell(8)
        assertEquals(6, engine.state.selectedCellIds.size)
    }

    // 24. Valid path
    @Test
    fun test24_validPath() {
        val board = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        assertTrue(wordValidator.isValidPath(listOf(0, 1, 2), board, diagonalEnabled = true))
    }

    // 25. Invalid path
    @Test
    fun test25_invalidPath() {
        val board = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        // Disconnected jump: 0 -> 4
        assertFalse(wordValidator.isValidPath(listOf(0, 4, 2), board, diagonalEnabled = true))
    }

    // 26. Neutral-cell detection
    @Test
    fun test26_neutralCellDetection() {
        val cells = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val captured = wordValidator.calculateCapture(listOf(0, 1, 2), cells, Side.PLAYER)
        assertEquals(listOf(0, 1, 2), captured)
    }

    // 27. No-territory detection
    @Test
    fun test27_noTerritoryDetection() {
        val cells = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        ).map { cell ->
            if (cell.id == 0) cell.copy(owner = CellOwner.PLAYER)
            else if (cell.id == 1) cell.copy(owner = CellOwner.CPU)
            else if (cell.id == 2) cell.copy(owner = CellOwner.PLAYER)
            else cell
        }

        val result = wordValidator.validateSubmission(
            selectedCellIds = listOf(0, 1, 2),
            cells = cells,
            usedWords = emptySet(),
            diagonalEnabled = true,
            side = Side.PLAYER
        )
        assertEquals(SubmissionResult.NO_TERRITORY, result)
    }

    // 28. Player territory capture
    @Test
    fun test28_playerTerritoryCapture() {
        val board = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)

        engine.selectCell(0)
        engine.selectCell(1)
        engine.selectCell(2)
        val (result, state) = engine.submitPlayerWord(diagonalEnabled = true)

        assertEquals(SubmissionResult.VALID, result)
        assertEquals(3, state.playerTerritory)
        assertEquals(CellOwner.PLAYER, state.cells[0].owner)
        assertEquals(CellOwner.PLAYER, state.cells[1].owner)
        assertEquals(CellOwner.PLAYER, state.cells[2].owner)
    }

    // 29. CPU territory capture
    @Test
    fun test29_cpuTerritoryCapture() {
        val board = createCustomBoard(
            """
            R O A D S
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)
        // Pass player turn to reach CPU turn
        engine.playerPass()

        assertEquals(GamePhase.CPU_TURN, engine.state.phase)
        val state = engine.executeCpuTurn(difficulty = CpuDifficulty.HARD, diagonalEnabled = true)

        assertTrue(state.cpuTerritory > 0)
        assertTrue(state.cpuWords.isNotEmpty())
    }

    // 30. Own-territory reuse
    @Test
    fun test30_ownTerritoryReuse() {
        // C is already PLAYER, A and T are NEUTRAL
        val board = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        ).map { if (it.id == 0) it.copy(owner = CellOwner.PLAYER) else it }

        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)

        engine.selectCell(0) // C (owned)
        engine.selectCell(1) // A (neutral)
        engine.selectCell(2) // T (neutral)
        val (result, state) = engine.submitPlayerWord(diagonalEnabled = true)

        assertEquals(SubmissionResult.VALID, result)
        assertEquals(2, state.lastMoveCells) // Only 2 newly captured!
        assertEquals(3, state.playerTerritory) // Total territory 3
    }

    // 31. Opponent-territory traversal
    @Test
    fun test31_opponentTerritoryTraversal() {
        // C is CPU owned, A and T are NEUTRAL
        val board = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        ).map { if (it.id == 0) it.copy(owner = CellOwner.CPU) else it }

        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)

        engine.selectCell(0) // C (CPU)
        engine.selectCell(1) // A (Neutral)
        engine.selectCell(2) // T (Neutral)
        val (result, state) = engine.submitPlayerWord(diagonalEnabled = true)

        assertEquals(SubmissionResult.VALID, result)
        assertEquals(CellOwner.CPU, state.cells[0].owner) // Remains CPU!
        assertEquals(CellOwner.PLAYER, state.cells[1].owner)
        assertEquals(CellOwner.PLAYER, state.cells[2].owner)
        assertEquals(2, state.playerTerritory)
    }

    // 32. CPU search
    @Test
    fun test32_cpuSearch() {
        val board = createCustomBoard(
            """
            C A T O O
            R O A D S
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val candidates = searchEngine.findCandidates(
            cells = board,
            usedWords = emptySet(),
            diagonalEnabled = true,
            side = Side.CPU
        )
        assertTrue(candidates.isNotEmpty())
        assertTrue(candidates.any { it.word == "CAT" || it.word == "ROAD" })
    }

    // 33. CPU candidate generation
    @Test
    fun test33_cpuCandidateGeneration() {
        val board = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val candidates = searchEngine.findCandidates(board, emptySet(), true, Side.CPU)
        val catCandidate = candidates.firstOrNull { it.word == "CAT" }
        assertNotNull(catCandidate)
        assertEquals(3, catCandidate!!.capturedCount)
    }

    // 34. CPU Easy
    @Test
    fun test34_cpuEasy() {
        val board = createCustomBoard(
            """
            C A T S S
            R O A D S
            P L A N T
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val move = cpuStrategy.selectMove(board, emptySet(), true, CpuDifficulty.EASY)
        // Easy may return a valid move or pass
        if (move != null) {
            assertTrue(wordValidator.isWordInDictionary(move.word))
        }
    }

    // 35. CPU Normal
    @Test
    fun test35_cpuNormal() {
        val board = createCustomBoard(
            """
            C A T S S
            R O A D S
            P L A N T
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val move = cpuStrategy.selectMove(board, emptySet(), true, CpuDifficulty.NORMAL)
        assertNotNull(move)
        assertTrue(move!!.capturedCount >= 1)
    }

    // 36. CPU Hard
    @Test
    fun test36_cpuHard() {
        val board = createCustomBoard(
            """
            C A T S S
            R O A D S
            P L A N T
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val move = cpuStrategy.selectMove(board, emptySet(), true, CpuDifficulty.HARD)
        assertNotNull(move)
        // Hard CPU prioritizes highest capture count
        assertTrue("Hard CPU should capture neutral cells", move!!.capturedCount >= 3)
    }

    // 37. Capture scoring
    @Test
    fun test37_captureScoring() {
        val board = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)
        engine.selectCell(0)
        engine.selectCell(1)
        engine.selectCell(2)
        val (_, state) = engine.submitPlayerWord(diagonalEnabled = true)

        assertEquals(3, state.playerTerritory)
        assertEquals(3, state.lastMoveCells)
    }

    // 38. Turn switching
    @Test
    fun test38_turnSwitching() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame()
        assertEquals(GamePhase.PLAYER_TURN, engine.state.phase)
        engine.playerPass()
        assertEquals(GamePhase.CPU_TURN, engine.state.phase)
    }

    // 39. Round progression
    @Test
    fun test39_roundProgression() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame()
        assertEquals(1, engine.state.round)

        engine.playerPass() // Player finishes turn
        engine.executeCpuTurn() // CPU finishes turn

        assertEquals(2, engine.state.round)
        assertEquals(GamePhase.PLAYER_TURN, engine.state.phase)
    }

    // 40. Pass
    @Test
    fun test40_pass() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame()
        engine.playerPass()

        assertEquals(1, engine.state.playerPasses)
        assertEquals(0, engine.state.playerInvalidAttempts)
        assertEquals(GamePhase.CPU_TURN, engine.state.phase)
    }

    // 41. Invalid submission
    @Test
    fun test41_invalidSubmission() {
        val board = createCustomBoard(
            """
            C A X O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)
        engine.selectCell(0)
        engine.selectCell(1)
        engine.selectCell(2)
        val (result, state) = engine.submitPlayerWord(diagonalEnabled = true)

        assertEquals(SubmissionResult.INVALID_WORD, result)
        assertEquals(1, state.playerInvalidAttempts)
        assertEquals(0, state.playerTerritory)
        assertEquals(GamePhase.CPU_TURN, state.phase)
    }

    // 42. Used word protection
    @Test
    fun test42_usedWordProtection() {
        val board = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)
        engine.selectCell(0)
        engine.selectCell(1)
        engine.selectCell(2)
        engine.submitPlayerWord()

        // Switch back to player
        engine.executeCpuTurn()

        // Attempt same word CAT again
        engine.selectCell(0)
        engine.selectCell(1)
        engine.selectCell(2)
        val (result, state) = engine.submitPlayerWord()

        assertEquals(SubmissionResult.WORD_ALREADY_USED, result)
        assertEquals(1, state.playerInvalidAttempts)
    }

    // 43. Game completion (12 rounds)
    @Test
    fun test43_gameCompletion() {
        val engine = LetterTerritoryGameEngine(wordValidator, totalRounds = 12)
        engine.startNewGame()

        repeat(12) {
            engine.playerPass()
            engine.executeCpuTurn()
        }

        assertEquals(GamePhase.GAME_OVER, engine.state.phase)
        assertNotNull(engine.state.result)
    }

    // 44. Early board completion (all 25 cells claimed)
    @Test
    fun test44_earlyBoardCompletion() {
        // Board where 24 cells are already claimed, only 1 cell neutral
        val board = createCustomBoard(
            """
            C A T O O
            O O O O O
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        ).map { cell ->
            if (cell.id == 2) cell.copy(owner = CellOwner.NEUTRAL)
            else cell.copy(owner = CellOwner.PLAYER)
        }

        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)

        engine.selectCell(0)
        engine.selectCell(1)
        engine.selectCell(2)
        val (res, state) = engine.submitPlayerWord()

        assertEquals(SubmissionResult.VALID, res)
        assertEquals(0, state.neutralCells)
        assertEquals(GamePhase.GAME_OVER, state.phase)
    }

    // 45. Player win
    @Test
    fun test45_playerWin() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        val result = engine.calculateFinalResult(
            pTerritory = 14,
            cTerritory = 11,
            pWords = emptyList(),
            cWords = emptyList(),
            pInvalid = 1,
            pPasses = 0,
            pTimeouts = 0
        )
        assertEquals(GameResult.PLAYER_WIN, result)
    }

    // 46. CPU win
    @Test
    fun test46_cpuWin() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        val result = engine.calculateFinalResult(
            pTerritory = 10,
            cTerritory = 15,
            pWords = emptyList(),
            cWords = emptyList(),
            pInvalid = 0,
            pPasses = 0,
            pTimeouts = 0
        )
        assertEquals(GameResult.CPU_WIN, result)
    }

    // 47. Territory tie
    @Test
    fun test47_territoryTie() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        val pMoves = listOf(TerritoryMove("CAT", listOf(0, 1, 2), listOf(0, 1, 2), 3))
        val cMoves = listOf(
            TerritoryMove("ROAD", listOf(5, 6, 7, 8), listOf(5, 6, 7, 8), 4),
            TerritoryMove("TREE", listOf(9, 10, 11, 12), listOf(9, 10, 11, 12), 4)
        )
        // Equal territory (12 each), CPU has more valid words (2 vs 1)
        val result = engine.calculateFinalResult(
            pTerritory = 12,
            cTerritory = 12,
            pWords = pMoves,
            cWords = cMoves,
            pInvalid = 0,
            pPasses = 0,
            pTimeouts = 0
        )
        assertEquals(GameResult.CPU_WIN, result)
    }

    // 48. Word-count tiebreaker
    @Test
    fun test48_wordCountTiebreaker() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        val pMoves = listOf(
            TerritoryMove("CAT", listOf(0, 1, 2), listOf(0, 1, 2), 3),
            TerritoryMove("DOG", listOf(3, 4, 5), listOf(3, 4, 5), 3)
        )
        val cMoves = listOf(
            TerritoryMove("STONE", listOf(6, 7, 8, 9, 10), listOf(6, 7, 8, 9, 10), 5)
        )
        // Player has 2 words, CPU has 1 word -> Player wins tiebreaker!
        val result = engine.calculateFinalResult(
            pTerritory = 10,
            cTerritory = 10,
            pWords = pMoves,
            cWords = cMoves,
            pInvalid = 0,
            pPasses = 0,
            pTimeouts = 0
        )
        assertEquals(GameResult.PLAYER_WIN, result)
    }

    // 49. Letter-count tiebreaker
    @Test
    fun test49_letterCountTiebreaker() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        val pMoves = listOf(TerritoryMove("PLANT", listOf(0, 1, 2, 3, 4), listOf(0, 1, 2, 3, 4), 5))
        val cMoves = listOf(TerritoryMove("CARD", listOf(5, 6, 7, 8), listOf(5, 6, 7, 8), 4))
        // Equal territory (10 each), equal word count (1 each), Player letters = 5 > CPU letters = 4
        val result = engine.calculateFinalResult(
            pTerritory = 10,
            cTerritory = 10,
            pWords = pMoves,
            cWords = cMoves,
            pInvalid = 0,
            pPasses = 0,
            pTimeouts = 0
        )
        assertEquals(GameResult.PLAYER_WIN, result)
    }

    // 50. Draw
    @Test
    fun test50_draw() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        val pMoves = listOf(TerritoryMove("ROAD", listOf(0, 1, 2, 3), listOf(0, 1, 2, 3), 4))
        val cMoves = listOf(TerritoryMove("TREE", listOf(4, 5, 6, 7), listOf(4, 5, 6, 7), 4))
        // Equal territory, equal words, equal letters -> DRAW
        val result = engine.calculateFinalResult(
            pTerritory = 10,
            cTerritory = 10,
            pWords = pMoves,
            cWords = cMoves,
            pInvalid = 0,
            pPasses = 0,
            pTimeouts = 0
        )
        assertEquals(GameResult.DRAW, result)
    }

    // 51. Perfect claim
    @Test
    fun test51_perfectClaim() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        // Player wins without invalid words, passes, or timeouts
        val result = engine.calculateFinalResult(
            pTerritory = 15,
            cTerritory = 10,
            pWords = listOf(TerritoryMove("CAT", listOf(0, 1, 2), listOf(0, 1, 2), 3)),
            cWords = emptyList(),
            pInvalid = 0,
            pPasses = 0,
            pTimeouts = 0
        )
        assertEquals(GameResult.PERFECT_CLAIM, result)
    }

    // 52. Accuracy
    @Test
    fun test52_accuracy() {
        val board = createCustomBoard(
            """
            C A T X X
            X X X X X
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame(customCells = board)

        // 1 valid move: 0(C) -> 1(A) -> 2(T) = "CAT"
        engine.selectCell(0)
        engine.selectCell(1)
        engine.selectCell(2)
        engine.submitPlayerWord()

        // CPU turn
        engine.executeCpuTurn()

        // 1 invalid move: 0(C) -> 1(A) -> 6(X) = "CAX" (adjacent: 0 to 1, 1 to 6)
        engine.selectCell(0)
        engine.selectCell(1)
        engine.selectCell(6)
        val (res, _) = engine.submitPlayerWord()
        assertEquals(SubmissionResult.INVALID_WORD, res)

        // 1 valid out of 2 total submissions = 50% accuracy
        assertEquals(50, engine.calculatePlayerAccuracy())
    }

    // 53. Timeout
    @Test
    fun test53_timeout() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame()
        engine.playerTimeout()

        assertEquals(1, engine.state.playerTimeouts)
        assertEquals(0, engine.state.playerInvalidAttempts)
        assertEquals(GamePhase.CPU_TURN, engine.state.phase)
    }

    // 54. Restart
    @Test
    fun test54_restart() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame()
        engine.playerPass()
        engine.executeCpuTurn()

        assertEquals(2, engine.state.round)

        engine.startNewGame()
        assertEquals(1, engine.state.round)
        assertEquals(GamePhase.PLAYER_TURN, engine.state.phase)
        assertEquals(0, engine.state.playerTerritory)
        assertEquals(0, engine.state.cpuTerritory)
    }

    // 55. Statistics
    @Test
    fun test55_statistics() {
        val prefs = TestSharedPreferences()
        val statsManager = StatisticsManager(prefs)

        statsManager.recordMatch(
            result = GameResult.PLAYER_WIN,
            playerTerritory = 16,
            cpuTerritory = 9,
            playerWords = listOf("CAT", "ROAD"),
            playerInvalidAttempts = 0,
            playerPasses = 0,
            playerTimeouts = 0,
            playerAccuracy = 100,
            cellsCaptured = 7
        )

        val stats = statsManager.statistics.value
        assertEquals(1, stats.gamesPlayed)
        assertEquals(1, stats.wins)
        assertEquals(16, stats.bestTerritory)
        assertEquals(7, stats.bestTerritoryMargin)
        assertEquals(4, stats.longestWord) // "ROAD" is 4 letters
        assertEquals(1, stats.currentStreak)
    }

    // 56. Win streak
    @Test
    fun test56_winStreak() {
        val prefs = TestSharedPreferences()
        val statsManager = StatisticsManager(prefs)

        // Win 1
        statsManager.recordMatch(GameResult.PLAYER_WIN, 15, 10, listOf("CAT"), 0, 0, 0, 100, 3)
        assertEquals(1, statsManager.statistics.value.currentStreak)

        // Win 2
        statsManager.recordMatch(GameResult.PLAYER_WIN, 14, 11, listOf("CAT"), 0, 0, 0, 100, 3)
        assertEquals(2, statsManager.statistics.value.currentStreak)
        assertEquals(2, statsManager.statistics.value.bestStreak)

        // Loss resets current streak
        statsManager.recordMatch(GameResult.CPU_WIN, 10, 15, listOf("CAT"), 0, 0, 0, 100, 3)
        assertEquals(0, statsManager.statistics.value.currentStreak)
        assertEquals(2, statsManager.statistics.value.bestStreak)
    }

    // 57. Settings persistence
    @Test
    fun test57_settingsPersistence() {
        val prefs = TestSharedPreferences()
        val settingsManager = SettingsManager(prefs)

        settingsManager.updateTheme(ThemeMode.DARK)
        settingsManager.updateCpuDifficulty(CpuDifficulty.HARD)
        settingsManager.updateTurnTimer(TurnTimerOption.TEN_SECONDS)
        settingsManager.updateDiagonalMovement(false)

        assertEquals(ThemeMode.DARK, settingsManager.settings.value.themeMode)
        assertEquals(CpuDifficulty.HARD, settingsManager.settings.value.cpuDifficulty)
        assertEquals(TurnTimerOption.TEN_SECONDS, settingsManager.settings.value.turnTimer)
        assertEquals(false, settingsManager.settings.value.diagonalMovement)

        // Verify reload from SharedPreferences
        val reloaded = SettingsManager(prefs)
        assertEquals(ThemeMode.DARK, reloaded.settings.value.themeMode)
        assertEquals(CpuDifficulty.HARD, reloaded.settings.value.cpuDifficulty)
        assertEquals(false, reloaded.settings.value.diagonalMovement)
    }

    // 58. Diagonal movement setting
    @Test
    fun test58_diagonalMovementSetting() {
        val board = createCustomBoard(
            """
            C O O O O
            O A O O O
            O O T O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        // Cells (0,0) -> (1,1) -> (2,2) = C -> A -> T along diagonal
        val path = listOf(0, 6, 12)

        // Valid when diagonal is ON
        assertTrue(wordValidator.isValidPath(path, board, diagonalEnabled = true))

        // Invalid when diagonal is OFF
        assertFalse(wordValidator.isValidPath(path, board, diagonalEnabled = false))
    }

    // 59. CPU fairness
    @Test
    fun test59_cpuFairness() {
        val board = createCustomBoard(
            """
            C A T O O
            R O A D S
            O O O O O
            O O O O O
            O O O O O
            """.trimIndent()
        )
        // CPU inspected moves only from the visible board and dictionary
        val move = cpuStrategy.selectMove(board, setOf("CAT"), diagonalEnabled = true, CpuDifficulty.HARD)
        assertNotNull(move)
        assertFalse("CPU must respect usedWords and not play CAT", move!!.word == "CAT")
        assertTrue("CPU must choose a word on the board", wordValidator.isWordInDictionary(move.word))
    }

    // 60. Rapid tap protection
    @Test
    fun test60_rapidTapProtection() {
        val engine = LetterTerritoryGameEngine(wordValidator)
        engine.startNewGame()

        // Switch to CPU turn
        engine.playerPass()
        assertEquals(GamePhase.CPU_TURN, engine.state.phase)

        // Rapid player taps while not player turn should be ignored
        val stateAfterTap = engine.selectCell(0)
        assertTrue("Selection during CPU turn must be ignored", stateAfterTap.selectedCellIds.isEmpty())

        val (subResult, _) = engine.submitPlayerWord()
        assertEquals(SubmissionResult.INVALID_PATH, subResult)
    }
}
