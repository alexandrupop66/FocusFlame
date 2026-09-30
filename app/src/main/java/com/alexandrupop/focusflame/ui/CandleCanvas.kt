package com.alexandrupop.focusflame.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.alexandrupop.focusflame.WaxDrip
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun CandleCanvas(
    progress: Float,
    drips: List<WaxDrip>,
    finished: Boolean,
    modifier: Modifier = Modifier
) {
    val flicker by rememberInfiniteTransition(label = "flame").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(950, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flicker"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
    ) {
        val candleWidth = size.width * 0.28f
        val candleHeight = size.height * 0.62f
        val left = (size.width - candleWidth) / 2f
        val bottom = size.height * 0.88f
        val top = bottom - candleHeight
        val burnY = top + candleHeight * progress.coerceIn(0f, 1f)
        val remainingHeight = (bottom - burnY).coerceAtLeast(0f)
        val centerX = size.width / 2f

        drawOval(
            color = Color(0x22000000),
            topLeft = Offset(centerX - candleWidth * 0.9f, bottom - 8f),
            size = Size(candleWidth * 1.8f, 22f)
        )

        drawRoundRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFF6CFA2), Color(0xFFFFF0D5), Color(0xFFDFAE74))
            ),
            topLeft = Offset(left, burnY),
            size = Size(candleWidth, remainingHeight),
            cornerRadius = CornerRadius(18f, 18f)
        )

        drawRoundRect(
            color = Color(0x33A86E3B),
            topLeft = Offset(left + candleWidth * 0.64f, burnY + 8f),
            size = Size(candleWidth * 0.14f, remainingHeight * 0.9f),
            cornerRadius = CornerRadius(10f, 10f)
        )

        drawOval(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFF7E8), Color(0xFFE2B478)),
                center = Offset(centerX, burnY),
                radius = candleWidth * 0.58f
            ),
            topLeft = Offset(left, burnY - 11f),
            size = Size(candleWidth, 22f)
        )

        drips.forEachIndexed { index, drip ->
            val y = top + candleHeight * drip.progress.coerceIn(0f, 1f)
            val side = if (index % 2 == 0) -1f else 1f
            val x = centerX + side * candleWidth * (0.28f + (index % 3) * 0.07f)
            val dripLength = candleHeight * (0.08f + drip.lengthFactor * 0.26f)
            val path = Path().apply {
                moveTo(x, y)
                quadraticBezierTo(x + side * 18f, y + dripLength * 0.34f, x + side * 8f, y + dripLength)
                quadraticBezierTo(x - side * 6f, y + dripLength * 0.72f, x, y)
            }
            drawPath(path, color = Color(0xFFE5B57F))
            drawPath(path, color = Color(0x55FFFFFF), style = Stroke(width = 2f))
        }

        if (!finished && progress < 1f) {
            val wickTop = burnY - 28f
            drawLine(
                color = Color(0xFF3C2518),
                start = Offset(centerX, burnY + 1f),
                end = Offset(centerX, wickTop),
                strokeWidth = 5f
            )

            val wave = sin(flicker * 2f * PI).toFloat()
            val flameHeight = 58f + wave * 7f
            val flameWidth = 36f - wave * 3f
            val flame = Path().apply {
                moveTo(centerX, wickTop - flameHeight)
                cubicTo(centerX + flameWidth, wickTop - flameHeight * 0.55f, centerX + flameWidth * 0.45f, wickTop - 6f, centerX, wickTop)
                cubicTo(centerX - flameWidth * 0.55f, wickTop - 10f, centerX - flameWidth, wickTop - flameHeight * 0.52f, centerX, wickTop - flameHeight)
            }
            drawPath(
                path = flame,
                brush = Brush.verticalGradient(listOf(Color(0xFFFFF1A6), Color(0xFFFF8A1D), Color(0xFFE04B16)))
            )

            val inner = Path().apply {
                moveTo(centerX + 1f, wickTop - flameHeight * 0.62f)
                cubicTo(centerX + 13f, wickTop - flameHeight * 0.35f, centerX + 8f, wickTop - 7f, centerX, wickTop - 3f)
                cubicTo(centerX - 8f, wickTop - 8f, centerX - 11f, wickTop - flameHeight * 0.34f, centerX + 1f, wickTop - flameHeight * 0.62f)
            }
            drawPath(inner, color = Color(0xFFFFF6C7))
        } else {
            repeat(4) { index ->
                val smokeY = burnY - 30f - index * 26f
                val smokeX = centerX + sin((flicker + index * 0.23f) * 2f * PI).toFloat() * 16f
                drawArc(
                    color = Color(0x554D5652),
                    startAngle = 210f,
                    sweepAngle = 165f,
                    useCenter = false,
                    topLeft = Offset(smokeX - 16f, smokeY - 16f),
                    size = Size(34f + index * 5f, 30f),
                    style = Stroke(width = 3f)
                )
            }
            drawLine(
                color = Color(0xFF3C2518),
                start = Offset(centerX, burnY + 1f),
                end = Offset(centerX, burnY - 16f),
                strokeWidth = 5f
            )
        }

        drawArc(
            color = Color(0x443B2418),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(left, burnY - 11f),
            size = Size(candleWidth, 22f),
            style = Stroke(width = 2f)
        )
    }
}
