package com.example.letterterritory.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.letterterritory.model.CellOwner
import com.example.letterterritory.model.LetterCell

/**
 * LetterTile represents a single cell on the 5x5 Letter Territory grid.
 * Displays high-contrast letter typography, clear ownership styling,
 * optional coordinate badge, selection feedback, and accessibility semantics.
 */
@Composable
fun LetterTile(
    cell: LetterCell,
    isSelected: Boolean,
    selectionOrder: Int?, // 1-based index if selected in path
    isNewlyCaptured: Boolean,
    showCoordinates: Boolean,
    animationsEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Dynamic scale for selection or newly captured pulse
    val targetScale = when {
        isSelected -> 1.08f
        isNewlyCaptured -> 1.05f
        else -> 1.0f
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (animationsEnabled) targetScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tileScale"
    )

    // Colors according to CellOwner
    val targetContainerColor = when (cell.owner) {
        CellOwner.PLAYER -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        CellOwner.CPU -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        CellOwner.NEUTRAL -> MaterialTheme.colorScheme.surfaceContainer
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(durationMillis = if (animationsEnabled) 350 else 0),
        label = "tileColor"
    )

    // Border based on selection and ownership
    val borderStroke = when {
        isSelected -> BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
        cell.owner == CellOwner.PLAYER -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        cell.owner == CellOwner.CPU -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f))
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    }

    val elevation = if (isSelected) 6.dp else 1.dp

    // Accessibility description
    val ownerDesc = when (cell.owner) {
        CellOwner.PLAYER -> "Player territory"
        CellOwner.CPU -> "CPU territory"
        CellOwner.NEUTRAL -> "Neutral"
    }
    val selDesc = if (isSelected) ", selected in path (letter ${selectionOrder ?: ""})" else ""
    val fullDesc = "Letter ${cell.letter} at ${cell.coordinate.label}, $ownerDesc$selDesc"

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .scale(animatedScale)
            .semantics { contentDescription = fullDesc }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Optional Coordinate Label in top-left
            if (showCoordinates) {
                Text(
                    text = cell.coordinate.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 5.dp, top = 3.dp)
                )
            }

            // Small Territory Owner Marker in top-right
            when (cell.owner) {
                CellOwner.PLAYER -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 5.dp, top = 5.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                CellOwner.CPU -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 5.dp, top = 5.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                }
                CellOwner.NEUTRAL -> Unit
            }

            // Center Letter Typography - Dominant and bold
            Text(
                text = cell.letter.toString(),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    cell.owner == CellOwner.PLAYER -> MaterialTheme.colorScheme.onSurface
                    cell.owner == CellOwner.CPU -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            // Selection order indicator at bottom-right
            if (selectionOrder != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 5.dp, bottom = 4.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectionOrder.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
