package com.example.letterterritory.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.letterterritory.ui.screens.AboutScreen
import com.example.letterterritory.ui.screens.GameScreen
import com.example.letterterritory.ui.screens.HomeScreen
import com.example.letterterritory.ui.screens.ResultScreen
import com.example.letterterritory.ui.screens.SettingsScreen
import com.example.letterterritory.ui.screens.SplashScreen
import com.example.letterterritory.viewmodel.LetterTerritoryViewModel

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Splash : Screen("splash")
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Game : Screen("game")
    object Result : Screen("result")
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object About : Screen("about", "About", Icons.Default.Info)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Settings,
    Screen.About
)

@Composable
fun LetterTerritoryNavHost(
    viewModel: LetterTerritoryViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val gameState by viewModel.gameState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val isCpuThinking by viewModel.isCpuThinking.collectAsState()
    val isProcessingInput by viewModel.isProcessingInput.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Settings.route,
        Screen.About.route
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                screen.icon?.let { Icon(imageVector = it, contentDescription = screen.title) }
                            },
                            label = { Text(screen.title ?: "") }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    settings = settings,
                    statistics = statistics,
                    onPlayClicked = {
                        viewModel.restartGame()
                        navController.navigate(Screen.Game.route)
                    },
                    onDifficultySelected = { diff ->
                        viewModel.setCpuDifficulty(diff)
                    },
                    onSettingsClicked = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onAboutClicked = {
                        navController.navigate(Screen.About.route)
                    }
                )
            }

            composable(Screen.Game.route) {
                GameScreen(
                    gameState = gameState,
                    settings = settings,
                    isCpuThinking = isCpuThinking,
                    isProcessingInput = isProcessingInput,
                    timerSeconds = timerSeconds,
                    onCellClicked = { id -> viewModel.onCellClicked(id) },
                    onClaimClicked = { viewModel.onClaimClicked() },
                    onClearClicked = { viewModel.onClearClicked() },
                    onPassClicked = { viewModel.onPassClicked() },
                    onRestartClicked = { viewModel.restartGame() },
                    onGameOver = {
                        navController.navigate(Screen.Result.route) {
                            popUpTo(Screen.Game.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Result.route) {
                ResultScreen(
                    gameState = gameState,
                    onPlayAgain = {
                        viewModel.restartGame()
                        navController.navigate(Screen.Game.route) {
                            popUpTo(Screen.Result.route) { inclusive = true }
                        }
                    },
                    onHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settings = settings,
                    onThemeChanged = { mode -> viewModel.setTheme(mode) },
                    onDynamicColorChanged = { en -> viewModel.setDynamicColor(en) },
                    onCpuDifficultyChanged = { diff -> viewModel.setCpuDifficulty(diff) },
                    onSoundChanged = { en -> viewModel.setSound(en) },
                    onHapticsChanged = { en -> viewModel.setHaptics(en) },
                    onAnimationsChanged = { en -> viewModel.setAnimations(en) },
                    onDiagonalChanged = { en -> viewModel.setDiagonalMovement(en) },
                    onTimerChanged = { opt -> viewModel.setTurnTimer(opt) },
                    onShowCoordinatesChanged = { en -> viewModel.setShowCoordinates(en) },
                    onShowFeedbackChanged = { en -> viewModel.setShowWordFeedback(en) },
                    onConfirmRestartChanged = { en -> viewModel.setConfirmRestart(en) },
                    onResetSettings = { viewModel.resetSettings() },
                    onResetStatistics = { viewModel.resetStatistics() }
                )
            }

            composable(Screen.About.route) {
                AboutScreen()
            }
        }
    }
}
