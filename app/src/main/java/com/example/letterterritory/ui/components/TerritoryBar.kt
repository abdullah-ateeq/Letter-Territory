package com.example.letterterritory.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Clean, compact segmented territory bar displaying relative board control:
 * Player %, CPU %, and Neutral %.
 */
@Composable
fun TerritoryBar(
    playerCount: Int,
    cpuCount: Int,
    neutralCount: Int,
    modifier: Modifier = Modifier,
    totalCells: Int = 25
) {
    val playerFraction = (playerCount.toFloat() / totalCells).coerceIn(0f, 1f)
    val cpuFraction = (cpuCount.toFloat() / totalCells).coerceIn(0f, 1f)
    val neutralFraction = (neutralCount.toFloat() / totalCells).coerceIn(0f, 1f)

    val animatedPlayer by animateFloatAsState(
        targetValue = playerFraction,
        animationSpec = tween(durationMillis = 400),
        label = "animPlayer"
    )
    val animatedCpu by animateFloatAsState(
        targetValue = cpuFraction,
        animationSpec = tween(durationMillis = 400),
        label = "animCpu"
    )
    val animatedNeutral by animateFloatAsState(
        targetValue = neutralFraction,
        animationSpec = tween(durationMillis = 400),
        label = "animNeutral"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Percentage labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOU ${(playerFraction * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "NEUTRAL ${(neutralFraction * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "CPU ${(cpuFraction * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Horizontal Segmented Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            if (animatedPlayer > 0f) {
                Box(
                    modifier = Modifier
                        .weight(animatedPlayer.coerceAtLeast(0.001f))
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            if (animatedNeutral > 0f) {
                Box(
                    modifier = Modifier
                        .weight(animatedNeutral.coerceAtLeast(0.001f))
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                )
            }
            if (animatedCpu > 0f) {
                Box(
                    modifier = Modifier
                        .weight(animatedCpu.coerceAtLeast(0.001f))
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.secondary)
                )
            }
        }
    }
}
