package com.example.life4pollinators.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RankingBadge(
    modifier: Modifier = Modifier,
    period: String,
    rank: Int,
    color: Color = MaterialTheme.colorScheme.primary,
    ) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cerchio "pulito" con bordo e gradiente
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.25f),
                                color.copy(alpha = 0.10f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(2.dp, color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${rank}°",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = color
                )
            }

            // Label periodo
            Text(
                text = period,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
