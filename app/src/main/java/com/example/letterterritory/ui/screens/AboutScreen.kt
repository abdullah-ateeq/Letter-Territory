package com.example.letterterritory.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * About screen detailing game identity, mechanics, grid specs, and privacy guarantee.
 */
@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "LETTER",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "TERRITORY",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        AboutCard(
            title = "CONCEPT",
            body = "An offline competitive word game where valid words let you claim neutral cells on a shared letter grid."
        )

        Spacer(modifier = Modifier.height(14.dp))

        AboutCard(
            title = "HOW IT WORKS",
            body = "Connect adjacent letters to create words. Every neutral cell used in a valid word becomes your territory. You can traverse already-owned cells, but enemy ground is never stolen."
        )

        Spacer(modifier = Modifier.height(14.dp))

        AboutCard(
            title = "BOARD",
            body = "5 × 5 shared letter grid (25 cells total). Weighted letter frequencies with guaranteed high word potential."
        )

        Spacer(modifier = Modifier.height(14.dp))

        AboutCard(
            title = "PRIVACY",
            body = "No account. No internet connection. No backend. No advertisements. No personal data collection. Fully offline."
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AboutCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }
    }
}
