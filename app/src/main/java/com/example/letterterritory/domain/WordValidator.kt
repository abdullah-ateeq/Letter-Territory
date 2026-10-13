package com.example.letterterritory.domain

import android.content.Context
import com.example.letterterritory.model.CellOwner
import com.example.letterterritory.model.Coordinate
import com.example.letterterritory.model.LetterCell
import com.example.letterterritory.model.Side
import com.example.letterterritory.model.SubmissionResult
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import kotlin.math.abs

/**
 * Validates word paths, dictionary entries, length constraints,
 * and territory capture requirements.
 */
class WordValidator(
    initialWords: Collection<String>? = null
) {
    private val trie = Trie()
    private val dictionarySet = HashSet<String>()

    init {
        initialWords?.let { addWords(it) }
    }

    val wordCount: Int
        get() = dictionarySet.size

    val trieInstance: Trie
        get() = trie

    fun addWords(words: Collection<String>) {
        for (w in words) {
            val normalized = normalizeWord(w)
            if (isValidLength(normalized)) {
                dictionarySet.add(normalized)
                trie.insert(normalized)
            }
        }
    }

    fun loadFromStream(inputStream: InputStream) {
        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
            reader.lineSequence().forEach { line ->
                val normalized = normalizeWord(line)
                if (isValidLength(normalized)) {
                    dictionarySet.add(normalized)
                    trie.insert(normalized)
                }
            }
        }
    }

    fun normalizeWord(word: String): String {
        return word.trim().uppercase()
    }

    fun isValidLength(word: String): Boolean {
        return word.length in MIN_WORD_LENGTH..MAX_WORD_LENGTH
    }

    fun isWordInDictionary(word: String): Boolean {
        val normalized = normalizeWord(word)
        return dictionarySet.contains(normalized)
    }

    fun isPrefix(prefix: String): Boolean {
        return trie.isPrefix(normalizeWord(prefix))
    }

    /**
     * Builds uppercase word string from selected cell IDs in order.
     */
    fun buildWord(cells: List<LetterCell>, selectedIds: List<Int>): String {
        val cellMap = cells.associateBy { it.id }
        val sb = StringBuilder()
        for (id in selectedIds) {
            cellMap[id]?.let { sb.append(it.letter.uppercaseChar()) }
        }
        return sb.toString()
    }

    /**
     * Checks if two board coordinates are adjacent.
     * When diagonal is enabled: 8 neighbors (abs(dr) <= 1 && abs(dc) <= 1 && dr*dr + dc*dc > 0).
     * When diagonal is disabled: 4 orthogonal neighbors (abs(dr) + abs(dc) == 1).
     */
    fun areAdjacent(first: Coordinate, second: Coordinate, diagonalEnabled: Boolean): Boolean {
        val dr = abs(first.row - second.row)
        val dc = abs(first.column - second.column)
        if (dr == 0 && dc == 0) return false

        return if (diagonalEnabled) {
            dr <= 1 && dc <= 1
        } else {
            dr + dc == 1
        }
    }

    /**
     * Validates whether a sequence of cell IDs forms a valid path on the board:
     * - Length between 3 and 6
     * - No duplicate cell IDs
     * - All IDs exist on the board
     * - Consecutive cells are adjacent
     */
    fun isValidPath(
        cellIds: List<Int>,
        cells: List<LetterCell>,
        diagonalEnabled: Boolean
    ): Boolean {
        if (cellIds.size < MIN_WORD_LENGTH || cellIds.size > MAX_WORD_LENGTH) {
            return false
        }
        if (cellIds.distinct().size != cellIds.size) {
            return false
        }

        val cellMap = cells.associateBy { it.id }
        for (id in cellIds) {
            if (!cellMap.containsKey(id)) return false
        }

        for (i in 0 until cellIds.size - 1) {
            val current = cellMap[cellIds[i]] ?: return false
            val next = cellMap[cellIds[i + 1]] ?: return false
            if (!areAdjacent(current.coordinate, next.coordinate, diagonalEnabled)) {
                return false
            }
        }

        return true
    }

    /**
     * Calculates which neutral cells would be captured by this word.
     * Only cells currently owned by NEUTRAL are captured!
     * Already-owned player or CPU cells are not captured.
     */
    fun calculateCapture(
        selectedCellIds: List<Int>,
        cells: List<LetterCell>,
        side: Side
    ): List<Int> {
        val cellMap = cells.associateBy { it.id }
        return selectedCellIds.filter { id ->
            cellMap[id]?.owner == CellOwner.NEUTRAL
        }
    }

    /**
     * Validates a word submission attempt according to all game rules.
     */
    fun validateSubmission(
        selectedCellIds: List<Int>,
        cells: List<LetterCell>,
        usedWords: Set<String>,
        diagonalEnabled: Boolean,
        side: Side = Side.PLAYER
    ): SubmissionResult {
        if (selectedCellIds.size < MIN_WORD_LENGTH) {
            return SubmissionResult.TOO_SHORT
        }
        if (selectedCellIds.size > MAX_WORD_LENGTH) {
            return SubmissionResult.TOO_LONG
        }

        if (!isValidPath(selectedCellIds, cells, diagonalEnabled)) {
            return SubmissionResult.INVALID_PATH
        }

        val word = buildWord(cells, selectedCellIds)
        if (usedWords.contains(word)) {
            return SubmissionResult.WORD_ALREADY_USED
        }

        if (!isWordInDictionary(word)) {
            return SubmissionResult.INVALID_WORD
        }

        val capture = calculateCapture(selectedCellIds, cells, side)
        if (capture.isEmpty()) {
            return SubmissionResult.NO_TERRITORY
        }

        return SubmissionResult.VALID
    }

    companion object {
        const val MIN_WORD_LENGTH = 3
        const val MAX_WORD_LENGTH = 6

        @Volatile
        private var sharedValidator: WordValidator? = null

        /**
         * Singleton loader for production Android context or JVM test environment.
         */
        fun getOrCreate(context: Context? = null): WordValidator {
            return sharedValidator ?: synchronized(this) {
                sharedValidator ?: createInstance(context).also { sharedValidator = it }
            }
        }

        private fun createInstance(context: Context?): WordValidator {
            val validator = WordValidator()
            var loaded = false

            // Try Android assets
            if (context != null) {
                try {
                    context.assets.open("word_list.txt").use { stream ->
                        validator.loadFromStream(stream)
                        loaded = true
                    }
                } catch (_: Exception) {
                }
            }

            // Fallback to ClassLoader resource if asset failed or running in JVM test
            if (!loaded || validator.wordCount == 0) {
                try {
                    val stream = WordValidator::class.java.classLoader?.getResourceAsStream("word_list.txt")
                        ?: ClassLoader.getSystemResourceAsStream("word_list.txt")
                    if (stream != null) {
                        stream.use { validator.loadFromStream(it) }
                        loaded = true
                    }
                } catch (_: Exception) {
                }
            }

            // If still not loaded, populate essential fallback words
            if (validator.wordCount == 0) {
                validator.addWords(DEFAULT_FALLBACK_WORDS)
            }

            return validator
        }

        val DEFAULT_FALLBACK_WORDS = listOf(
            "CAT", "CAR", "CARE", "CARD", "CART", "ROAD", "TREE", "PLANT", "STONE",
            "PLAY", "GAME", "CLAIM", "WORD", "GRID", "BOLD", "BLUE", "RED", "GOLD",
            "WALK", "TALK", "LINE", "PINE", "LION", "BEAR", "DEER", "DUCK", "HAND",
            "HEAD", "FOOT", "MIND", "SOUL", "BODY", "HOME", "ROOM", "WALL", "DOOR",
            "ROOF", "TOWN", "CITY", "HERO", "KING", "QUEEN", "RING", "SONG", "TONE",
            "HOPE", "LOVE", "LIFE", "TIME", "YEAR", "HOUR", "DAYS", "WEEK", "STAR",
            "MOON", "SUN", "WIND", "FIRE", "SHIP", "BOAT", "DARK", "DEEP", "FREE",
            "SOFT", "WARM", "COLD", "RICH", "POOR", "PURE", "REAL", "SAFE", "TRUE",
            "WISE", "CALM", "EASY", "FAIR", "FINE", "FIRM", "GOOD", "HARD", "HIGH",
            "JUST", "KEEN", "KIND", "LATE", "NEAT", "PALE", "FAST", "SLOW", "STOP"
        )
    }
}
