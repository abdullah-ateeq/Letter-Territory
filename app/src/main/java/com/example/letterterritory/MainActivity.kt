package com.example.letterterritory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.letterterritory.ui.navigation.LetterTerritoryNavHost
import com.example.letterterritory.ui.theme.LetterTerritoryTheme
import com.example.letterterritory.viewmodel.LetterTerritoryViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LetterTerritoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsState()

            LetterTerritoryTheme(
                themeMode = settings.themeMode,
                dynamicColor = settings.dynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LetterTerritoryNavHost(viewModel = viewModel)
                }
            }
        }
    }
}