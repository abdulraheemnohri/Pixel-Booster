package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun LiveTrendGraph(
    currentValue: Float, // 0.0 to 100.0
    lineColor: Color,
    modifier: Modifier = Modifier,
    maxHistory: Int = 50
) {
    val history = remember { mutableStateListOf<Float>() }

    LaunchedEffect(currentValue) {
        history.add(currentValue)
        if (history.size > maxHistory) {
            history.removeAt(0)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        if (history.size < 2) return@Canvas

        val path = Path()
        val width = size.width
        val height = size.height
        val stepX = width / (maxHistory - 1)

        history.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / 100f * height)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}
