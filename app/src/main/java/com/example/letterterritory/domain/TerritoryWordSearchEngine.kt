package com.example.letterterritory.domain

import com.example.letterterritory.model.LetterCell
import com.example.letterterritory.model.Side
import com.example.letterterritory.model.TerritoryMove

/**
 * High-performance search engine to discover all valid words and paths
 * on the 5x5 board using Trie prefix pruning and DFS backtracking.
 */
class TerritoryWordSearchEngine(
    private val wordValidator: WordValidator
) {

    /**
     * Finds all valid candidate moves on the board.
     *
     * @param cells The 25 board cells.
     * @param usedWords Set of words already claimed in this game.
     * @param diagonalEnabled Adjacency mode (8 directions vs 4 directions).
     * @param side Participant searching for moves (PLAYER or CPU).
     * @param requireNeutralCell Whether candidate must capture >= 1 neutral cell.
     * @return List of discovered TerritoryMove candidates.
     */
    fun findCandidates(
        cells: List<LetterCell>,
        usedWords: Set<String>,
        diagonalEnabled: Boolean,
        side: Side = Side.CPU,
        requireNeutralCell: Boolean = true
    ): List<TerritoryMove> {
        val cellMap = cells.associateBy { it.id }
        val candidates = mutableListOf<TerritoryMove>()
        val visitedIds = BooleanArray(25)

        // Precompute neighbors for all 25 cells to optimize DFS performance
        val neighbors = Array(25) { i ->
            val cell = cellMap[i] ?: return@Array intArrayOf()
            val list = mutableListOf<Int>()
            for (j in 0 until 25) {
                if (i != j) {
                    val other = cellMap[j] ?: continue
                    if (wordValidator.areAdjacent(cell.coordinate, other.coordinate, diagonalEnabled)) {
                        list.add(j)
                    }
                }
            }
            list.toIntArray()
        }

        val path = ArrayList<Int>(WordValidator.MAX_WORD_LENGTH)
        val wordChars = CharArray(WordValidator.MAX_WORD_LENGTH)

        fun dfs(currentId: Int, depth: Int) {
            val cell = cellMap[currentId] ?: return
            path.add(currentId)
            visitedIds[currentId] = true
            wordChars[depth] = cell.letter.uppercaseChar()
            val currentWord = String(wordChars, 0, depth + 1)

            // Trie prefix pruning
            if (wordValidator.isPrefix(currentWord)) {
                // If word is valid length (3..6)
                if (depth + 1 >= WordValidator.MIN_WORD_LENGTH) {
                    if (!usedWords.contains(currentWord) && wordValidator.isWordInDictionary(currentWord)) {
                        val captured = wordValidator.calculateCapture(path, cells, side)
                        if (!requireNeutralCell || captured.isNotEmpty()) {
                            candidates.add(
                                TerritoryMove(
                                    word = currentWord,
                                    cellIds = ArrayList(path),
                                    capturedCellIds = captured,
                                    capturedCount = captured.size
                                )
                            )
                        }
                    }
                }

                // Continue expanding path up to MAX_WORD_LENGTH
                if (depth + 1 < WordValidator.MAX_WORD_LENGTH) {
                    val nextNeighbors = neighbors[currentId]
                    for (nextId in nextNeighbors) {
                        if (!visitedIds[nextId]) {
                            dfs(nextId, depth + 1)
                        }
                    }
                }
            }

            // Backtrack
            visitedIds[currentId] = false
            path.removeAt(path.size - 1)
        }

        // Start search from each cell on the board
        for (startId in 0 until 25) {
            dfs(startId, 0)
        }

        return candidates
    }

    /**
     * Returns count of discoverable words in the board dictionary (used for board playability check).
     */
    fun countDiscoverableWords(
        cells: List<LetterCell>,
        diagonalEnabled: Boolean
    ): Int {
        val candidates = findCandidates(
            cells = cells,
            usedWords = emptySet(),
            diagonalEnabled = diagonalEnabled,
            side = Side.PLAYER,
            requireNeutralCell = false
        )
        return candidates.map { it.word }.distinct().size
    }
}
