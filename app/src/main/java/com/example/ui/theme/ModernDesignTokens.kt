package com.example.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ModernDesignTokens {
    val RadiusXSmall = 8.dp
    val RadiusSmall = 12.dp
    val RadiusMedium = 16.dp
    val RadiusLarge = 22.dp
    val RadiusXLarge = 28.dp
    val RadiusFull = 14.dp

    // Cyber Modern Gradients
    val PrimaryGlowBrush = Brush.linearGradient(
        colors = listOf(CyberCyan, PrimaryBlue, ElectricIndigo)
    )

    val MintGlowBrush = Brush.linearGradient(
        colors = listOf(Color(0xFF34D399), Color(0xFF059669))
    )

    val AmberGlowBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFFBBF24), Color(0xFFD97706))
    )

    val CrimsonGlowBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFF87171), Color(0xFFDC2626))
    )

    val DarkGlassSurface = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF131D36).copy(alpha = 0.90f),
            Color(0xFF0B1224).copy(alpha = 0.95f)
        )
    )

    val CardBorderBrush = Brush.linearGradient(
        colors = listOf(
            Color(0x5538BDF8),
            Color(0x1538BDF8),
            Color(0x05FFFFFF),
            Color(0x2038BDF8)
        )
    )

    val AccentBorderBrush = Brush.linearGradient(
        colors = listOf(
            CyberCyan.copy(alpha = 0.8f),
            ElectricIndigo.copy(alpha = 0.4f),
            Color.Transparent
        )
    )
}

/**
 * Modern glassmorphic card modifier with glowing border and atmospheric depth.
 */
fun Modifier.modernCard(
    shape: Shape = RoundedCornerShape(ModernDesignTokens.RadiusLarge),
    borderBrush: Brush = ModernDesignTokens.CardBorderBrush,
    borderWidth: Dp = 1.dp,
    containerColor: Color? = null
): Modifier = this
    .clip(shape)
    .then(
        if (containerColor != null) {
            Modifier.background(containerColor)
        } else {
            Modifier.background(ModernDesignTokens.DarkGlassSurface)
        }
    )
    .border(borderWidth, borderBrush, shape)

/**
 * Subtle breathing neon glow effect around hero elements.
 */
@Composable
fun Modifier.pulsingGlow(
    glowColor: Color = PrimaryBlue,
    minAlpha: Float = 0.15f,
    maxAlpha: Float = 0.45f,
    durationMs: Int = 2200
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    this.drawBehind {
        drawCircle(
            color = glowColor.copy(alpha = alpha),
            radius = size.maxDimension * 0.58f
        )
    }
}
