package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium Glassmorphism & Depth styling library
 * Inspired by Ionic Framework & modern web/native translucent designs.
 */

@Composable
fun getGlassmorphicBrush(isDark: Boolean = LocalIsDark.current): Brush {
    return if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF23232D).copy(alpha = 0.55f), // iOS/Ionic translucent gray top
                Color(0xFF141418).copy(alpha = 0.75f)  // Matte dark base bottom
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF).copy(alpha = 0.65f), // Crystal translucent white top
                Color(0xFFF3F4F6).copy(alpha = 0.78f)  // Soft light grey bottom
            ),
            start = Offset(0f, 0f),
            end = Offset(0f, Float.POSITIVE_INFINITY)
        )
    }
}

@Composable
fun getGlassmorphicBorderColor(isDark: Boolean = LocalIsDark.current): Color {
    return if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.45f)
    }
}

/**
 * Extension Modifier that converts any Composable into a luxurious semi-translucent Glass container.
 */
@Composable
fun Modifier.glassmorphic(
    isDark: Boolean = LocalIsDark.current,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    accentGlow: Boolean = false
): Modifier {
    val glassBrush = getGlassmorphicBrush(isDark)
    val glassBorder = getGlassmorphicBorderColor(isDark)
    
    // Smooth pulse for premium visual depth if accentGlow is enabled
    val infiniteTransition = rememberInfiniteTransition(label = "accentPulse")
    val borderAlpha by if (accentGlow) {
        infiniteTransition.animateFloat(
            initialValue = 0.12f,
            targetValue = 0.22f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "borderGlow"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val finalBorderColor = if (accentGlow) {
        val accentColor = if (isDark) Color(0xFF3880FF) else Color(0xFF5260FF)
        accentColor.copy(alpha = borderAlpha)
    } else {
        glassBorder
    }

    return this.then(
        Modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(glassBrush)
            .border(borderWidth, finalBorderColor, RoundedCornerShape(cornerRadius))
    )
}

/**
 * Ionic-Glass Container layout that handles glowing background elements
 * creating a striking contrast with the glassmorphic cards in the foreground.
 */
@Composable
fun GlassBackgroundBox(
    modifier: Modifier = Modifier,
    isDark: Boolean = LocalIsDark.current,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF121212) else Color(0xFFF4F5F8))
    ) {
        // Ambient glowing light blobs in the background (Ionic Atmosphere style)
        val infiniteTransition = rememberInfiniteTransition(label = "bgAtmosphere")
        val blobOffset1 by infiniteTransition.animateValue(
            initialValue = Offset(-80f, 150f),
            targetValue = Offset(160f, 250f),
            typeConverter = Offset.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blob1"
        )
        val blobOffset2 by infiniteTransition.animateValue(
            initialValue = Offset(150f, 400f),
            targetValue = Offset(-120f, 300f),
            typeConverter = Offset.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "blob2"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(90.dp)
        ) {
            // First glow blob (Teal / Blue)
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .offset(x = blobOffset1.x.dp, y = blobOffset1.y.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (isDark) Color(0xFF3880FF) else Color(0xFF3880FF)).copy(alpha = if (isDark) 0.12f else 0.18f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(140.dp)
                    )
            )

            // Second glow blob (Purple / Blue accent)
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .offset(x = blobOffset2.x.dp, y = blobOffset2.y.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                (if (isDark) Color(0xFF5260FF) else Color(0xFF3DC2FF)).copy(alpha = if (isDark) 0.08f else 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(125.dp)
                    )
            )
        }

        // Foreground content
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
