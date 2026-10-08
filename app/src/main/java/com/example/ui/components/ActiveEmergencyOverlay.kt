package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmergencyAlert
import com.example.model.NearbyContactLocation
import com.example.ui.components.TacticalRadarMap
import com.example.ui.theme.*

@Composable
fun ActiveEmergencyOverlay(
    alert: EmergencyAlert,
    remainingSeconds: Int,
    isSirenMuted: Boolean,
    onToggleSiren: () -> Unit,
    onCancelEmergency: (String) -> Unit,
    onOpenGoogleMaps: () -> Unit,
    onCallEmergency112: () -> Unit,
    nearbyContacts: List<NearbyContactLocation> = emptyList(),
    onCallContact: (String) -> Unit = {},
    onRefreshLocation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "alertFlash")
    val flashColor by infiniteTransition.animateColor(
        initialValue = SelrRedDark,
        targetValue = SelrRedBright,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SelrNavyBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Flashing Emergency Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(flashColor.copy(alpha = 0.2f))
                .border(2.dp, flashColor, RoundedCornerShape(16.dp))
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = SelrRedBright,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HELP IS ON THE WAY!",
                        fontFamily = AntonFontFamily,
                        fontSize = 24.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your live location is shared with parents & teachers.",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    color = SelrGold,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Sharing Timer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LIVE RESCUE TRANSMISSION ACTIVE",
                    fontFamily = AntonFontFamily,
                    fontSize = 13.sp,
                    color = SelrSafeGreenBright,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = timeFormatted,
                    fontFamily = AntonFontFamily,
                    fontSize = 48.sp,
                    color = Color.White,
                    letterSpacing = 2.sp
                )

                Text(
                    text = "Active tracking session remaining",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = SelrTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tactical Radar Map showing User GPS & Nearby Responders Convergence
        TacticalRadarMap(
            location = alert.location,
            onOpenGoogleMaps = onOpenGoogleMaps,
            onRefreshLocation = onRefreshLocation,
            isEmergencyActive = true,
            nearbyContacts = nearbyContacts,
            onCallContact = onCallContact,
            subtitle = "ACTIVE SOS RESCUE RADAR & CONVERGENCE"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dispatch status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DISPATCH & RESCUE LOG",
                    fontFamily = AntonFontFamily,
                    fontSize = 14.sp,
                    color = SelrTextSecondary,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                DispatchStatusRow(
                    label = "Parent / Emergency Contacts",
                    detail = "Auto-sent SMS with live Google Maps link",
                    isSuccess = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                DispatchStatusRow(
                    label = "Class Teacher / Faculty",
                    detail = "Urgent high-priority push dispatch notified",
                    isSuccess = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                DispatchStatusRow(
                    label = "Campus Security Control",
                    detail = "Active telemetry packet broadcasted",
                    isSuccess = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Siren Controls & Police Fast Call
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onToggleSiren,
                modifier = Modifier.weight(1f).testTag("toggle_siren_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSirenMuted) SelrNavyCard else Color(0xFFD32F2F)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = if (isSirenMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSirenMuted) "UNMUTE SIREN" else "MUTE SIREN",
                    fontFamily = AntonFontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Button(
                onClick = onCallEmergency112,
                modifier = Modifier.weight(1f).testTag("call_police_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SelrNavyCard
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalPolice,
                    contentDescription = null,
                    tint = SelrGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CALL 112",
                    fontFamily = AntonFontFamily,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cancel button for false alarm
        OutlinedButton(
            onClick = { showCancelDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cancel_sos_emergency_btn"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = SelrTextSecondary),
            border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "CANCEL EMERGENCY (FALSE ALARM)",
                fontFamily = AntonFontFamily,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = "Cancel SOS Alert?",
                    fontFamily = AntonFontFamily,
                    fontSize = 20.sp,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to stop live rescue sharing? Parents and Faculty will be informed that the alert has been stood down.",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    color = SelrTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCancelDialog = false
                        onCancelEmergency("Student stood down alert safely.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("YES, CANCEL ALERT", fontFamily = AntonFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("KEEP ACTIVE", color = SelrTextSecondary, fontFamily = AntonFontFamily)
                }
            },
            containerColor = SelrNavyCard
        )
    }
}

@Composable
private fun DispatchStatusRow(
    label: String,
    detail: String,
    isSuccess: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSuccess) SelrSafeGreen.copy(alpha = 0.2f) else SelrNavyBorder),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SelrSafeGreenBright,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = label,
                fontFamily = PoppinsFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = detail,
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = SelrTextSecondary
            )
        }
    }
}
