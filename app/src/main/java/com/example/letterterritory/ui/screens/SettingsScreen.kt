package com.example.letterterritory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.letterterritory.data.GameSettings
import com.example.letterterritory.data.ThemeMode
import com.example.letterterritory.model.CpuDifficulty
import com.example.letterterritory.model.TurnTimerOption

/**
 * Settings Screen for managing preferences and resetting statistics/settings.
 */
@Composable
fun SettingsScreen(
    settings: GameSettings,
    onThemeChanged: (ThemeMode) -> Unit,
    onDynamicColorChanged: (Boolean) -> Unit,
    onCpuDifficultyChanged: (CpuDifficulty) -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onHapticsChanged: (Boolean) -> Unit,
    onAnimationsChanged: (Boolean) -> Unit,
    onDiagonalChanged: (Boolean) -> Unit,
    onTimerChanged: (TurnTimerOption) -> Unit,
    onShowCoordinatesChanged: (Boolean) -> Unit,
    onShowFeedbackChanged: (Boolean) -> Unit,
    onConfirmRestartChanged: (Boolean) -> Unit,
    onResetSettings: () -> Unit,
    onResetStatistics: () -> Unit
) {
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showResetStatsDialog by remember { mutableStateOf(false) }

    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = { Text(text = "Reset Settings?") },
            text = { Text(text = "Are you sure you want to reset all gameplay settings to default values?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetSettingsDialog = false
                        onResetSettings()
                    }
                ) {
                    Text(text = "Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    if (showResetStatsDialog) {
        AlertDialog(
            onDismissRequest = { showResetStatsDialog = false },
            title = { Text(text = "Reset Statistics?") },
            text = { Text(text = "Are you sure you want to clear all your lifetime games, records, and win streaks?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetStatsDialog = false
                        onResetStatistics()
                    }
                ) {
                    Text(text = "Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetStatsDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance Section
        SettingsSectionHeader(title = "APPEARANCE")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Theme Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeChanged(mode) },
                            label = { Text(mode.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                SwitchRow(
                    label = "Dynamic Color",
                    subtitle = "Adapt UI colors to device wallpaper",
                    checked = settings.dynamicColor,
                    onCheckedChange = onDynamicColorChanged
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                SwitchRow(
                    label = "UI Animations",
                    subtitle = "Smooth line transitions and capture pulses",
                    checked = settings.animationsEnabled,
                    onCheckedChange = onAnimationsChanged
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gameplay Section
        SettingsSectionHeader(title = "GAMEPLAY")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "CPU Difficulty",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CpuDifficulty.entries.forEach { diff ->
                        FilterChip(
                            selected = settings.cpuDifficulty == diff,
                            onClick = { onCpuDifficultyChanged(diff) },
                            label = { Text(diff.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                SwitchRow(
                    label = "Diagonal Movement",
                    subtitle = "Allow connecting cells diagonally (8 directions)",
                    checked = settings.diagonalMovement,
                    onCheckedChange = onDiagonalChanged
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Turn Timer",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TurnTimerOption.entries.forEach { opt ->
                        FilterChip(
                            selected = settings.turnTimer == opt,
                            onClick = { onTimerChanged(opt) },
                            label = {
                                Text(
                                    when (opt) {
                                        TurnTimerOption.OFF -> "OFF"
                                        TurnTimerOption.TEN_SECONDS -> "10 SEC"
                                        TurnTimerOption.FIFTEEN_SECONDS -> "15 SEC"
                                    }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                SwitchRow(
                    label = "Show Coordinates",
                    subtitle = "Display subtle A1..E5 labels on cells",
                    checked = settings.showCoordinates,
                    onCheckedChange = onShowCoordinatesChanged
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                SwitchRow(
                    label = "Word Feedback Banners",
                    subtitle = "Display validity feedback after submissions",
                    checked = settings.showWordFeedback,
                    onCheckedChange = onShowFeedbackChanged
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                SwitchRow(
                    label = "Confirm Restart",
                    subtitle = "Ask for confirmation before restarting a match",
                    checked = settings.confirmRestart,
                    onCheckedChange = onConfirmRestartChanged
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Feedback & Audio
        SettingsSectionHeader(title = "AUDIO & HAPTICS")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SwitchRow(
                    label = "Sound Effects",
                    subtitle = "Tile taps, valid claims, and capture cues",
                    checked = settings.soundEnabled,
                    onCheckedChange = onSoundChanged
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                SwitchRow(
                    label = "Haptic Vibration",
                    subtitle = "Tactile response on letter selection and claims",
                    checked = settings.hapticsEnabled,
                    onCheckedChange = onHapticsChanged
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reset Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = { showResetSettingsDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Reset Settings")
            }

            OutlinedButton(
                onClick = { showResetStatsDialog = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Reset Statistics")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun SwitchRow(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
