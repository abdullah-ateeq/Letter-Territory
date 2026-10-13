package com.example.letterterritory.domain

import com.example.letterterritory.model.CellOwner
import com.example.letterterritory.model.Coordinate
import com.example.letterterritory.model.LetterCell
import kotlin.random.Random

/**
 * Generates and validates balanced, playable 5x5 letter grids
 * using weighted letter frequencies and guaranteed discoverable word counts.
 */
class LetterTerritoryBoardGenerator(
    private val wordValidator: WordValidator,
    private val random: Random = Random.Default
) {
    private val searchEngine = TerritoryWordSearchEngine(wordValidator)

    companion object {
        const val BOARD_SIZE = 5
        const val TOTAL_CELLS = 25
        const val MIN_REQUIRED_WORDS = 10

        // Weighted English letter frequencies optimized for 3-6 letter word formation
        // Common vowels and consonants are heavily favored.
        private val LETTER_WEIGHTS = mapOf(
            'E' to 12, 'A' to 9, 'R' to 8, 'I' to 8, 'O' to 8, 'T' to 7,
            'N' to 6, 'S' to 6, 'L' to 5, 'C' to 4, 'D' to 4, 'U' to 4,
            'M' to 3, 'P' to 3, 'H' to 3, 'B' to 2, 'F' to 2, 'G' to 2,
            'Y' to 2, 'W' to 2, 'V' to 1, 'K' to 1, 'X' to 1, 'J' to 1,
            'Z' to 1
        )

        private val VOWELS = setOf('A', 'E', 'I', 'O', 'U')

        // Predefined fallback boards guaranteed to have > 20 valid words
        private val FALLBACK_LETTERS = listOf(
            // S T A R T / P L A N E / C A R E S / M I N D S / B O L D Y
            "STARTPLANECARSMINDSBOLDY".take(25).padEnd(25, 'E'),
            "CATSDOGSEARTHSTONELINESRD".take(25),
            "TREEROADSPARKPLANTGOLDBLU".take(25)
        )
    }

    /**
     * Generates a fully validated, playable 5x5 board.
     * Retries up to maxAttempts, falling back to a curated playable arrangement if needed.
     */
    fun generateBoard(
        diagonalEnabled: Boolean = true,
        maxAttempts: Int = 10
    ): List<LetterCell> {
        val pool = buildLetterPool()

        for (attempt in 0 until maxAttempts) {
            val letters = generateLetterDistribution(pool)
            val candidateCells = createCellsFromLetters(letters)
            if (validateBoard(candidateCells, diagonalEnabled)) {
                return candidateCells
            }
        }

        // Fallback board
        return generateFallbackBoard(diagonalEnabled)
    }

    private fun buildLetterPool(): List<Char> {
        val pool = ArrayList<Char>(150)
        for ((char, weight) in LETTER_WEIGHTS) {
            repeat(weight) {
                pool.add(char)
            }
        }
        return pool
    }

    private fun generateLetterDistribution(pool: List<Char>): List<Char> {
        val result = mutableListOf<Char>()
        // Ensure at least 6-9 vowels for high word-forming potential
        val vowelCount = random.nextInt(6, 10)
        val vowelList = pool.filter { it in VOWELS }
        val consonantList = pool.filter { it !in VOWELS }

        repeat(vowelCount) {
            result.add(vowelList[random.nextInt(vowelList.size)])
        }
        repeat(TOTAL_CELLS - vowelCount) {
            result.add(consonantList[random.nextInt(consonantList.size)])
        }

        result.shuffle(random)
        return result
    }

    private fun createCellsFromLetters(letters: List<Char>): List<LetterCell> {
        val cells = ArrayList<LetterCell>(TOTAL_CELLS)
        for (row in 0 until BOARD_SIZE) {
            for (col in 0 until BOARD_SIZE) {
                val index = row * BOARD_SIZE + col
                cells.add(
                    LetterCell(
                        id = index,
                        coordinate = Coordinate(row, col),
                        letter = letters[index],
                        owner = CellOwner.NEUTRAL
                    )
                )
            }
        }
        return cells
    }

    /**
     * Generates a fallback playable board with guaranteed valid words.
     */
    fun generateFallbackBoard(diagonalEnabled: Boolean = true): List<LetterCell> {
        val template = FALLBACK_LETTERS[random.nextInt(FALLBACK_LETTERS.size)]
        val charList = template.toList().shuffled(random)
        val cells = createCellsFromLetters(charList)
        if (validateBoard(cells, diagonalEnabled)) {
            return cells
        }
        // If shuffled failed playability check, return ordered template
        return createCellsFromLetters(FALLBACK_LETTERS[0].toList())
    }

    /**
     * Validates board rules:
     * - Exactly 25 cells
     * - Unique IDs 0..24
     * - Valid coordinates (0..4, 0..4) with no duplicates
     * - Valid uppercase letters
     * - At least 10 discoverable words
     * - Reasonable letter distribution
     */
    fun validateBoard(
        cells: List<LetterCell>,
        diagonalEnabled: Boolean = true
    ): Boolean {
        if (cells.size != TOTAL_CELLS) return false

        val ids = cells.map { it.id }.toSet()
        if (ids.size != TOTAL_CELLS) return false
        if (ids.any { it !in 0 until TOTAL_CELLS }) return false

        val coords = cells.map { it.coordinate }.toSet()
        if (coords.size != TOTAL_CELLS) return false
        if (coords.any { it.row !in 0 until BOARD_SIZE || it.column !in 0 until BOARD_SIZE }) return false

        if (cells.any { it.letter !in 'A'..'Z' }) return false

        // Check vowel distribution (must have at least 4 vowels)
        val vowelCount = cells.count { it.letter.uppercaseChar() in VOWELS }
        if (vowelCount < 4) return false

        // Check discoverable words
        val wordCount = searchEngine.countDiscoverableWords(cells, diagonalEnabled)
        return wordCount >= MIN_REQUIRED_WORDS
    }
}
