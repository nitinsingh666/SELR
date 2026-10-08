package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.TacticalRadarMap
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ParentDashboardScreen(
    studentProfile: StudentProfile,
    currentLocation: LocationData,
    activeEmergency: EmergencyAlert?,
    emergencyHistory: List<EmergencyAlert>,
    safeZones: List<GeoSafeZone>,
    geofenceEvents: List<GeofenceEvent>,
    onCallStudent: () -> Unit,
    onCallPolice: () -> Unit,
    onNavigateToChild: () -> Unit,
    onAddSafeZone: (String, Float) -> Unit,
    onRemoveSafeZone: (String) -> Unit,
    onOpenGoogleMaps: () -> Unit,
    nearbyContacts: List<NearbyContactLocation> = emptyList(),
    onCallContact: (String) -> Unit = {},
    highRiskZones: List<HighRiskZone> = emptyList(),
    highRiskMonitoringState: HighRiskMonitoringState = HighRiskMonitoringState(),
    safeStatusHistory: List<SafeStatusUpdate> = emptyList(),
    modifier: Modifier = Modifier
) {
    var showAddZoneDialog by remember { mutableStateOf(false) }
    var newZoneName by remember { mutableStateOf("") }
    var newZoneRadius by remember { mutableFloatStateOf(400f) }

    val infiniteTransition = rememberInfiniteTransition(label = "parentAlert")
    val alertGlowColor by infiniteTransition.animateColor(
        initialValue = SelrRedDark,
        targetValue = SelrRedBright,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SelrNavyBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Child Overview Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(SelrOlive)
                                .border(2.dp, SelrGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = studentProfile.avatarInitial,
                                fontFamily = AntonFontFamily,
                                fontSize = 24.sp,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MONITORING CHILD",
                                fontFamily = AntonFontFamily,
                                fontSize = 11.sp,
                                color = SelrTextSecondary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = studentProfile.fullName,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "${studentProfile.classRollNo} • ${studentProfile.schoolCollegeName}",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = SelrTextSecondary
                            )
                        }
                    }

                    // Status Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeEmergency != null) SelrRed else SelrSafeGreen)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (activeEmergency != null) "🚨 SOS ACTIVE" else "🛡️ SAFE",
                            fontFamily = AntonFontFamily,
                            fontSize = 11.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        // CRITICAL PUSH ALERT BANNER IF SOS IS ACTIVE
        if (activeEmergency != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, alertGlowColor, RoundedCornerShape(16.dp))
                        .testTag("parent_critical_sos_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF381014)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = SelrRedBright,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "EMERGENCY SOS RECEIVED!",
                                fontFamily = AntonFontFamily,
                                fontSize = 20.sp,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${studentProfile.fullName} triggered Emergency SOS at ${activeEmergency.location.address}. Live tracking active.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 13.sp,
                            color = SelrGold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onCallStudent,
                                modifier = Modifier.weight(1f).testTag("parent_call_student_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SelrSafeGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CALL", fontFamily = AntonFontFamily, fontSize = 12.sp)
                            }

                            Button(
                                onClick = onNavigateToChild,
                                modifier = Modifier.weight(1.3f).testTag("parent_navigate_child_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("NAVIGATE", fontFamily = AntonFontFamily, fontSize = 12.sp)
                            }

                            Button(
                                onClick = onCallPolice,
                                modifier = Modifier.weight(1f).testTag("parent_call_police_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SelrNavyCard),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("112 POLICE", fontFamily = AntonFontFamily, fontSize = 11.sp, color = SelrGold)
                            }
                        }
                    }
                }
            }
        }

        // HIGH-RISK SECTOR AWARENESS BANNER FOR PARENT
        if (highRiskMonitoringState.isInsideHighRiskArea) {
            val minutes = highRiskMonitoringState.nextUpdateSecondsRemaining / 60
            val seconds = highRiskMonitoringState.nextUpdateSecondsRemaining % 60
            val countdown = String.format("%02d:%02d", minutes, seconds)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, Color(0xFFFF9800), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A14))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CHILD IN HIGH-RISK SECTOR",
                                fontFamily = AntonFontFamily,
                                fontSize = 16.sp,
                                color = Color(0xFFFF9800),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${studentProfile.fullName} entered ${highRiskMonitoringState.activeZone?.name ?: "Monitored Danger Zone"}. Recurring 30-minute 'Safe' telemetry active. Next auto-ping in $countdown.",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        if (safeStatusHistory.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Latest: ${safeStatusHistory.first().deliveryStatus}",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                color = SelrSafeGreenBright,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Child's Real-time Location Radar & Map
        item {
            TacticalRadarMap(
                location = currentLocation,
                onOpenGoogleMaps = onOpenGoogleMaps,
                onRefreshLocation = {},
                isEmergencyActive = activeEmergency != null,
                nearbyContacts = nearbyContacts,
                onCallContact = onCallContact,
                subtitle = "CHILD'S REAL-TIME GPS TELEMETRY",
                highRiskZones = highRiskZones,
                activeHighRiskZone = highRiskMonitoringState.activeZone
            )
        }

        // Geo-Fencing Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "GEO-FENCING SAFE ZONES",
                        fontFamily = AntonFontFamily,
                        fontSize = 15.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Instant notification when student enters/leaves zones",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = SelrTextSecondary
                    )
                }

                IconButton(
                    onClick = { showAddZoneDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SelrOlive)
                        .testTag("add_safe_zone_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Safe Zone",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Safe Zones Cards
        items(safeZones) { zone ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (zone.isInside) SelrSafeGreen.copy(alpha = 0.6f) else SelrNavyBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (zone.isInside) SelrSafeGreen.copy(alpha = 0.2f) else SelrNavyCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fence,
                                contentDescription = null,
                                tint = if (zone.isInside) SelrSafeGreenBright else SelrGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = zone.name,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = "Radius: ${zone.radiusMeters.toInt()}m • ${if (zone.isInside) "Child Currently Inside" else "Child Outside Zone"}",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = if (zone.isInside) SelrSafeGreenBright else SelrTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { onRemoveSafeZone(zone.id) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            tint = SelrTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Geofence Event Log
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "RECENT GEOFENCE TRANSITIONS",
                        fontFamily = AntonFontFamily,
                        fontSize = 13.sp,
                        color = SelrGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    geofenceEvents.forEach { event ->
                        val timeStr = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(event.timestamp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (event.eventType == "ENTERED") Icons.Default.Login else Icons.Default.Logout,
                                    contentDescription = null,
                                    tint = if (event.eventType == "ENTERED") SelrSafeGreenBright else SelrGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${event.eventType}: ${event.zoneName}",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = timeStr,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                color = SelrTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Alert History
        item {
            Text(
                text = "CHILD SOS ALERT AUDIT TRAIL",
                fontFamily = AntonFontFamily,
                fontSize = 15.sp,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        items(emergencyHistory) { hist ->
            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(hist.timestamp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${hist.alertType.emoji} ${hist.alertType.title}",
                            fontFamily = AntonFontFamily,
                            fontSize = 13.sp,
                            color = SelrRedBright,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SelrNavyCard)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = hist.status.label,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                color = SelrSafeGreenBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Location: ${hist.location.address}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$dateStr • ${hist.resolutionNotes ?: "Completed safely."}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        color = SelrTextSecondary
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAddZoneDialog) {
        AlertDialog(
            onDismissRequest = { showAddZoneDialog = false },
            title = {
                Text(
                    text = "Add Safe Geo-Fence Zone",
                    fontFamily = AntonFontFamily,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Define a protected area around child's current location.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = SelrTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newZoneName,
                        onValueChange = { newZoneName = it },
                        label = { Text("Zone Name (e.g. Tuition Center, Library)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SelrRed,
                            unfocusedBorderColor = SelrNavyBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Radius: ${newZoneRadius.toInt()} meters",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = SelrGold
                    )
                    Slider(
                        value = newZoneRadius,
                        onValueChange = { newZoneRadius = it },
                        valueRange = 100f..1200f,
                        colors = SliderDefaults.colors(thumbColor = SelrRed, activeTrackColor = SelrRed)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newZoneName.isNotBlank()) {
                            onAddSafeZone(newZoneName, newZoneRadius)
                            newZoneName = ""
                            showAddZoneDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelrRed)
                ) {
                    Text("ADD ZONE", fontFamily = AntonFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddZoneDialog = false }) {
                    Text("CANCEL", color = SelrTextSecondary)
                }
            },
            containerColor = SelrNavyCard
        )
    }
}
