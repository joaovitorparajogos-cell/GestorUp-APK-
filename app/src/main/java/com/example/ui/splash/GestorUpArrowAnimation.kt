package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.example.brand.BrandAssets
import kotlin.math.atan2

/**
 * Animated visual layer that reproduces the trajectory of the arrow incorporated
 * into the 'U' of GestorUp.
 *
 * Sequence:
 * 1. Initial rest state showing the brand.
 * 2. An energetic, fluid emerald pulse travels along the curve of the 'U':
 *    starts at top-left stem, sweeps down, curves along the bottom basin,
 *    and shoots upwards along the right stem.
 * 3. An arrow tip tracks along the trajectory following the instantaneous tangent.
 * 4. Decelerates smoothly as it reaches the peak of the arrow.
 * 5. Culminates in a brief radiant gleam at the top point of the arrow.
 */
@Composable
fun GestorUpArrowAnimationLayer(
  modifier: Modifier = Modifier,
  durationMs: Int = BrandAssets.UArrowGeometry.DEFAULT_ANIMATION_DURATION_MS.toInt(),
  startDelayMs: Int = 200,
  onAnimationFinished: () -> Unit = {},
) {
  // Main progression of the arrow along the 'U' path (0.0f -> 1.0f)
  val progress = remember { Animatable(0f) }
  // Pulse / shine burst at the peak (0.0f -> 1.0f)
  val peakShine = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    kotlinx.coroutines.delay(startDelayMs.toLong())

    // 1. Traverse the 'U' curve with smooth acceleration and deceleration
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = durationMs,
        easing = CubicBezierEasing(0.40f, 0.0f, 0.20f, 1.0f) // smooth progression & soft stop
      )
    )

    // 2. Culmination shine when reaching the top of the arrow
    peakShine.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = 350,
        easing = FastOutSlowInEasing
      )
    )

    // 3. Short settle pause
    kotlinx.coroutines.delay(BrandAssets.UArrowGeometry.POST_ANIMATION_HOLD_MS)
    onAnimationFinished()
  }

  Canvas(modifier = modifier.fillMaxSize()) {
    val canvasSize = size.minDimension
    val offsetX = (size.width - canvasSize) / 2f
    val offsetY = (size.height - canvasSize) / 2f

    // Convert normalized logo coordinates to actual canvas coordinates
    fun norm(point: Offset): Offset {
      return Offset(
        x = offsetX + point.x * canvasSize,
        y = offsetY + point.y * canvasSize
      )
    }

    // Build the exact bezier path following the letter 'U' and arrow
    val pStart = norm(BrandAssets.UArrowGeometry.StartLeftStem)
    val pMidLeft = norm(BrandAssets.UArrowGeometry.MidLeftStem)
    val pBottomLeft = norm(BrandAssets.UArrowGeometry.BottomLeftCurve)
    val pBottomCenter = norm(BrandAssets.UArrowGeometry.BottomCenter)
    val pBottomRight = norm(BrandAssets.UArrowGeometry.BottomRightCurve)
    val pMidRight = norm(BrandAssets.UArrowGeometry.MidRightStem)
    val pArrowBase = norm(BrandAssets.UArrowGeometry.ArrowBase)
    val pArrowPeak = norm(BrandAssets.UArrowGeometry.ArrowPeak)

    val uPath = Path().apply {
      moveTo(pStart.x, pStart.y)
      // Left vertical stem going down
      lineTo(pMidLeft.x, pMidLeft.y)
      // Bottom basin curve
      cubicTo(
        pBottomLeft.x, pBottomLeft.y,
        pBottomLeft.x + (pBottomCenter.x - pBottomLeft.x) * 0.4f, pBottomCenter.y,
        pBottomCenter.x, pBottomCenter.y
      )
      cubicTo(
        pBottomCenter.x + (pBottomRight.x - pBottomCenter.x) * 0.6f, pBottomCenter.y,
        pBottomRight.x, pBottomRight.y,
        pMidRight.x, pMidRight.y
      )
      // Right stem ascending
      lineTo(pArrowBase.x, pArrowBase.y)
      // Into the arrowhead peak
      lineTo(pArrowPeak.x, pArrowPeak.y)
    }

    val pathMeasure = PathMeasure().apply {
      setPath(uPath, forceClosed = false)
    }
    val totalLength = pathMeasure.length
    if (totalLength <= 0f) return@Canvas

    val currentProg = progress.value
    if (currentProg > 0.01f) {
      val currentDist = currentProg * totalLength
      // Tail length of the moving arrow beam (e.g. up to 35% of total length)
      val tailLength = (totalLength * 0.35f).coerceAtMost(currentDist)
      val startDist = (currentDist - tailLength).coerceAtLeast(0f)

      // Extract the segment currently traversed
      val segmentPath = Path()
      pathMeasure.getSegment(startDist, currentDist, segmentPath, true)

      val strokeWidthPx = canvasSize * 0.018f

      // 1. Draw glowing aura / trace along the 'U' path
      val glowBrush = Brush.linearGradient(
        colors = listOf(
          Color(0x0010B981),
          Color(0x6634D399),
          Color(0xCC5BDD62),
          Color(0xFFFFFFFF)
        ),
        start = pathMeasure.getPosition(startDist),
        end = pathMeasure.getPosition(currentDist)
      )

      drawPath(
        path = segmentPath,
        brush = glowBrush,
        style = Stroke(
          width = strokeWidthPx * 1.8f,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round
        ),
        blendMode = BlendMode.SrcOver
      )

      // 2. Draw crisp core energetic stroke
      val coreBrush = Brush.linearGradient(
        colors = listOf(
          Color(0x00059669),
          Color(0xFF10B981),
          Color(0xFF4EDE9F),
          Color(0xFFFFFFFF)
        ),
        start = pathMeasure.getPosition(startDist),
        end = pathMeasure.getPosition(currentDist)
      )

      drawPath(
        path = segmentPath,
        brush = coreBrush,
        style = Stroke(
          width = strokeWidthPx,
          cap = StrokeCap.Round,
          join = StrokeJoin.Round
        )
      )

      // 3. Arrow tip at the head of the motion, rotated along the path tangent
      val headPos = pathMeasure.getPosition(currentDist)
      val tangent = pathMeasure.getTangent(currentDist)
      val angleDegrees = (atan2(tangent.y, tangent.x) * 180f / Math.PI).toFloat()

      // Arrow tip size relative to logo canvas
      val headSize = canvasSize * 0.032f

      rotate(degrees = angleDegrees - 90f, pivot = headPos) {
        val arrowHeadPath = Path().apply {
          moveTo(headPos.x, headPos.y)
          lineTo(headPos.x - headSize * 0.55f, headPos.y + headSize)
          lineTo(headPos.x, headPos.y + headSize * 0.7f)
          lineTo(headPos.x + headSize * 0.55f, headPos.y + headSize)
          close()
        }

        drawPath(
          path = arrowHeadPath,
          color = Color(0xFF5BDD62)
        )
        drawPath(
          path = arrowHeadPath,
          color = Color.White.copy(alpha = 0.85f),
          style = Stroke(width = strokeWidthPx * 0.3f)
        )
      }
    }

    // 4. Culmination gleam at the top of the arrow when animation finishes
    val shineVal = peakShine.value
    if (shineVal > 0f) {
      val peak = pArrowPeak
      val shineRadius = canvasSize * 0.045f * (0.8f + 0.4f * shineVal)
      val alpha = (1f - shineVal).coerceIn(0f, 1f)

      // Radiant halo
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White.copy(alpha = 0.9f * alpha),
            Color(0xFF5BDD62).copy(alpha = 0.6f * alpha),
            Color(0xFF10B981).copy(alpha = 0.2f * alpha),
            Color.Transparent
          ),
          center = peak,
          radius = shineRadius
        ),
        radius = shineRadius,
        center = peak
      )

      // Subtle horizontal flare
      val flareLength = shineRadius * 1.6f
      drawLine(
        color = Color.White.copy(alpha = 0.8f * alpha),
        start = Offset(peak.x - flareLength, peak.y),
        end = Offset(peak.x + flareLength, peak.y),
        strokeWidth = 3f * (1f - shineVal)
      )
    }
  }
}
