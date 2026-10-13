package com.example.letterterritory.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Animated splash screen showcasing the 5x5 grid, C-A-T connection,
 * territory capture transition, and app title. Duration ~1.4 seconds.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var step by remember { mutableStateOf(0) }
    val scaleAnim = remember { Animatable(0.85f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = tween(600))
        step = 1 // Grid visible
        delay(350)
        step = 2 // CAT connected
        delay(400)
        step = 3 // Claimed to Player Territory
        delay(650)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Decorative 5x5 Mini Grid
            Column(
                modifier = Modifier
                    .scale(scaleAnim.value)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val decorativeLetters = listOf(
                    listOf('S', 'T', 'O', 'N', 'E'),
                    listOf('C', 'A', 'T', 'R', 'D'),
                    listOf('P', 'L', 'A', 'N', 'T'),
                    listOf('G', 'R', 'I', 'D', 'S'),
                    listOf('B', 'O', 'L', 'D', 'Y')
                )

                for (r in 0 until 5) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (c in 0 until 5) {
                            val char = decorativeLetters[r][c]
                            val isCatCell = (r == 1 && c in 0..2)
                            val isClaimed = isCatCell && step >= 3
                            val isSelected = isCatCell && step == 2

                            val bgColor = when {
                                isClaimed -> MaterialTheme.colorScheme.primaryContainer
                                isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                else -> MaterialTheme.colorScheme.surfaceContainer
                            }

                            val borderColor = when {
                                isClaimed || isSelected -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(bgColor)
                                    .border(
                                        width = if (isCatCell) 1.5.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(6.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isCatCell) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCatCell) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title & Tagline
            Text(
                text = "LETTER",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "TERRITORY",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"Build words. Claim ground.\"",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
