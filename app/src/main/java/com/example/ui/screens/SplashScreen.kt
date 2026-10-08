package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SelrEmblemBadge
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "splashAlpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "splashScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2600)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SelrNavyBg)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .alpha(alphaAnim.value)
                .scale(scaleAnim.value)
        ) {
            // Military Crest Badge
            SelrEmblemBadge(
                size = 170.dp,
                showBannerText = true,
                isPulsing = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Main Title
            Text(
                text = "SELR",
                fontFamily = AntonFontFamily,
                fontSize = 46.sp,
                color = SelrRed,
                letterSpacing = 4.sp,
                lineHeight = 48.sp
            )

            Text(
                text = "STUDENT EMERGENCY LOCATION & RESCUE",
                fontFamily = AntonFontFamily,
                fontSize = 14.sp,
                color = Color.White,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline Box
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SelrNavyCard)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "LEAD • PROTECT • RESCUE • INTEGRITY",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = SelrGold,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = SelrRedBright,
                strokeWidth = 2.5.dp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "INITIALIZING TACTICAL DEFENSE MATRIX...",
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = SelrTextSecondary,
                letterSpacing = 0.5.sp
            )
        }
    }
}
