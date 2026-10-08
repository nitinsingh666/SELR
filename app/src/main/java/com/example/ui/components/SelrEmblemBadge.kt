package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SelrEmblemBadge(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showBannerText: Boolean = true,
    isPulsing: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "badgePulse")
    val pulseScale by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    } else {
        rememberUpdatedState(1.0f)
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSelrShieldWithStudent()
        }

        // Overlay Texts: "SELR" at top and "सत्यमेव जयते" on bottom ribbon banner
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Stencil SELR
            Box(
                modifier = Modifier
                    .padding(top = (size.value * 0.13f).dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF151924).copy(alpha = 0.9f))
                    .border(1.dp, SelrRed, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "SELR",
                    fontFamily = AntonFontFamily,
                    fontSize = (size.value * 0.17f).sp,
                    color = SelrRed,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Bottom Ribbon Banner: "सत्यमेव जयते"
            if (showBannerText) {
                Box(
                    modifier = Modifier
                        .padding(bottom = (size.value * 0.08f).dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SelrRed)
                        .border(1.dp, SelrGold.copy(alpha = 0.8f), RoundedCornerShape(3.dp))
                        .padding(horizontal = 6.dp, vertical = 1.5.dp)
                ) {
                    Text(
                        text = "सत्यमेव जयते",
                        fontFamily = PoppinsFontFamily,
                        fontSize = (size.value * 0.085f).sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawSelrShieldWithStudent() {
    val w = size.width
    val h = size.height

    // Military Shield Path
    val shieldPath = Path().apply {
        moveTo(w * 0.18f, h * 0.12f)
        lineTo(w * 0.82f, h * 0.12f)
        lineTo(w * 0.85f, h * 0.54f)
        cubicTo(w * 0.85f, h * 0.76f, w * 0.50f, h * 0.90f, w * 0.50f, h * 0.90f)
        cubicTo(w * 0.50f, h * 0.90f, w * 0.15f, h * 0.76f, w * 0.15f, h * 0.54f)
        close()
    }

    // 1. Olive Camo Base Fill
    drawPath(shieldPath, color = Color(0xFF4B5320))

    // Camo organic texture spots
    drawCircle(color = Color(0xFF383F15), radius = w * 0.18f, center = Offset(w * 0.35f, h * 0.35f))
    drawCircle(color = Color(0xFF5D672A), radius = w * 0.20f, center = Offset(w * 0.65f, h * 0.45f))
    drawCircle(color = Color(0xFF313710), radius = w * 0.15f, center = Offset(w * 0.48f, h * 0.68f))

    // 2. Distressed Thick RED Outer Border (#C1272D)
    drawPath(
        shieldPath,
        color = Color(0xFFC1272D),
        style = Stroke(width = w * 0.055f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Inner Red Accent Line
    val innerShieldPath = Path().apply {
        moveTo(w * 0.23f, h * 0.17f)
        lineTo(w * 0.77f, h * 0.17f)
        lineTo(w * 0.79f, h * 0.52f)
        cubicTo(w * 0.79f, h * 0.71f, w * 0.50f, h * 0.84f, w * 0.50f, h * 0.84f)
        cubicTo(w * 0.50f, h * 0.84f, w * 0.21f, h * 0.71f, w * 0.21f, h * 0.52f)
        close()
    }
    drawPath(
        innerShieldPath,
        color = Color(0xFF8E1C22),
        style = Stroke(width = w * 0.015f)
    )

    // 3. Central Aura of Knowledge & Safety (Warm Golden Halo)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(SelrGold.copy(alpha = 0.4f), Color.Transparent),
            center = Offset(w * 0.50f, h * 0.46f),
            radius = w * 0.30f
        ),
        radius = w * 0.30f,
        center = Offset(w * 0.50f, h * 0.46f)
    )

    // 4. STUDENT / BOY SILHOUETTE (Center Figure)
    // Boy's Head
    val headCenter = Offset(w * 0.50f, h * 0.34f)
    drawCircle(
        color = Color(0xFFFFD1A4), // Face tone
        radius = w * 0.075f,
        center = headCenter
    )

    // Boy's Hair (Dark modern side-swept student haircut)
    val hairPath = Path().apply {
        moveTo(w * 0.42f, h * 0.34f)
        cubicTo(w * 0.42f, h * 0.27f, w * 0.58f, h * 0.27f, w * 0.58f, h * 0.34f)
        lineTo(w * 0.57f, h * 0.32f)
        cubicTo(w * 0.54f, h * 0.29f, w * 0.46f, h * 0.29f, w * 0.43f, h * 0.33f)
        close()
    }
    drawPath(hairPath, color = Color(0xFF261D19))
    drawOval(
        color = Color(0xFF261D19),
        topLeft = Offset(w * 0.43f, h * 0.265f),
        size = Size(w * 0.14f, h * 0.065f)
    )

    // Boy's Torso & Shoulders in Student Uniform (Navy & Cadet Olive)
    val torsoPath = Path().apply {
        moveTo(w * 0.46f, h * 0.40f)
        lineTo(w * 0.35f, h * 0.50f)
        lineTo(w * 0.37f, h * 0.57f)
        lineTo(w * 0.63f, h * 0.57f)
        lineTo(w * 0.65f, h * 0.50f)
        lineTo(w * 0.54f, h * 0.40f)
        close()
    }
    drawPath(torsoPath, color = Color(0xFF1B2335))

    // Uniform Collar / Tie (Red & Gold accent)
    drawLine(
        color = Color.White,
        start = Offset(w * 0.50f, h * 0.40f),
        end = Offset(w * 0.50f, h * 0.46f),
        strokeWidth = w * 0.015f
    )
    val tiePath = Path().apply {
        moveTo(w * 0.485f, h * 0.42f)
        lineTo(w * 0.515f, h * 0.42f)
        lineTo(w * 0.50f, h * 0.47f)
        close()
    }
    drawPath(tiePath, color = SelrRed)

    // 5. BOY'S HANDS HOLDING THE OPEN BOOK
    // Left & Right Arms coming forward
    drawLine(
        color = Color(0xFF1B2335),
        start = Offset(w * 0.37f, h * 0.50f),
        end = Offset(w * 0.42f, h * 0.56f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF1B2335),
        start = Offset(w * 0.63f, h * 0.50f),
        end = Offset(w * 0.58f, h * 0.56f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round
    )

    // Hands gripping the book (warm skin tone)
    drawCircle(color = Color(0xFFFFD1A4), radius = w * 0.026f, center = Offset(w * 0.38f, h * 0.54f))
    drawCircle(color = Color(0xFFFFD1A4), radius = w * 0.026f, center = Offset(w * 0.62f, h * 0.54f))

    // 6. THE OPEN BOOK (Knowledge & Student Education Symbol)
    // Book Outer Cover (Military Crimson / Gold Binding)
    val bookCoverPath = Path().apply {
        moveTo(w * 0.35f, h * 0.51f)
        lineTo(w * 0.50f, h * 0.535f)
        lineTo(w * 0.65f, h * 0.51f)
        lineTo(w * 0.65f, h * 0.63f)
        lineTo(w * 0.50f, h * 0.655f)
        lineTo(w * 0.35f, h * 0.63f)
        close()
    }
    drawPath(bookCoverPath, color = Color(0xFF8E1C22))

    // Book Pages (Crisp White / Golden Parchment)
    // Left Page
    val leftPage = Path().apply {
        moveTo(w * 0.36f, h * 0.50f)
        lineTo(w * 0.495f, h * 0.525f)
        lineTo(w * 0.495f, h * 0.635f)
        lineTo(w * 0.36f, h * 0.61f)
        close()
    }
    drawPath(leftPage, color = Color(0xFFFFFDE7))

    // Right Page
    val rightPage = Path().apply {
        moveTo(w * 0.505f, h * 0.525f)
        lineTo(w * 0.64f, h * 0.50f)
        lineTo(w * 0.64f, h * 0.61f)
        lineTo(w * 0.505f, h * 0.635f)
        close()
    }
    drawPath(rightPage, color = Color(0xFFFFFDE7))

    // Book Center Spine Line
    drawLine(
        color = Color(0xFFC1272D),
        start = Offset(w * 0.50f, h * 0.525f),
        end = Offset(w * 0.50f, h * 0.645f),
        strokeWidth = w * 0.015f
    )

    // Lines of text on pages (Study notes)
    val lineStroke = w * 0.007f
    val textLineColor = Color(0xFF4A5568)
    // Left page lines
    drawLine(textLineColor, Offset(w * 0.39f, h * 0.535f), Offset(w * 0.47f, h * 0.548f), strokeWidth = lineStroke)
    drawLine(textLineColor, Offset(w * 0.39f, h * 0.565f), Offset(w * 0.47f, h * 0.578f), strokeWidth = lineStroke)
    drawLine(textLineColor, Offset(w * 0.39f, h * 0.595f), Offset(w * 0.47f, h * 0.608f), strokeWidth = lineStroke)
    // Right page lines
    drawLine(textLineColor, Offset(w * 0.53f, h * 0.548f), Offset(w * 0.61f, h * 0.535f), strokeWidth = lineStroke)
    drawLine(textLineColor, Offset(w * 0.53f, h * 0.578f), Offset(w * 0.61f, h * 0.565f), strokeWidth = lineStroke)
    drawLine(textLineColor, Offset(w * 0.53f, h * 0.608f), Offset(w * 0.61f, h * 0.595f), strokeWidth = lineStroke)

    // 7. Dark Olive Laurel Wreath Below the Book
    val wreathPath = Path().apply {
        moveTo(w * 0.28f, h * 0.67f)
        cubicTo(w * 0.36f, h * 0.77f, w * 0.64f, h * 0.77f, w * 0.72f, h * 0.67f)
    }
    drawPath(
        wreathPath,
        color = Color(0xFF283010),
        style = Stroke(width = w * 0.028f, cap = StrokeCap.Round)
    )

    // Laurel leaves
    val leafColor = Color(0xFF384316)
    drawOval(color = leafColor, topLeft = Offset(w * 0.32f, h * 0.69f), size = Size(w * 0.07f, h * 0.04f))
    drawOval(color = leafColor, topLeft = Offset(w * 0.61f, h * 0.69f), size = Size(w * 0.07f, h * 0.04f))
    drawOval(color = leafColor, topLeft = Offset(w * 0.46f, h * 0.73f), size = Size(w * 0.08f, h * 0.04f))
}
