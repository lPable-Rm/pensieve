package com.mvppostit.pensieve.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/*
 * Estos dos iconos son estáticos y solo acompañan a las tarjetas informativas.
 * Se dibujan con Canvas para no introducir una librería de iconos adicional.
 */

@Composable
internal fun NotificationBellIcon() {
    val primary = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.size(28.dp)) {
        val stroke = Stroke(
            width = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val bell = Path().apply {
            moveTo(size.width * 0.25f, size.height * 0.75f)
            lineTo(size.width * 0.3f, size.height * 0.7f)
            lineTo(size.width * 0.3f, size.height * 0.45f)
            quadraticTo(
                size.width * 0.3f,
                size.height * 0.18f,
                size.width * 0.5f,
                size.height * 0.18f,
            )
            quadraticTo(
                size.width * 0.7f,
                size.height * 0.18f,
                size.width * 0.7f,
                size.height * 0.45f,
            )
            lineTo(size.width * 0.7f, size.height * 0.7f)
            lineTo(size.width * 0.75f, size.height * 0.75f)
            close()
        }

        drawPath(bell, color = primary, style = stroke)
        drawLine(
            color = primary,
            start = Offset(size.width * 0.2f, size.height * 0.75f),
            end = Offset(size.width * 0.8f, size.height * 0.75f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = primary,
            radius = stroke.width / 2,
            center = Offset(size.width * 0.5f, size.height * 0.86f),
        )
    }
}

@Composable
internal fun WidgetGridIcon() {
    val primary = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.size(28.dp)) {
        val strokeWidth = 2.5.dp.toPx()
        val cellSize = size.minDimension * 0.3f
        val gap = size.minDimension * 0.1f
        val start = size.minDimension * 0.15f

        repeat(2) { row ->
            repeat(2) { column ->
                drawRoundRect(
                    color = primary,
                    topLeft = Offset(
                        start + column * (cellSize + gap),
                        start + row * (cellSize + gap),
                    ),
                    size = Size(cellSize, cellSize),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = Stroke(width = strokeWidth),
                )
            }
        }
    }
}
