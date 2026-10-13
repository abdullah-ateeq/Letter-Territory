package com.example.letterterritory.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.letterterritory.model.LetterCell

/**
 * 5x5 interactive board displaying 25 LetterTiles and a Canvas overlay
 * drawing the smooth, animated connection path across selected cells.
 */
@Composable
fun BoardView(
    cells: List<LetterCell>,
    selectedCellIds: List<Int>,
    newlyCapturedIds: Set<Int>,
    showCoordinates: Boolean,
    animationsEnabled: Boolean,
    onCellClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pathAnimAlpha by animateFloatAsState(
        targetValue = if (selectedCellIds.isNotEmpty()) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "pathAlpha"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        val totalWidth = constraints.maxWidth.toFloat()
        val totalHeight = constraints.maxHeight.toFloat()
        val cellSize = totalWidth / 5f

        // Cell Map for rapid lookup
        val cellMap = cells.associateBy { it.id }

        // Grid layout (5 rows x 5 columns)
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (row in 0 until 5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0 until 5) {
                        val cellId = row * 5 + col
                        val cell = cellMap[cellId] ?: LetterCell(
                            id = cellId,
                            coordinate = com.example.letterterritory.model.Coordinate(row, col),
                            letter = ' '
                        )

                        val isSelected = selectedCellIds.contains(cellId)
                        val selectionIndex = if (isSelected) selectedCellIds.indexOf(cellId) + 1 else null
                        val isCaptured = newlyCapturedIds.contains(cellId)

                        LetterTile(
                            cell = cell,
                            isSelected = isSelected,
                            selectionOrder = selectionIndex,
                            isNewlyCaptured = isCaptured,
                            showCoordinates = showCoordinates,
                            animationsEnabled = animationsEnabled,
                            onClick = { onCellClicked(cellId) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Overlay Canvas for drawing the connecting path line
        if (selectedCellIds.size >= 2 && pathAnimAlpha > 0f) {
            val primaryColor = MaterialTheme.colorScheme.primary

            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path()

                fun getCenter(id: Int): Offset {
                    val row = id / 5
                    val col = id % 5
                    // Calculate center based on uniform cell distribution
                    val x = (col + 0.5f) * (size.width / 5f)
                    val y = (row + 0.5f) * (size.height / 5f)
                    return Offset(x, y)
                }

                val firstPoint = getCenter(selectedCellIds.first())
                path.moveTo(firstPoint.x, firstPoint.y)

                for (i in 1 until selectedCellIds.size) {
                    val pt = getCenter(selectedCellIds[i])
                    path.lineTo(pt.x, pt.y)
                }

                // Draw outer subtle glow / shadow line
                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = 0.25f * pathAnimAlpha),
                    style = Stroke(
                        width = 12.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw solid center connecting line
                drawPath(
                    path = path,
                    color = primaryColor.copy(alpha = 0.85f * pathAnimAlpha),
                    style = Stroke(
                        width = 4.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw small anchor dots at vertices
                for (id in selectedCellIds) {
                    val pt = getCenter(id)
                    drawCircle(
                        color = primaryColor.copy(alpha = pathAnimAlpha),
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                }
            }
        }
    }
}
