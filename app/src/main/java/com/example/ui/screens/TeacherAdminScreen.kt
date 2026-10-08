package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.model.EmergencyAlert
import com.example.model.NearbyContactLocation
import com.example.model.StudentProfile
import com.example.model.UserRole
import com.example.ui.components.TacticalRadarMap
import com.example.ui.theme.*

@Composable
fun TeacherAdminScreen(
    currentRole: UserRole,
    allStudents: List<StudentProfile>,
    activeEmergency: EmergencyAlert?,
    onCallStudent: (String) -> Unit,
    onNavigateToLocation: (Double, Double) -> Unit,
    onResolveEmergency: (String, String) -> Unit,
    nearbyContacts: List<NearbyContactLocation> = emptyList(),
    onCallContact: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterHostellerOnly by remember { mutableStateOf(false) }
    var showResolveDialog by remember { mutableStateOf(false) }
    var resolveNotes by remember { mutableStateOf("Faculty reached student. Student safe and in campus medical bay.") }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastMessage by remember { mutableStateOf("All faculty please note: Campus security drill active. Guide students to designated assembly zones.") }
    var broadcastSentSuccess by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "adminFlash")
    val borderFlash by infiniteTransition.animateColor(
        initialValue = SelrRedDark,
        targetValue = SelrRedBright,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )

    val totalStudentsCount = 348 + allStudents.size
    val activeEmergenciesCount = if (activeEmergency != null) 1 else 0
    val safeStudentsCount = totalStudentsCount - activeEmergenciesCount

    val filteredStudents = allStudents.filter { student ->
        (student.fullName.contains(searchQuery, ignoreCase = true) ||
                student.classRollNo.contains(searchQuery, ignoreCase = true)) &&
                (!filterHostellerOnly || student.isHosteller)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SelrNavyBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Console Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (currentRole == UserRole.ADMIN) "CAMPUS RESCUE COMMAND" else "FACULTY DISPATCH PANEL",
                        fontFamily = AntonFontFamily,
                        fontSize = 20.sp,
                        color = SelrRed,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "National Defense & Technology Institute",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = SelrTextSecondary
                    )
                }

                Button(
                    onClick = { showBroadcastDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SelrOlive),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("BROADCAST", fontFamily = AntonFontFamily, fontSize = 11.sp)
                }
            }
        }

        // 3 Key Metrics Cards: Total Students, Safe Students, Active Emergencies
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "TOTAL",
                    value = "$totalStudentsCount",
                    subtitle = "Enrolled",
                    accentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "SAFE",
                    value = "$safeStudentsCount",
                    subtitle = "Shielded",
                    accentColor = SelrSafeGreenBright,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "ACTIVE SOS",
                    value = "$activeEmergenciesCount",
                    subtitle = if (activeEmergenciesCount > 0) "CRITICAL" else "Zero",
                    accentColor = if (activeEmergenciesCount > 0) SelrRedBright else SelrGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // ACTIVE EMERGENCIES HIGHLIGHTED IN RED (Most Important)
        if (activeEmergency != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.5.dp, borderFlash, RoundedCornerShape(16.dp))
                        .testTag("teacher_active_emergency_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF381014))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(borderFlash)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACTIVE CRITICAL EMERGENCY",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 17.sp,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SelrRed)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "LIVE RESCUE",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Student Details
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(SelrOlive),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeEmergency.studentName.take(1),
                                    fontFamily = AntonFontFamily,
                                    fontSize = 22.sp,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = activeEmergency.studentName,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Class: ${activeEmergency.studentClass} • Blood: ${activeEmergency.bloodGroup} • ${if (activeEmergency.isHosteller) "Hosteller" else "Day-Scholar"}",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = SelrGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Location
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SelrNavySurface)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = SelrRedBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = activeEmergency.location.address,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Live Map Component displaying Student GPS Location and Nearby Responders
                        TacticalRadarMap(
                            location = activeEmergency.location,
                            onOpenGoogleMaps = { onNavigateToLocation(activeEmergency.location.latitude, activeEmergency.location.longitude) },
                            onRefreshLocation = {},
                            isEmergencyActive = true,
                            nearbyContacts = nearbyContacts,
                            onCallContact = onCallContact,
                            subtitle = "STUDENT LIVE GPS TELEMETRY & RESPONDERS"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Call, Navigate, Resolve
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onCallStudent(activeEmergency.studentPhone) },
                                modifier = Modifier.weight(1f).testTag("admin_call_student_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SelrSafeGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CALL", fontFamily = AntonFontFamily, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { onNavigateToLocation(activeEmergency.location.latitude, activeEmergency.location.longitude) },
                                modifier = Modifier.weight(1.2f).testTag("admin_navigate_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("NAVIGATE", fontFamily = AntonFontFamily, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showResolveDialog = true },
                                modifier = Modifier.weight(1.3f).testTag("admin_resolve_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = SelrOlive),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("STAND DOWN", fontFamily = AntonFontFamily, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Student Directory Header & Search
        item {
            Column {
                Text(
                    text = "STUDENT SAFETY DIRECTORY",
                    fontFamily = AntonFontFamily,
                    fontSize = 15.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, roll no, or section...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = SelrTextSecondary)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("student_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SelrRed,
                        unfocusedBorderColor = SelrNavyBorder,
                        focusedContainerColor = SelrNavySurface,
                        unfocusedContainerColor = SelrNavySurface
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = filterHostellerOnly,
                        onClick = { filterHostellerOnly = !filterHostellerOnly },
                        label = { Text("Hostellers Only") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SelrOlive,
                            selectedLabelColor = Color.White
                        )
                    )

                    Text(
                        text = "Showing ${filteredStudents.size} students",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = SelrTextSecondary
                    )
                }
            }
        }

        // Student List
        items(filteredStudents) { student ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(SelrNavyCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = student.avatarInitial,
                                    fontFamily = AntonFontFamily,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = student.fullName,
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${student.classRollNo} • Blood: ${student.bloodGroup}",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = SelrTextSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (student.isHosteller) SelrOlive else SelrNavyCard)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (student.isHosteller) "HOSTELLER" else "DAY-SCHOLAR",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (expanded) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = SelrNavyBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Mobile: ${student.mobile}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Emergency 1: ${student.emergencyContact1.name} (${student.emergencyContact1.relation}) - ${student.emergencyContact1.phone}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = SelrGold
                        )
                        Text(
                            text = "Emergency 2: ${student.emergencyContact2.name} (${student.emergencyContact2.relation}) - ${student.emergencyContact2.phone}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = SelrTextSecondary
                        )
                        Text(
                            text = "Medical Notes: ${student.medicalNotes}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 11.sp,
                            color = SelrTextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { onCallStudent(student.mobile) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SelrGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SelrGold.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Direct Contact Student", fontFamily = AntonFontFamily, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog for Resolving Emergency
    if (showResolveDialog && activeEmergency != null) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = {
                Text(
                    text = "Stand Down & Resolve Emergency",
                    fontFamily = AntonFontFamily,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Verify that ${activeEmergency.studentName} is safely accounted for before closing the live rescue channel.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = SelrTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = resolveNotes,
                        onValueChange = { resolveNotes = it },
                        label = { Text("Resolution Dispatch Log") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SelrSafeGreen,
                            unfocusedBorderColor = SelrNavyBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResolveEmergency(activeEmergency.id, resolveNotes)
                        showResolveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelrSafeGreen)
                ) {
                    Text("CONFIRM RESOLVED", fontFamily = AntonFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) {
                    Text("CANCEL", color = SelrTextSecondary)
                }
            },
            containerColor = SelrNavyCard
        )
    }

    // Dialog for Campus Broadcast
    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = {
                Text(
                    text = "Broadcast Campus Advisory",
                    fontFamily = AntonFontFamily,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Text(
                        text = "Send instant notification to all active student & faculty devices.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = SelrTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = broadcastMessage,
                        onValueChange = { broadcastMessage = it },
                        label = { Text("Advisory Message") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = SelrRed,
                            unfocusedBorderColor = SelrNavyBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBroadcastDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelrRed)
                ) {
                    Text("TRANSMIT BROADCAST", fontFamily = AntonFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("CANCEL", color = SelrTextSecondary)
                }
            },
            containerColor = SelrNavyCard
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontFamily = AntonFontFamily,
                fontSize = 12.sp,
                color = SelrTextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = AntonFontFamily,
                fontSize = 26.sp,
                color = accentColor,
                letterSpacing = 0.5.sp
            )
            Text(
                text = subtitle,
                fontFamily = PoppinsFontFamily,
                fontSize = 10.sp,
                color = SelrTextMuted
            )
        }
    }
}
