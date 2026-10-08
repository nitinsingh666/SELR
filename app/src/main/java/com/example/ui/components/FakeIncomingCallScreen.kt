package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FakeCallState
import com.example.ui.theme.*

@Composable
fun FakeIncomingCallScreen(
    fakeCallState: FakeCallState,
    onAcceptCall: () -> Unit,
    onDeclineCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "callPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F1522), Color(0xFF080B12), Color.Black)
                )
            )
            .padding(24.dp)
            .testTag("fake_call_screen")
    ) {
        if (fakeCallState.isRinging) {
            // Incoming Call Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Caller Info Top
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E283E))
                            .border(2.dp, Color(0xFF38496B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(56.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = fakeCallState.callerName,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = fakeCallState.callerNumber,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 14.sp,
                        color = SelrTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Incoming Emergency Escape Call...",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = SelrGold
                    )
                }

                // Accept & Decline Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Decline (Red)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onDeclineCall,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD32F2F))
                                .testTag("decline_fake_call_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallEnd,
                                contentDescription = "Decline Call",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Decline",
                            color = Color.White,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp
                        )
                    }

                    // Accept (Green with pulse)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onAcceptCall,
                            modifier = Modifier
                                .size((68 * pulseScale).dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                                .testTag("accept_fake_call_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Accept Call",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Answer",
                            color = Color.White,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else if (fakeCallState.isInCall) {
            // Active Call Screen
            val min = fakeCallState.callDurationSeconds / 60
            val sec = fakeCallState.callDurationSeconds % 60
            val durationText = String.format("%02d:%02d", min, sec)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header & Duration
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = fakeCallState.callerName,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = durationText,
                        fontFamily = AntonFontFamily,
                        fontSize = 18.sp,
                        color = SelrSafeGreenBright,
                        letterSpacing = 1.sp
                    )
                }

                // Simulated Voice Message Cue card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelrOliveAccent.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = SelrGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SAFE EXIT SCRIPT",
                                fontFamily = AntonFontFamily,
                                fontSize = 13.sp,
                                color = SelrGold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "\"Haan beta, main campus gate ke bahar car mein wait kar raha hoon. 2 minute mein gate par aa jao, sab theek hai na?\"",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 13.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "(Speak normally into your phone: 'Haan Papa, main bas 2 minute mein gate par pahunch rahi hoon.')",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = SelrTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // In-Call Controls
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = { isMuted = !isMuted },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isMuted) Color.White else Color(0xFF22293C))
                        ) {
                            Icon(
                                imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (isMuted) Color.Black else Color.White
                            )
                        }

                        IconButton(
                            onClick = { isSpeakerOn = !isSpeakerOn },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isSpeakerOn) Color.White else Color(0xFF22293C))
                        ) {
                            Icon(
                                imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                                contentDescription = "Speaker",
                                tint = if (isSpeakerOn) Color.Black else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // End Call Button
                    IconButton(
                        onClick = onDeclineCall,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD32F2F))
                            .testTag("end_fake_call_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
