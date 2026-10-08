package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HighRiskMonitoringState
import com.example.model.HighRiskZone
import com.example.model.SafeStatusUpdate
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HighRiskStatusCard(
    monitoringState: HighRiskMonitoringState,
    highRiskZones: List<HighRiskZone>,
    safeStatusHistory: List<SafeStatusUpdate>,
    onSendManualSafeUpdate: () -> Unit,
    onSimulateEnterZone: (HighRiskZone) -> Unit,
    onSimulateExitZone: () -> Unit,
    onToggleTestInterval: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showZonesList by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "riskWarningPulse")
    val warningBorderColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFFF9800),
        targetValue = SelrRedBright,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "warningGlow"
    )

    val minutes = monitoringState.nextUpdateSecondsRemaining / 60
    val seconds = monitoringState.nextUpdateSecondsRemaining % 60
    val countdownText = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.5.dp,
                if (monitoringState.isInsideHighRiskArea) warningBorderColor else SelrNavyBorder,
                RoundedCornerShape(16.dp)
            )
            .testTag("high_risk_monitoring_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (monitoringState.isInsideHighRiskArea) Color(0xFF261815) else SelrNavySurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (monitoringState.isInsideHighRiskArea)
                                    Color(0xFFE53935).copy(alpha = 0.25f)
                                else
                                    Color(0xFFFF9800).copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (monitoringState.isInsideHighRiskArea) Icons.Default.Warning else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (monitoringState.isInsideHighRiskArea) SelrRedBright else Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (monitoringState.isInsideHighRiskArea)
                                "HIGH-RISK AREA ACTIVE"
                            else
                                "HIGH-RISK 'SAFE' BROADCAST GUARD",
                            fontFamily = AntonFontFamily,
                            fontSize = 14.sp,
                            color = if (monitoringState.isInsideHighRiskArea) SelrRedBright else SelrGold,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (monitoringState.isInsideHighRiskArea)
                                monitoringState.activeZone?.name ?: "Monitored Danger Sector"
                            else
                                "Auto 30-min 'Safe' updates on danger entry",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = if (monitoringState.isInsideHighRiskArea) Color.White else SelrTextSecondary,
                            fontWeight = if (monitoringState.isInsideHighRiskArea) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }

                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (monitoringState.isInsideHighRiskArea)
                                SelrRed.copy(alpha = 0.3f)
                            else
                                SelrNavyCard
                        )
                        .border(
                            1.dp,
                            if (monitoringState.isInsideHighRiskArea) SelrRed else SelrNavyBorder,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (monitoringState.isInsideHighRiskArea) "RECURRING ON" else "STANDBY",
                        fontFamily = AntonFontFamily,
                        fontSize = 10.sp,
                        color = if (monitoringState.isInsideHighRiskArea) SelrRedBright else SelrTextMuted,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body when INSIDE High-Risk Area
            if (monitoringState.isInsideHighRiskArea) {
                val zone = monitoringState.activeZone
                if (zone != null) {
                    Text(
                        text = "${zone.description} • Reported incidents: ${zone.reportedIncidents}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = SelrGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 30-Minute Recurring Countdown Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF191F2D))
                        .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(SelrSafeGreenBright)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "NEXT 'SAFE' UPDATE DISPATCH",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 11.sp,
                                    color = SelrSafeGreenBright,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Auto-sent to registered emergency contacts",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                color = SelrTextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = countdownText,
                                fontFamily = AntonFontFamily,
                                fontSize = 28.sp,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (monitoringState.isFastTestInterval) "30s Test Mode" else "30 Min Interval",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 9.sp,
                                color = SelrGold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Buttons inside High-Risk Zone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onSendManualSafeUpdate,
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("send_manual_safe_update_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = SelrSafeGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "SEND 'SAFE' UPDATE NOW",
                            fontFamily = AntonFontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onSimulateExitZone,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("exit_high_risk_zone_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SelrTextSecondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "EXIT ZONE",
                            fontFamily = AntonFontFamily,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            } else {
                // Standby mode - explain feature and allow simulating high-risk entry for testing
                Text(
                    text = "Identified 3 high-risk zones on map (unlit canals, forest paths, underpasses). When student enters, SELR automatically pings registered contacts every 30 minutes with a 'Safe & Secure' status update.",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 11.sp,
                    color = SelrTextSecondary,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showZonesList = !showZonesList },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (showZonesList) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = SelrGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showZonesList) "HIDE HIGH-RISK ZONES" else "VIEW MAP HIGH-RISK ZONES (${highRiskZones.size})",
                            fontFamily = AntonFontFamily,
                            fontSize = 11.sp,
                            color = SelrGold
                        )
                    }

                    if (safeStatusHistory.isNotEmpty()) {
                        TextButton(
                            onClick = { showHistoryDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = SelrSafeGreenBright, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LOG (${safeStatusHistory.size})",
                                fontFamily = AntonFontFamily,
                                fontSize = 11.sp,
                                color = SelrSafeGreenBright
                            )
                        }
                    }
                }

                // Collapsible list of High-Risk Zones on Map
                AnimatedVisibility(visible = showZonesList) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        highRiskZones.forEach { zone ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF141926))
                                    .border(1.dp, SelrNavyBorder, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(zone.riskLevel.badgeColor))
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = zone.name,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "${zone.riskLevel.label} • Radius ${zone.radiusMeters.toInt()}m",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 9.sp,
                                            color = SelrTextSecondary
                                        )
                                    }
                                }

                                Button(
                                    onClick = { onSimulateEnterZone(zone) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SelrOlive),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("enter_zone_${zone.id}")
                                ) {
                                    Text(
                                        text = "TEST ENTRY",
                                        fontFamily = AntonFontFamily,
                                        fontSize = 10.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer info & Test mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${monitoringState.totalUpdatesSent} 'Safe' Broadcasts Sent Total",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 10.sp,
                    color = SelrTextMuted
                )

                TextButton(
                    onClick = onToggleTestInterval,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (monitoringState.isFastTestInterval) "Mode: 30s Fast Demo [ON]" else "Mode: 30m Standard",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        color = if (monitoringState.isFastTestInterval) SelrGold else SelrTextSecondary
                    )
                }
            }
        }
    }

    // Dialog showing log of past 'Safe' Status Updates
    if (showHistoryDialog) {
        val dateFormat = remember { SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()) }
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = {
                Text(
                    text = "'SAFE' STATUS BROADCAST LOG",
                    fontFamily = AntonFontFamily,
                    fontSize = 16.sp,
                    color = SelrSafeGreenBright
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    safeStatusHistory.forEach { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SelrNavyCard),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = item.zoneName,
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = dateFormat.format(Date(item.timestamp)),
                                        fontFamily = PoppinsFontFamily,
                                        fontSize = 9.sp,
                                        color = SelrTextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.messageText,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 10.sp,
                                    color = SelrTextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Status: ${item.deliveryStatus} (${item.recipientsCount} contacts)",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 9.sp,
                                    color = SelrSafeGreenBright,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) {
                    Text("CLOSE", fontFamily = AntonFontFamily, color = SelrGold)
                }
            },
            containerColor = SelrNavySurface
        )
    }
}
