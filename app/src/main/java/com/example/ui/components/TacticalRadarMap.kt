package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LocationData
import com.example.model.NearbyContactLocation
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

enum class MapVisualMode {
    RADAR,
    STREET_GRID
}

@Composable
fun TacticalRadarMap(
    location: LocationData,
    onOpenGoogleMaps: () -> Unit,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier,
    isEmergencyActive: Boolean = false,
    nearbyContacts: List<NearbyContactLocation> = emptyList(),
    onCallContact: (String) -> Unit = {},
    subtitle: String = "LIVE RESCUE TELEMETRY",
    highRiskZones: List<com.example.model.HighRiskZone> = emptyList(),
    activeHighRiskZone: com.example.model.HighRiskZone? = null,
    onSelectHighRiskZone: (com.example.model.HighRiskZone) -> Unit = {}
) {
    var selectedContact by remember { mutableStateOf<NearbyContactLocation?>(null) }
    var visualMode by remember { mutableStateOf(MapVisualMode.RADAR) }

    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    val beaconPulse by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconPulse"
    )

    val contactBlipPulse by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "contactBlipPulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.5.dp,
                if (isEmergencyActive) SelrRed else SelrOliveAccent.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .testTag("tactical_map_component"),
        colors = CardDefaults.cardColors(
            containerColor = SelrNavySurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Title, Responders count, Mode Toggle, Refresh
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isEmergencyActive) SelrRedBright else SelrSafeGreenBright)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subtitle,
                        fontFamily = AntonFontFamily,
                        fontSize = 13.sp,
                        color = if (isEmergencyActive) SelrRedBright else SelrSafeGreenBright,
                        letterSpacing = 1.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // View mode switcher: Radar / Grid
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SelrNavyCard)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (visualMode == MapVisualMode.RADAR) SelrOlive else Color.Transparent)
                                .clickable { visualMode = MapVisualMode.RADAR }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "RADAR",
                                fontFamily = AntonFontFamily,
                                fontSize = 9.sp,
                                color = if (visualMode == MapVisualMode.RADAR) Color.White else SelrTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (visualMode == MapVisualMode.STREET_GRID) SelrOlive else Color.Transparent)
                                .clickable { visualMode = MapVisualMode.STREET_GRID }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "STREET",
                                fontFamily = AntonFontFamily,
                                fontSize = 9.sp,
                                color = if (visualMode == MapVisualMode.STREET_GRID) Color.White else SelrTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onRefreshLocation,
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("refresh_location_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh GPS",
                            tint = SelrTextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Emergency Alert Active Responders Banner
            if (isEmergencyActive && nearbyContacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(SelrRedDark.copy(alpha = 0.4f))
                        .border(1.dp, SelrRed, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = SelrRedBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${nearbyContacts.size} RESPONDERS DETECTED NEARBY",
                            fontFamily = AntonFontFamily,
                            fontSize = 11.sp,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "NEAREST: ${nearbyContacts.minOfOrNull { it.distanceKm } ?: 0.4f} KM",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        color = SelrGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // High-Risk Area Active Alert Banner on Map
            if (activeHighRiskZone != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF381512))
                        .border(1.2.dp, Color(0xFFFF9800), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HIGH-RISK: ${activeHighRiskZone.name.uppercase()}",
                            fontFamily = AntonFontFamily,
                            fontSize = 11.sp,
                            color = Color(0xFFFF9800),
                            letterSpacing = 0.5.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SelrSafeGreen.copy(alpha = 0.3f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "30-MIN 'SAFE' BROADCAST ON",
                            fontFamily = AntonFontFamily,
                            fontSize = 8.5.sp,
                            color = SelrSafeGreenBright,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Map Visualizer Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0C101A))
                    .border(1.dp, SelrNavyBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val maxRadius = (size.height / 2) * 0.88f

                    if (visualMode == MapVisualMode.RADAR) {
                        drawTacticalRadar(
                            center = center,
                            maxRadius = maxRadius,
                            sweepAngle = sweepAngle,
                            isEmergencyActive = isEmergencyActive,
                            beaconPulse = beaconPulse,
                            contactBlipPulse = contactBlipPulse,
                            nearbyContacts = nearbyContacts,
                            selectedContact = selectedContact,
                            highRiskZones = highRiskZones,
                            activeHighRiskZone = activeHighRiskZone
                        )
                    } else {
                        drawStreetGridMap(
                            center = center,
                            isEmergencyActive = isEmergencyActive,
                            beaconPulse = beaconPulse,
                            contactBlipPulse = contactBlipPulse,
                            nearbyContacts = nearbyContacts,
                            selectedContact = selectedContact,
                            highRiskZones = highRiskZones,
                            activeHighRiskZone = activeHighRiskZone
                        )
                    }
                }

                // Range labels (for Radar mode)
                if (visualMode == MapVisualMode.RADAR) {
                    Text(
                        text = "500m",
                        color = SelrTextMuted,
                        fontSize = 8.sp,
                        fontFamily = PoppinsFontFamily,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = 28.dp, y = (-8).dp)
                    )
                    Text(
                        text = "1km",
                        color = SelrTextMuted,
                        fontSize = 8.sp,
                        fontFamily = PoppinsFontFamily,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = 58.dp, y = (-8).dp)
                    )
                    Text(
                        text = "2km",
                        color = SelrTextMuted,
                        fontSize = 8.sp,
                        fontFamily = PoppinsFontFamily,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = 88.dp, y = (-8).dp)
                    )
                } else {
                    // Street Grid Compass
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF161B28).copy(alpha = 0.85f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CAMPUS GRID MAP",
                            fontFamily = AntonFontFamily,
                            fontSize = 8.sp,
                            color = SelrGold
                        )
                    }
                }

                // Coordinates pill top-left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF161B28).copy(alpha = 0.88f))
                        .border(0.5.dp, SelrNavyBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LAT: ${"%.4f".format(location.latitude)}  LNG: ${"%.4f".format(location.longitude)}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        color = SelrTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Accuracy badge top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF161B28).copy(alpha = 0.88f))
                        .border(0.5.dp, SelrNavyBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ACCURACY: ±${location.accuracyMeters}m",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        color = SelrSafeGreenBright,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Center Location Pin for Student / User
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-14).dp)
                        .testTag("gps_current_location_marker"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isEmergencyActive) SelrRed else SelrSafeGreen)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isEmergencyActive) "SOS" else "YOU",
                            fontFamily = AntonFontFamily,
                            fontSize = 8.sp,
                            color = Color.White
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Current GPS Location",
                        tint = if (isEmergencyActive) SelrRedBright else SelrSafeGreenBright,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Tap Target Overlay for Responders on Radar
                nearbyContacts.forEach { contact ->
                    val angleRad = Math.toRadians((contact.bearingDegrees - 90.0))
                    val normalizedDist = (contact.distanceKm / 2.5f).coerceIn(0.28f, 0.88f)
                    val xOffset = (normalizedDist * 100 * cos(angleRad)).toInt().dp
                    val yOffset = (normalizedDist * 85 * sin(angleRad)).toInt().dp

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = xOffset, y = yOffset)
                            .size(32.dp)
                            .clickable {
                                selectedContact = if (selectedContact?.id == contact.id) null else contact
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Empty clickable target over radar blip
                    }
                }
            }

            // Focused Responder Callout Card (When tapped on Map)
            AnimatedVisibility(
                visible = selectedContact != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                selectedContact?.let { contact ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, SelrGold, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2638))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SelrGold.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = null,
                                        tint = SelrGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = contact.name,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${contact.relation} • ${contact.distanceKm} km away • ETA ~${contact.etaMinutes} min",
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 10.sp,
                                        color = SelrGold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { onCallContact(contact.phone) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SelrSafeGreen),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("CALL", fontFamily = AntonFontFamily, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { selectedContact = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SelrTextSecondary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Street Address Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SelrNavyCard)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = null,
                    tint = SelrGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = location.address,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = Color.White,
                    lineHeight = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            // NEARBY EMERGENCY RESPONDER & CONTACT LOCATIONS LIST
            if (isEmergencyActive || nearbyContacts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEmergencyActive) "ACTIVE RESPONDERS & CONTACTS" else "NEARBY RESCUE NETWORK",
                        fontFamily = AntonFontFamily,
                        fontSize = 12.sp,
                        color = if (isEmergencyActive) SelrRedBright else SelrGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "TAP TO FOCUS ON MAP",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 9.sp,
                        color = SelrTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    nearbyContacts.forEach { contact ->
                        val isSelected = selectedContact?.id == contact.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) SelrGold else SelrNavyBorder,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedContact = if (isSelected) null else contact },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1E2638) else Color(0xFF141926)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (contact.relation.contains("Police", true))
                                                    SelrRed.copy(alpha = 0.25f)
                                                else if (contact.relation.contains("Security", true))
                                                    SelrOlive.copy(alpha = 0.35f)
                                                else
                                                    SelrGold.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when {
                                                contact.relation.contains("Police", true) -> Icons.Default.LocalPolice
                                                contact.relation.contains("Security", true) -> Icons.Default.Security
                                                else -> Icons.Default.PersonPinCircle
                                            },
                                            contentDescription = null,
                                            tint = when {
                                                contact.relation.contains("Police", true) -> SelrRedBright
                                                contact.relation.contains("Security", true) -> SelrOliveAccent
                                                else -> SelrGold
                                            },
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = contact.name,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${contact.relation} • ${contact.distanceKm} km (~${contact.etaMinutes}m ETA)",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 10.sp,
                                            color = SelrTextSecondary
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (contact.status == "Approaching" || contact.status == "En Route")
                                                    SelrSafeGreen.copy(alpha = 0.25f)
                                                else
                                                    SelrNavyCard
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = contact.status.uppercase(),
                                            fontFamily = AntonFontFamily,
                                            fontSize = 9.sp,
                                            color = if (contact.status == "Approaching" || contact.status == "En Route")
                                                SelrSafeGreenBright
                                            else
                                                SelrGold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { onCallContact(contact.phone) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = "Call Contact",
                                            tint = SelrGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Open in Google Maps Button
            OutlinedButton(
                onClick = onOpenGoogleMaps,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_google_maps_btn"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SelrTextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, SelrOliveAccent.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = SelrGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VIEW IN GOOGLE MAPS",
                    fontFamily = AntonFontFamily,
                    fontSize = 13.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

private fun DrawScope.drawTacticalRadar(
    center: Offset,
    maxRadius: Float,
    sweepAngle: Float,
    isEmergencyActive: Boolean,
    beaconPulse: Float,
    contactBlipPulse: Float,
    nearbyContacts: List<NearbyContactLocation>,
    selectedContact: NearbyContactLocation?,
    highRiskZones: List<com.example.model.HighRiskZone> = emptyList(),
    activeHighRiskZone: com.example.model.HighRiskZone? = null
) {
    val gridColor = if (isEmergencyActive) Color(0xFF5A1618) else Color(0xFF283A20)

    // Concentric range rings
    drawCircle(gridColor, radius = maxRadius * 0.33f, center = center, style = Stroke(1f))
    drawCircle(gridColor, radius = maxRadius * 0.66f, center = center, style = Stroke(1f))
    drawCircle(gridColor, radius = maxRadius, center = center, style = Stroke(1.5f))

    // Crosshairs
    drawLine(
        gridColor,
        start = Offset(center.x - maxRadius, center.y),
        end = Offset(center.x + maxRadius, center.y),
        strokeWidth = 1f
    )
    drawLine(
        gridColor,
        start = Offset(center.x, center.y - maxRadius),
        end = Offset(center.x, center.y + maxRadius),
        strokeWidth = 1f
    )

    // Sweeping beam
    val rad = Math.toRadians(sweepAngle.toDouble())
    val sweepEnd = Offset(
        (center.x + maxRadius * cos(rad)).toFloat(),
        (center.y + maxRadius * sin(rad)).toFloat()
    )
    drawLine(
        brush = Brush.radialGradient(
            colors = listOf(
                (if (isEmergencyActive) SelrRed else SelrOliveAccent).copy(alpha = 0.85f),
                Color.Transparent
            ),
            center = center,
            radius = maxRadius
        ),
        start = center,
        end = sweepEnd,
        strokeWidth = 2.5f
    )

    // Draw Identified High-Risk Zones on Radar
    highRiskZones.forEachIndexed { index, zone ->
        val zoneAngleRad = Math.toRadians(when (index) {
            0 -> 35.0
            1 -> 205.0
            else -> 125.0
        })
        val normalizedDist = when (index) {
            0 -> 0.58f
            1 -> 0.72f
            else -> 0.62f
        }
        val zoneCenter = Offset(
            (center.x + maxRadius * normalizedDist * cos(zoneAngleRad)).toFloat(),
            (center.y + maxRadius * normalizedDist * sin(zoneAngleRad)).toFloat()
        )
        val zoneRadiusPx = maxRadius * (zone.radiusMeters / 2500f).coerceIn(0.18f, 0.32f)

        // Shaded hazard fill
        drawCircle(
            color = Color(zone.riskLevel.badgeColor).copy(alpha = 0.16f),
            radius = zoneRadiusPx,
            center = zoneCenter
        )
        // Dashed perimeter boundary
        drawCircle(
            color = Color(zone.riskLevel.badgeColor).copy(alpha = 0.75f),
            radius = zoneRadiusPx,
            center = zoneCenter,
            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
        )
        // Center hazard blip
        drawCircle(
            color = Color(zone.riskLevel.badgeColor),
            radius = 3.dp.toPx(),
            center = zoneCenter
        )
    }

    // Plot Nearby Emergency Contacts on Radar
    nearbyContacts.forEach { contact ->
        val angleRad = Math.toRadians((contact.bearingDegrees - 90.0))
        val normalizedDist = (contact.distanceKm / 2.5f).coerceIn(0.28f, 0.88f)
        val blipOffset = Offset(
            (center.x + maxRadius * normalizedDist * cos(angleRad)).toFloat(),
            (center.y + maxRadius * normalizedDist * sin(angleRad)).toFloat()
        )

        val isSelected = selectedContact?.id == contact.id
        val blipColor = when {
            isSelected -> SelrGold
            contact.relation.contains("Police", true) -> SelrRedBright
            contact.relation.contains("Security", true) -> SelrOliveAccent
            else -> SelrGold
        }

        // Draw connecting vector trajectory if selected or in active emergency
        if (isSelected || isEmergencyActive) {
            drawLine(
                color = blipColor.copy(alpha = if (isSelected) 0.8f else 0.4f),
                start = blipOffset,
                end = center,
                strokeWidth = if (isSelected) 2.5f else 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Contact halo pulse
        drawCircle(
            color = blipColor.copy(alpha = if (isSelected) 0.5f else 0.3f),
            radius = if (isSelected) contactBlipPulse * 1.5f else contactBlipPulse,
            center = blipOffset
        )

        // Contact core dot
        drawCircle(
            color = blipColor,
            radius = if (isSelected) 6.dp.toPx() else 4.5.dp.toPx(),
            center = blipOffset
        )
    }

    // User GPS center beacon pulse ring
    val beaconColor = when {
        isEmergencyActive -> SelrRed
        activeHighRiskZone != null -> Color(0xFFFF9800)
        else -> SelrSafeGreenBright
    }
    drawCircle(
        color = beaconColor.copy(alpha = 0.35f),
        radius = beaconPulse,
        center = center
    )
    // User core center dot
    drawCircle(
        color = beaconColor,
        radius = 5.dp.toPx(),
        center = center
    )
}

private fun DrawScope.drawStreetGridMap(
    center: Offset,
    isEmergencyActive: Boolean,
    beaconPulse: Float,
    contactBlipPulse: Float,
    nearbyContacts: List<NearbyContactLocation>,
    selectedContact: NearbyContactLocation?,
    highRiskZones: List<com.example.model.HighRiskZone> = emptyList(),
    activeHighRiskZone: com.example.model.HighRiskZone? = null
) {
    val roadColor = Color(0xFF1E2638)
    val avenueColor = Color(0xFF28334D)

    // Draw grid of campus avenues and streets
    val step = 42f
    var x = center.x % step
    while (x < size.width) {
        drawLine(
            color = if (((x / step).toInt()) % 3 == 0) avenueColor else roadColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = if (((x / step).toInt()) % 3 == 0) 3f else 1.5f
        )
        x += step
    }

    var y = center.y % step
    while (y < size.height) {
        drawLine(
            color = if (((y / step).toInt()) % 3 == 0) avenueColor else roadColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = if (((y / step).toInt()) % 3 == 0) 3f else 1.5f
        )
        y += step
    }

    // Diagonal arterial road
    drawLine(
        color = Color(0xFF334155),
        start = Offset(0f, size.height * 0.8f),
        end = Offset(size.width, size.height * 0.2f),
        strokeWidth = 4f
    )

    // Draw high-risk danger areas on street grid
    highRiskZones.forEachIndexed { index, zone ->
        val zoneAngleRad = Math.toRadians(when (index) {
            0 -> 35.0
            1 -> 205.0
            else -> 125.0
        })
        val normalizedDist = when (index) {
            0 -> 0.58f
            1 -> 0.72f
            else -> 0.62f
        }
        val zoneCenter = Offset(
            (center.x + (size.width * 0.42f) * normalizedDist * cos(zoneAngleRad)).toFloat(),
            (center.y + (size.height * 0.42f) * normalizedDist * sin(zoneAngleRad)).toFloat()
        )
        val zoneRadiusPx = (size.height * 0.42f) * (zone.radiusMeters / 2500f).coerceIn(0.18f, 0.32f)

        drawCircle(
            color = Color(zone.riskLevel.badgeColor).copy(alpha = 0.16f),
            radius = zoneRadiusPx,
            center = zoneCenter
        )
        drawCircle(
            color = Color(zone.riskLevel.badgeColor).copy(alpha = 0.8f),
            radius = zoneRadiusPx,
            center = zoneCenter,
            style = Stroke(width = 1.8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f))
        )
    }

    // Plot responders on the street grid
    nearbyContacts.forEach { contact ->
        val angleRad = Math.toRadians((contact.bearingDegrees - 90.0))
        val normalizedDist = (contact.distanceKm / 2.5f).coerceIn(0.28f, 0.88f)
        val blipOffset = Offset(
            (center.x + (size.width * 0.42f) * normalizedDist * cos(angleRad)).toFloat(),
            (center.y + (size.height * 0.42f) * normalizedDist * sin(angleRad)).toFloat()
        )

        val isSelected = selectedContact?.id == contact.id
        val blipColor = when {
            isSelected -> SelrGold
            contact.relation.contains("Police", true) -> SelrRedBright
            contact.relation.contains("Security", true) -> SelrOliveAccent
            else -> SelrGold
        }

        // Route line to user
        drawLine(
            color = blipColor.copy(alpha = if (isSelected) 0.85f else 0.45f),
            start = blipOffset,
            end = center,
            strokeWidth = if (isSelected) 2.5f else 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        )

        drawCircle(
            color = blipColor.copy(alpha = 0.35f),
            radius = if (isSelected) contactBlipPulse * 1.5f else contactBlipPulse,
            center = blipOffset
        )
        drawCircle(
            color = blipColor,
            radius = if (isSelected) 6.dp.toPx() else 5.dp.toPx(),
            center = blipOffset
        )
    }

    // User GPS center beacon
    val beaconColor = when {
        isEmergencyActive -> SelrRed
        activeHighRiskZone != null -> Color(0xFFFF9800)
        else -> SelrSafeGreenBright
    }
    drawCircle(
        color = beaconColor.copy(alpha = 0.35f),
        radius = beaconPulse,
        center = center
    )
    drawCircle(
        color = beaconColor,
        radius = 5.dp.toPx(),
        center = center
    )
}
