package com.example.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firestore.FirestoreUserLiveLocation
import com.example.model.DeviceStatus
import com.example.model.HighRiskMonitoringState
import com.example.model.HighRiskZone
import com.example.model.LocationData
import com.example.model.NearbyContactLocation
import com.example.model.SafeStatusUpdate
import com.example.model.StudentProfile
import com.example.ui.components.HighRiskStatusCard
import com.example.ui.components.QuickHelpGrid
import com.example.ui.components.SelrEmblemBadge
import com.example.ui.components.SosEmergencyButton
import com.example.ui.components.TacticalRadarMap
import com.example.ui.theme.*

@Composable
fun StudentHomeScreen(
    studentProfile: StudentProfile,
    currentLocation: LocationData,
    deviceStatus: DeviceStatus,
    onTriggerSos: () -> Unit,
    onShareLocation: () -> Unit,
    onMedicalEmergency: () -> Unit,
    onNeedHelp: () -> Unit,
    onFakeSafeCall: () -> Unit,
    onOpenGoogleMaps: () -> Unit,
    onRefreshLocation: () -> Unit,
    isEmergencyActive: Boolean = false,
    nearbyContacts: List<NearbyContactLocation> = emptyList(),
    onCallContact: (String) -> Unit = {},
    highRiskZones: List<HighRiskZone> = emptyList(),
    highRiskMonitoringState: HighRiskMonitoringState = HighRiskMonitoringState(),
    safeStatusHistory: List<SafeStatusUpdate> = emptyList(),
    onSendManualSafeUpdate: () -> Unit = {},
    onSimulateEnterZone: (HighRiskZone) -> Unit = {},
    onSimulateExitZone: () -> Unit = {},
    onToggleTestInterval: () -> Unit = {},
    isLocationTrackingRunning: Boolean = false,
    onToggleLocationTracking: () -> Unit = {},
    liveTrackingLocation: FirestoreUserLiveLocation? = null,
    trackingUpdatesCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SelrNavyBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Home Header Bar with SELR Crest
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SelrEmblemBadge(size = 56.dp, showBannerText = false)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "SELR DEFENSE",
                        fontFamily = AntonFontFamily,
                        fontSize = 18.sp,
                        color = SelrRed,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = studentProfile.fullName,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "${studentProfile.classRollNo} • Blood: ${studentProfile.bloodGroup}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        color = SelrTextSecondary
                    )
                }
            }

            // Hosteller / Day scholar tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (studentProfile.isHosteller) SelrOlive else SelrNavyCard)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (studentProfile.isHosteller) "HOSTELLER" else "DAY-SCHOLAR",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 9.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Top Status Card: "You Are Safe" in Green
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.5.dp, SelrSafeGreenBright, RoundedCornerShape(14.dp))
                .testTag("you_are_safe_card"),
            colors = CardDefaults.cardColors(
                containerColor = SelrSafeGreenContainer.copy(alpha = 0.85f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SelrSafeGreen)
                        .border(1.dp, SelrSafeGreenBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Safe Shield",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "YOU ARE SAFE",
                        fontFamily = AntonFontFamily,
                        fontSize = 18.sp,
                        color = SelrSafeGreenBright,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "SELR Active Shield Guarding • Rescue hotlines linked",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live GPS Tactical Radar Map
        TacticalRadarMap(
            location = currentLocation,
            onOpenGoogleMaps = onOpenGoogleMaps,
            onRefreshLocation = onRefreshLocation,
            isEmergencyActive = isEmergencyActive,
            nearbyContacts = nearbyContacts,
            onCallContact = onCallContact,
            subtitle = if (isEmergencyActive) "ACTIVE EMERGENCY RESCUE GRID" else "LIVE GPS POSITIONING",
            highRiskZones = highRiskZones,
            activeHighRiskZone = highRiskMonitoringState.activeZone,
            onSelectHighRiskZone = onSimulateEnterZone
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Recurring 30-Minute Safe Status Updates Card for High-Risk Areas
        HighRiskStatusCard(
            monitoringState = highRiskMonitoringState,
            highRiskZones = highRiskZones,
            safeStatusHistory = safeStatusHistory,
            onSendManualSafeUpdate = onSendManualSafeUpdate,
            onSimulateEnterZone = onSimulateEnterZone,
            onSimulateExitZone = onSimulateExitZone,
            onToggleTestInterval = onToggleTestInterval
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Real-Time Background Location Tracking Card (LocationManager -> Firestore)
        Card(
            modifier = Modifier.fillMaxWidth().testTag("background_tracking_card"),
            colors = CardDefaults.cardColors(
                containerColor = if (isLocationTrackingRunning) SelrNavySurface else SelrNavySurface.copy(alpha = 0.85f)
            ),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isLocationTrackingRunning) SelrOliveAccent else SelrNavyBorder
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isLocationTrackingRunning) SelrOliveAccent.copy(alpha = 0.2f) else SelrNavyBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = "Background GPS",
                                tint = if (isLocationTrackingRunning) SelrOliveAccent else SelrTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "BACKGROUND GPS TRACKING",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isLocationTrackingRunning) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SelrOliveAccent)
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "LIVE CLOUD SYNC",
                                            fontFamily = AntonFontFamily,
                                            fontSize = 9.sp,
                                            color = SelrNavyBg,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                            Text(
                                text = if (isLocationTrackingRunning)
                                    "LocationManager streaming coordinates to Firestore"
                                else
                                    "Foreground service idle • Switch ON to stream",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = SelrTextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isLocationTrackingRunning,
                        onCheckedChange = { onToggleLocationTracking() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SelrOliveAccent,
                            uncheckedThumbColor = SelrTextSecondary,
                            uncheckedTrackColor = SelrNavyBg
                        ),
                        modifier = Modifier.testTag("toggle_background_tracking")
                    )
                }

                if (isLocationTrackingRunning && liveTrackingLocation != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = SelrNavyBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CURRENT SYNCED COORDINATES",
                                fontFamily = AntonFontFamily,
                                fontSize = 10.sp,
                                color = SelrTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${"%.5f".format(liveTrackingLocation.latitude)}° N, ${"%.5f".format(liveTrackingLocation.longitude)}° E",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "SYNC SEQUENCE",
                                fontFamily = AntonFontFamily,
                                fontSize = 10.sp,
                                color = SelrTextSecondary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "#$trackingUpdatesCount (${liveTrackingLocation.provider.uppercase()})",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SelrGold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // CENTER BIG RED SOS BUTTON
        SosEmergencyButton(
            onTriggerSos = onTriggerSos
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 4 Quick Help Buttons
        QuickHelpGrid(
            onShareLocation = onShareLocation,
            onMedicalEmergency = onMedicalEmergency,
            onNeedHelp = onNeedHelp,
            onFakeSafeCall = onFakeSafeCall
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Bottom Telemetry: Battery, Signal, GPS Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Battery
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (deviceStatus.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = "Battery",
                        tint = if (deviceStatus.batteryPercent > 20) SelrSafeGreenBright else SelrRedBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${deviceStatus.batteryPercent}%",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Signal
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = "Signal",
                        tint = SelrGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = deviceStatus.signalStrength,
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }

                // GPS status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "GPS",
                        tint = SelrSafeGreenBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "GPS 4m",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = SelrSafeGreenBright,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
