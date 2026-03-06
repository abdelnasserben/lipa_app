package com.kori.app.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kori.app.core.designsystem.KoriAccent
import com.kori.app.core.designsystem.KoriBorder

@Composable
fun MiniChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
) {
    val safePoints = if (points.size >= 2) points else listOf(0f, 0f)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
    ) {
        val max = safePoints.maxOrNull()?.takeIf { it > 0f } ?: 1f
        val min = safePoints.minOrNull() ?: 0f
        val range = (max - min).takeIf { it > 0f } ?: 1f
        val stepX = size.width / (safePoints.size - 1).coerceAtLeast(1)

        drawLine(
            color = KoriBorder,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2.dp.toPx(),
        )

        val offsets = safePoints.mapIndexed { index, value ->
            val normalized = (value - min) / range
            val y = size.height - (normalized * (size.height * 0.82f))
            Offset(index * stepX, y)
        }

        offsets.zipWithNext().forEach { (start, end) ->
            drawLine(
                color = KoriAccent,
                start = start,
                end = end,
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        offsets.forEach { point ->
            drawCircle(
                color = KoriAccent,
                radius = 3.5.dp.toPx(),
                center = point,
            )
        }
    }
}