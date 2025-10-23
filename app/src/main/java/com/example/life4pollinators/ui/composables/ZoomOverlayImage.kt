package com.example.life4pollinators.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage

/**
 * Composable generico per mostrare un'immagine zoommabile in overlay a schermo intero.
 *
 * Supporta sia immagini remote (via URL) che locali (drawable resource).
 * Funzionalità:
 * - Pinch-to-zoom con gesture multitouch
 * - Pan (trascinamento) dell'immagine zoomata
 * - Single-Tap per chiudere
 * - Double-tap per resettare zoom e posizione
 *
 * L'overlay si posiziona sopra il contenuto esistente con sfondo nero semi-trasparente.
 *
 * @param imageUrl URL dell'immagine remota
 * @param imageRes ID risorsa drawable per immagine locale
 * @param contentDescription Descrizione per accessibilità
 * @param onClose Callback invocata quando l'utente chiude l'overlay (single-tap)
 * @param minScale Scala minima consentita (default: 1f = dimensione originale)
 * @param maxScale Scala massima consentita (default: 8f = 8x zoom)
 * @param contentScale Strategia di scaling dell'immagine (default: Fit)
 */
@Composable
fun ZoomOverlayImage(
    imageUrl: String? = null,
    imageRes: Int? = null,
    contentDescription: String? = null,
    onClose: () -> Unit,
    minScale: Float = 1f,
    maxScale: Float = 8f,
    contentScale: ContentScale = ContentScale.Fit,
) {
    // Stati per gestire zoom e pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .pointerInput(Unit) {
                detectTapGestures(
                    // Tap singolo: chiude l'overlay
                    onTap = { onClose() },
                    // Double tap: reset zoom e posizione
                    onDoubleTap = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }
                )
            }
    ) {
        when {
            // Caso 1: Immagine remota (AsyncImage con Coil)
            imageUrl != null -> {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .pointerInput(Unit) {
                            // Gesture multitouch: pinch-to-zoom e pan
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(minScale, maxScale)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        },
                    contentScale = contentScale
                )
            }
            // Caso 2: Immagine locale (Image con Painter)
            imageRes != null -> {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(minScale, maxScale)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        },
                    contentScale = contentScale
                )
            }
        }
    }
}