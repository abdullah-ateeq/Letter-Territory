package com.example.letterterritory.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.letterterritory.data.GameSettings
import com.example.letterterritory.domain.WordValidator
import com.example.letterterritory.model.GamePhase
import com.example.letterterritory.model.LetterTerritoryGameState
import com.example.letterterritory.model.TurnTimerOption
import com.example.letterterritory.ui.components.BoardView
import com.example.letterterritory.ui.components.TerritoryBar
import com.example.letterterritory.ui.components.WordHistoryBottomSheet

/**
 * Main gameplay screen for Letter Territory.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    gameState: LetterTerritoryGameState,
    settings: GameSettings,
    isCpuThinking: Boolean,
    isProcessingInput: Boolean,
    timerSeconds: Int,
    onCellClicked: (Int) -> Unit,
    onClaimClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onPassClicked: () -> Unit,
    onRestartClicked: () -> Unit,
    onGameOver: () -> Unit
) {
    var showRestartDialog by remember { mutableStateOf(false) }
    var showWordHistory by remember { mutableStateOf(false) }
    val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Check game over
    LaunchedEffect(gameState.phase) {
        if (gameState.phase == GamePhase.GAME_OVER) {
            onGameOver()
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(text = "Restart Battle?") },
            text = { Text(text = "Are you sure you want to restart this territory match with a new board?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        onRestartClicked()
                    }
                ) {
                    Text(text = "Restart", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    if (showWordHistory) {
        WordHistoryBottomSheet(
            playerWords = gameState.playerWords,
            cpuWords = gameState.cpuWords,
            sheetState = historySheetState,
            onDismiss = { showWordHistory = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Header: Title, Round, Restart Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "LETTER TERRITORY",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "ROUND ${gameState.round} / ${gameState.totalRounds}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (settings.turnTimer != TurnTimerOption.OFF && gameState.phase == GamePhase.PLAYER_TURN) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (timerSeconds <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${timerSeconds}s",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (timerSeconds <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                IconButton(
                    onClick = {
                        if (settings.confirmRestart) {
                            showRestartDialog = true
                        } else {
                            onRestartClicked()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restart game",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Territory Scoreboard (YOU, CPU, NEUTRAL)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScoreColumn(
                        label = "YOU",
                        count = gameState.playerTerritory,
                        color = MaterialTheme.colorScheme.primary
                    )
                    ScoreColumn(
                        label = "NEUTRAL",
                        count = gameState.neutralCells,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ScoreColumn(
                        label = "CPU",
                        count = gameState.cpuTerritory,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Segmented Territory Bar
                TerritoryBar(
                    playerCount = gameState.playerTerritory,
                    cpuCount = gameState.cpuTerritory,
                    neutralCount = gameState.neutralCells
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status / Challenge Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isCpuThinking) MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                )
                .padding(vertical = 6.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isCpuThinking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CPU TURN — Finding territory...",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = "YOUR TURN — CLAIM TERRITORY",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 5x5 Board
        BoardView(
            cells = gameState.cells,
            selectedCellIds = gameState.selectedCellIds,
            newlyCapturedIds = gameState.newlyCapturedIds,
            showCoordinates = settings.showCoordinates,
            animationsEnabled = settings.animationsEnabled,
            onCellClicked = onCellClicked
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Feedback banner (if enabled and present)
        AnimatedVisibility(
            visible = settings.showWordFeedback && !gameState.feedbackMessage.isNullOrEmpty(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (gameState.lastMoveValid == true) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gameState.feedbackMessage ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (gameState.lastMoveValid == true) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Current Word Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CURRENT WORD",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (gameState.currentWord.isEmpty()) "—"
                        else gameState.currentWord.map { "$it " }.joinToString("").trim(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${gameState.selectedCellIds.size} / ${WordValidator.MAX_WORD_LENGTH} LETTERS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Controls: CLAIM, CLEAR, PASS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onClaimClicked,
                enabled = gameState.selectedCellIds.size >= WordValidator.MIN_WORD_LENGTH &&
                        !isCpuThinking && !isProcessingInput,
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CLAIM",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            OutlinedButton(
                onClick = onClearClicked,
                enabled = gameState.selectedCellIds.isNotEmpty() && !isCpuThinking && !isProcessingInput,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "CLEAR",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = onPassClicked,
                enabled = !isCpuThinking && !isProcessingInput && gameState.phase == GamePhase.PLAYER_TURN,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "PASS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recent Words Summary and Words Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val lastPlayer = gameState.playerWords.lastOrNull()?.let { "${it.word} +${it.capturedCount}" } ?: "None"
            val lastCpu = gameState.cpuWords.lastOrNull()?.let { "${it.word} +${it.capturedCount}" } ?: "None"

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "YOU: $lastPlayer · CPU: $lastCpu",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(
                onClick = { showWordHistory = true }
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "WORDS (${gameState.playerWords.size + gameState.cpuWords.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ScoreColumn(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = color
        )
    }
}
