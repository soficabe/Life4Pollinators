package com.example.life4pollinators.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class SectionCardSize {
    Large, Small
}

/**
 * Card componibile per mostrare una sezione cliccabile nella home o lista.
 *
 * @param title Titolo della sezione.
 * @param imageRes Icona/immagine da visualizzare a sinistra.
 * @param backgroundColor Colore di sfondo della card.
 * @param onClick Callback eseguita al click sulla card.
 * @param modifier Modificatore opzionale.
 * @param cardSize Dimensione della card (Large o Small)
 */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String,
    imageRes: Painter,
    backgroundColor: Color,
    onClick: () -> Unit,
    cardSize: SectionCardSize = SectionCardSize.Small // default small
) {
    // Definizione delle dimensioni immagine e card (larghezza fissa, altezza variabile)
    val imageWidth = 64.dp
    val imageHeight = when (cardSize) {
        SectionCardSize.Large -> 64.dp
        SectionCardSize.Small -> 40.dp
    }
    val cardHeight = when (cardSize) {
        SectionCardSize.Large -> 88.dp
        SectionCardSize.Small -> 64.dp
    }
    val textSize = when (cardSize) {
        SectionCardSize.Large -> 22
        SectionCardSize.Small -> 18
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(bottom = 8.dp, start = 8.dp, end = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Image(
                painter = imageRes,
                contentDescription = "Section Card Image",
                contentScale = ContentScale.Crop, // o .Fit per non tagliare
                modifier = Modifier
                    .requiredWidth(imageWidth)
                    .requiredHeight(imageHeight)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = textSize.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = "Arrow Right",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}