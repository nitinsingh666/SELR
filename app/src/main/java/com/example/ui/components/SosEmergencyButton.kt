package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SosEmergencyButton(
    onTriggerSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressing by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var showQuickTapHint by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Pulse animation when idle
    val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
    val pulseRingRadius by infiniteTransition.animateFloat(
        initialValue = 95f,
        targetValue = 135f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Press progress updater
    LaunchedEffect(isPressing) {
        if (isPressing) {
            val totalSteps = 60
            val delayPerStep = 3000L / totalSteps // 3 seconds total
            for (step in 1..totalSteps) {
                delay(delayPerStep)
                holdProgress = step.toFloat() / totalSteps
            }
            // 3 seconds reached!
            onTriggerSos()
            isPressing = false
            holdProgress = 0f
        } else {
            holdProgress = 0f
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .testTag("sos_button_container"),
            contentAlignment = Alignment.Center
        ) {
            // Background Canvas for pulse ripples & progress arc
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val baseRadius = 88.dp.toPx()

                // Idle emergency radar ripple
                if (!isPressing) {
                    drawCircle(
                        color = SelrRedBright.copy(alpha = pulseAlpha),
                        radius = (pulseRingRadius).dp.toPx() * 0.7f,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Outer distressed rim
                drawCircle(
                    color = Color(0xFF5A1216),
                    radius = baseRadius + 8.dp.toPx(),
                    center = center
                )

                // Circular Hold Progress Arc (0 to 360 degrees)
                if (holdProgress > 0f) {
                    val arcStroke = 8.dp.toPx()
                    val arcRadius = baseRadius + 4.dp.toPx()
                    drawArc(
                        color = SelrGold,
                        startAngle = -90f,
                        sweepAngle = holdProgress * 360f,
                        useCenter = false,
                        topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                        size = Size(arcRadius * 2, arcRadius * 2),
                        style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                    )
                }
            }

            // Main SOS Button Body
            Box(
                modifier = Modifier
                    .size(176.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (isPressing) {
                                listOf(Color(0xFFE53935), Color(0xFFC1272D), Color(0xFF8E1C22))
                            } else {
                                listOf(Color(0xFFC1272D), Color(0xFF9E1B21), Color(0xFF6B1216))
                            }
                        )
                    )
                    .border(
                        width = 4.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(SelrGold.copy(alpha = 0.9f), Color(0xFFC1272D), Color.Black)
                        ),
                        shape = CircleShape
                    )
                    .shadow(elevation = 16.dp, shape = CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressing = true
                                showQuickTapHint = false
                                tryAwaitRelease()
                                if (holdProgress < 0.95f) {
                                    showQuickTapHint = true
                                }
                                isPressing = false
                                holdProgress = 0f
                            }
                        )
                    }
                    .testTag("sos_emergency_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SOS Warning",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "EMERGENCY",
                        fontFamily = AntonFontFamily,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "SOS",
                        fontFamily = AntonFontFamily,
                        fontSize = 42.sp,
                        color = Color.White,
                        lineHeight = 44.sp,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isPressing) "HOLDING... ${(3 * (1 - holdProgress)).toInt() + 1}s" else "3 SEC PRESS",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = if (isPressing) SelrGold else Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Instruction / False press hint
        if (showQuickTapHint) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF3E1D1F))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "⚠️ Press & HOLD for 3 full seconds to activate SOS",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = SelrGold,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Text(
                text = "Hold down for 3 seconds • Auto alerts Parents & Faculty",
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = SelrTextSecondary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
