package com.example.letterterritory.domain

import com.example.letterterritory.model.CellOwner
import com.example.letterterritory.model.CpuDifficulty
import com.example.letterterritory.model.LetterCell
import com.example.letterterritory.model.Side
import com.example.letterterritory.model.TerritoryMove
import kotlin.random.Random

/**
 * CPU artificial intelligence strategy for Letter Territory.
 * Evaluates candidate moves, optimizes territory capture, and adapts
 * play style based on difficulty (Easy, Normal, Hard).
 */
class LetterTerritoryCpuStrategy(
    private val wordValidator: WordValidator,
    private val searchEngine: TerritoryWordSearchEngine = TerritoryWordSearchEngine(wordValidator),
    private val random: Random = Random.Default
) {

    /**
     * Selects the best move for the CPU, or null if the CPU passes.
     */
    fun selectMove(
        cells: List<LetterCell>,
        usedWords: Set<String>,
        diagonalEnabled: Boolean,
        difficulty: CpuDifficulty
    ): TerritoryMove? {
        val candidates = searchEngine.findCandidates(
            cells = cells,
            usedWords = usedWords,
            diagonalEnabled = diagonalEnabled,
            side = Side.CPU,
            requireNeutralCell = true
        )

        if (candidates.isEmpty()) {
            return null
        }

        return when (difficulty) {
            CpuDifficulty.EASY -> selectEasyMove(candidates)
            CpuDifficulty.NORMAL -> selectNormalMove(candidates)
            CpuDifficulty.HARD -> selectHardMove(candidates, cells, diagonalEnabled)
        }
    }

    /**
     * Easy CPU:
     * - 20% chance of picking a weaker/random candidate or passing
     * - Prefers 3-4 letter words
     * - Does not optimize capture count
     */
    private fun selectEasyMove(candidates: List<TerritoryMove>): TerritoryMove? {
        // 10% chance to pass even if candidates exist
        if (random.nextFloat() < 0.10f && candidates.size > 2) {
            return null
        }

        // Filter or bias toward shorter words (3..4 letters)
        val shortCandidates = candidates.filter { it.word.length <= 4 }
        val pool = if (shortCandidates.isNotEmpty() && random.nextFloat() < 0.70f) {
            shortCandidates
        } else {
            candidates
        }

        return pool[random.nextInt(pool.size)]
    }

    /**
     * Normal CPU:
     * - Finds valid words
     * - Ranks primarily by capturedCount
     * - Prefers 4-5 letter words
     * - Rarely passes
     */
    private fun selectNormalMove(candidates: List<TerritoryMove>): TerritoryMove {
        val sorted = candidates.sortedWith(
            compareByDescending<TerritoryMove> { it.capturedCount }
                .thenByDescending { it.word.length }
        )

        // Pick among top 3 highest capture candidates to maintain realistic variety
        val topTierCount = minOf(3, sorted.size)
        val index = random.nextInt(topTierCount)
        return sorted[index]
    }

    /**
     * Hard CPU:
     * - Thoroughly maximizes territory capture
     * - Tiebreakers:
     *   1. capturedCount (highest neutral cell gain)
     *   2. word length (longer word if capture is equal)
     *   3. territory connectivity to existing CPU cells (adjacent cells to CPU territory)
     *   4. expansion toward open neutral regions
     */
    private fun selectHardMove(
        candidates: List<TerritoryMove>,
        cells: List<LetterCell>,
        diagonalEnabled: Boolean
    ): TerritoryMove {
        val cellMap = cells.associateBy { it.id }
        val cpuOwnedCells = cells.filter { it.owner == CellOwner.CPU }

        fun scoreCandidate(candidate: TerritoryMove): Double {
            var score = candidate.capturedCount * 100.0 + candidate.word.length * 5.0

            // Connectivity bonus: if any captured cell is adjacent to existing CPU territory
            if (cpuOwnedCells.isNotEmpty()) {
                var adjacentToCpuCount = 0
                for (capId in candidate.capturedCellIds) {
                    val capCell = cellMap[capId] ?: continue
                    val touchesCpu = cpuOwnedCells.any { cpuCell ->
                        wordValidator.areAdjacent(capCell.coordinate, cpuCell.coordinate, diagonalEnabled)
                    }
                    if (touchesCpu) {
                        adjacentToCpuCount++
                    }
                }
                score += adjacentToCpuCount * 10.0
            }

            return score
        }

        return candidates.maxByOrNull { scoreCandidate(it) } ?: candidates.first()
    }
}
