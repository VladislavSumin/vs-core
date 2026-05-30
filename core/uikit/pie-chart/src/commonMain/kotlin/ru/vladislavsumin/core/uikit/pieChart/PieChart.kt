package ru.vladislavsumin.core.uikit.pieChart

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
public fun PieChart(pies: List<Slice>, modifier: Modifier = Modifier) {
    val totalFraction by remember { derivedStateOf { pies.sumOf { it.fraction.toDouble() }.toFloat() } }
    Canvas(modifier) {
        var currentAngle = 0f
        pies.forEach { slice ->
            @Suppress("MagicNumber")
            val sweepAngle = 360f * slice.fraction / totalFraction
            drawArc(slice.color, currentAngle, sweepAngle, useCenter = true)
            currentAngle += sweepAngle
        }
    }
}

public data class Slice(val fraction: Float, val color: Color)
