package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AlertType
import com.example.model.UserRole
import com.example.ui.components.ActiveEmergencyOverlay
import com.example.ui.components.FakeIncomingCallScreen
import com.example.ui.components.SelrEmblemBadge
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.SelrViewModel

enum class AppDestination(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("DEFENSE", Icons.Default.Shield),
    MONITOR("TRACKER", Icons.Default.LocationOn),
    ZONES("SAFE ZONES", Icons.Default.Fence),
    DISPATCH("DISPATCH", Icons.Default.LocalPolice),
    REGISTER("REGISTER", Icons.Default.AppRegistration),
    PROFILE("PROFILE", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelrMainApp(
    viewModel: SelrViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // App state
    var showSplash by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf(true) } // starts authenticated with demo, can switch or sign-in anytime
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    // ViewModel flows
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val studentProfile by viewModel.studentProfile.collectAsStateWithLifecycle()
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val activeEmergency by viewModel.activeEmergency.collectAsStateWithLifecycle()
    val emergencyHistory by viewModel.emergencyHistory.collectAsStateWithLifecycle()
    val safeZones by viewModel.safeZones.collectAsStateWithLifecycle()
    val geofenceEvents by viewModel.geofenceEvents.collectAsStateWithLifecycle()
    val isSirenMuted by viewModel.isSirenMuted.collectAsStateWithLifecycle()
    val deviceStatus by viewModel.deviceStatus.collectAsStateWithLifecycle()
    val fakeCallState by viewModel.fakeCallState.collectAsStateWithLifecycle()
    val remainingCountdown by viewModel.remainingCountdownSeconds.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
    val isDynamicColor by viewModel.isDynamicColor.collectAsStateWithLifecycle()
    val emergencyContacts by viewModel.emergencyContacts.collectAsStateWithLifecycle()
    val nearbyResponders by viewModel.nearbyResponders.collectAsStateWithLifecycle()
    val highRiskZones by viewModel.highRiskZones.collectAsStateWithLifecycle()
    val highRiskMonitoringState by viewModel.highRiskMonitoringState.collectAsStateWithLifecycle()
    val safeStatusHistory by viewModel.safeStatusHistory.collectAsStateWithLifecycle()
    val isLocationTrackingRunning by viewModel.isLocationTrackingRunning.collectAsStateWithLifecycle()
    val liveTrackingLocation by viewModel.liveTrackingLocation.collectAsStateWithLifecycle()
    val trackingUpdatesCount by viewModel.trackingUpdatesPushedCount.collectAsStateWithLifecycle()

    if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
        return
    }

    if (!isAuthenticated) {
        AuthScreen(
            onRoleAuthenticated = { role, identifier, name ->
                viewModel.selectRole(role, identifier, name)
                isAuthenticated = true
            },
            onStudentRegistered = { newProfile ->
                viewModel.registerStudent(newProfile)
                isAuthenticated = true
            }
        )
        return
    }

    // Active Emergency Overlay for Student
    if (activeEmergency != null && currentSession.role == UserRole.STUDENT) {
        ActiveEmergencyOverlay(
            alert = activeEmergency!!,
            remainingSeconds = remainingCountdown,
            isSirenMuted = isSirenMuted,
            onToggleSiren = { viewModel.toggleSirenMute() },
            onCancelEmergency = { reason -> viewModel.cancelEmergency(reason) },
            onOpenGoogleMaps = {
                viewModel.notificationHelper.openMapsNavigation(
                    activeEmergency!!.location.latitude,
                    activeEmergency!!.location.longitude
                )
            },
            onCallEmergency112 = {
                viewModel.notificationHelper.makePhoneCall("112")
            },
            nearbyContacts = nearbyResponders,
            onCallContact = { phone -> viewModel.notificationHelper.makePhoneCall(phone) },
            onRefreshLocation = { viewModel.refreshLocation() }
        )
        return
    }

    // Fake Call Overlay
    if (fakeCallState.isRinging || fakeCallState.isInCall) {
        FakeIncomingCallScreen(
            fakeCallState = fakeCallState,
            onAcceptCall = { viewModel.acceptFakeCall() },
            onDeclineCall = { viewModel.declineOrEndFakeCall() }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SelrEmblemBadge(size = 38.dp, showBannerText = false)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SELR",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 18.sp,
                                    color = SelrRed,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SelrNavyCard)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = currentSession.role.name,
                                        fontFamily = AntonFontFamily,
                                        fontSize = 10.sp,
                                        color = SelrGold
                                    )
                                }
                            }
                            Text(
                                text = "LEAD • PROTECT • RESCUE",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 9.sp,
                                color = SelrTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Direct Registration Form Button
                    IconButton(
                        onClick = { currentDestination = AppDestination.REGISTER },
                        modifier = Modifier.testTag("topbar_register_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AppRegistration,
                            contentDescription = "Registration Form",
                            tint = if (currentDestination == AppDestination.REGISTER) SelrRedBright else SelrGold
                        )
                    }

                    // Dark / Light Mode Toggle Button
                    IconButton(
                        onClick = { viewModel.toggleDarkTheme() },
                        modifier = Modifier.testTag("toggle_dark_mode_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Switch to Day Mode" else "Night Emergency Mode",
                            tint = if (isDarkTheme) SelrGold else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Quick Role Switch Dropdown
                    Box {
                        IconButton(
                            onClick = { roleMenuExpanded = true },
                            modifier = Modifier.testTag("switch_role_menu_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Role",
                                tint = SelrGold
                            )
                        }

                        DropdownMenu(
                            expanded = roleMenuExpanded,
                            onDismissRequest = { roleMenuExpanded = false },
                            modifier = Modifier.background(SelrNavyCard)
                        ) {
                            Text(
                                text = "SWITCH VIEW / ROLE",
                                fontFamily = AntonFontFamily,
                                fontSize = 11.sp,
                                color = SelrGold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                            UserRole.values().forEach { role ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${role.displayName} ${if (role == currentSession.role) "✓" else ""}",
                                            color = if (role == currentSession.role) SelrRedBright else Color.White,
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectRole(role, "${role.name.lowercase()}@selr.edu")
                                        roleMenuExpanded = false
                                        currentDestination = AppDestination.HOME
                                    }
                                )
                            }
                            Divider(color = SelrNavyBorder)
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AppRegistration,
                                            contentDescription = null,
                                            tint = SelrRedBright,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Registration & Enrollment Form",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                },
                                onClick = {
                                    roleMenuExpanded = false
                                    currentDestination = AppDestination.REGISTER
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                            contentDescription = null,
                                            tint = SelrGold,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isDarkTheme) "Night Mode (Active)" else "Day Mode (Active)",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.toggleDarkTheme()
                                    roleMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ColorLens,
                                            contentDescription = null,
                                            tint = SelrOliveAccent,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isDynamicColor) "Dynamic M3 Colors: ON" else "Dynamic M3 Colors: OFF",
                                            fontFamily = PoppinsFontFamily,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.toggleDynamicColor()
                                    roleMenuExpanded = false
                                }
                            )
                            Divider(color = SelrNavyBorder)
                            DropdownMenuItem(
                                text = { Text("Log Out / Re-Enroll", color = Color(0xFFEF5350)) },
                                onClick = {
                                    roleMenuExpanded = false
                                    isAuthenticated = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SelrNavySurface,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SelrNavySurface,
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                // Navigation items tailored for the active role
                val destinations = when (currentSession.role) {
                    UserRole.STUDENT -> listOf(AppDestination.HOME, AppDestination.ZONES, AppDestination.REGISTER, AppDestination.PROFILE)
                    UserRole.PARENT -> listOf(AppDestination.MONITOR, AppDestination.ZONES, AppDestination.REGISTER, AppDestination.PROFILE)
                    UserRole.TEACHER, UserRole.ADMIN -> listOf(AppDestination.DISPATCH, AppDestination.MONITOR, AppDestination.REGISTER, AppDestination.PROFILE)
                }

                destinations.forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.title,
                                tint = if (isSelected) SelrRedBright else SelrTextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = dest.title,
                                fontFamily = AntonFontFamily,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else SelrTextMuted,
                                letterSpacing = 0.5.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = SelrRed.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.HOME -> {
                    if (currentSession.role == UserRole.STUDENT) {
                        StudentHomeScreen(
                            studentProfile = studentProfile,
                            currentLocation = currentLocation,
                            deviceStatus = deviceStatus,
                            onTriggerSos = { viewModel.triggerCriticalSos() },
                            onShareLocation = {
                                val text = "🚨 SELR LOCATION SHARE: ${studentProfile.fullName} is currently at: ${currentLocation.googleMapsUrl} (${currentLocation.address})"
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, text)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Location via"))
                            },
                            onMedicalEmergency = { viewModel.triggerQuickHelp(AlertType.MEDICAL) },
                            onNeedHelp = { viewModel.triggerQuickHelp(AlertType.NEED_HELP) },
                            onFakeSafeCall = { viewModel.scheduleFakeSafeCall(delaySeconds = 1) },
                            onOpenGoogleMaps = {
                                viewModel.notificationHelper.openMapsNavigation(
                                    currentLocation.latitude,
                                    currentLocation.longitude
                                )
                            },
                            onRefreshLocation = { viewModel.refreshLocation() },
                            isEmergencyActive = activeEmergency != null,
                            nearbyContacts = nearbyResponders,
                            onCallContact = { phone -> viewModel.notificationHelper.makePhoneCall(phone) },
                            highRiskZones = highRiskZones,
                            highRiskMonitoringState = highRiskMonitoringState,
                            safeStatusHistory = safeStatusHistory,
                            onSendManualSafeUpdate = { viewModel.sendManualSafeStatusUpdate() },
                            onSimulateEnterZone = { zone -> viewModel.simulateEnterHighRiskZone(zone) },
                            onSimulateExitZone = { viewModel.simulateExitHighRiskZone() },
                            onToggleTestInterval = { viewModel.toggleHighRiskTestInterval() },
                            isLocationTrackingRunning = isLocationTrackingRunning,
                            onToggleLocationTracking = { viewModel.toggleBackgroundLocationTracking() },
                            liveTrackingLocation = liveTrackingLocation,
                            trackingUpdatesCount = trackingUpdatesCount
                        )
                    } else if (currentSession.role == UserRole.PARENT) {
                        ParentDashboardScreen(
                            studentProfile = studentProfile,
                            currentLocation = currentLocation,
                            activeEmergency = activeEmergency,
                            emergencyHistory = emergencyHistory,
                            safeZones = safeZones,
                            geofenceEvents = geofenceEvents,
                            onCallStudent = { viewModel.notificationHelper.makePhoneCall(studentProfile.mobile) },
                            onCallPolice = { viewModel.notificationHelper.makePhoneCall("112") },
                            onNavigateToChild = {
                                viewModel.notificationHelper.openMapsNavigation(
                                    currentLocation.latitude,
                                    currentLocation.longitude
                                )
                            },
                            onAddSafeZone = { name, radius -> viewModel.addSafeZone(name, radius) },
                            onRemoveSafeZone = { id -> viewModel.removeSafeZone(id) },
                            onOpenGoogleMaps = {
                                viewModel.notificationHelper.openMapsNavigation(
                                    currentLocation.latitude,
                                    currentLocation.longitude
                                )
                            },
                            nearbyContacts = nearbyResponders,
                            onCallContact = { phone -> viewModel.notificationHelper.makePhoneCall(phone) },
                            highRiskZones = highRiskZones,
                            highRiskMonitoringState = highRiskMonitoringState,
                            safeStatusHistory = safeStatusHistory
                        )
                    } else {
                        TeacherAdminScreen(
                            currentRole = currentSession.role,
                            allStudents = allStudents,
                            activeEmergency = activeEmergency,
                            onCallStudent = { phone -> viewModel.notificationHelper.makePhoneCall(phone) },
                            onNavigateToLocation = { lat, lng ->
                                viewModel.notificationHelper.openMapsNavigation(lat, lng)
                            },
                            onResolveEmergency = { id, notes -> viewModel.resolveEmergency(id, notes) },
                            nearbyContacts = nearbyResponders,
                            onCallContact = { phone -> viewModel.notificationHelper.makePhoneCall(phone) }
                        )
                    }
                }

                AppDestination.MONITOR -> {
                    ParentDashboardScreen(
                        studentProfile = studentProfile,
                        currentLocation = currentLocation,
                        activeEmergency = activeEmergency,
                        emergencyHistory = emergencyHistory,
                        safeZones = safeZones,
                        geofenceEvents = geofenceEvents,
                        onCallStudent = { viewModel.notificationHelper.makePhoneCall(studentProfile.mobile) },
                        onCallPolice = { viewModel.notificationHelper.makePhoneCall("112") },
                        onNavigateToChild = {
                            viewModel.notificationHelper.openMapsNavigation(
                                currentLocation.latitude,
                                currentLocation.longitude
                            )
                        },
                        onAddSafeZone = { name, radius -> viewModel.addSafeZone(name, radius) },
                        onRemoveSafeZone = { id -> viewModel.removeSafeZone(id) },
                        onOpenGoogleMaps = {
                            viewModel.notificationHelper.openMapsNavigation(
                                currentLocation.latitude,
                                currentLocation.longitude
                            )
                        },
                        nearbyContacts = nearbyResponders,
                        onCallContact = { phone -> viewModel.notificationHelper.makePhoneCall(phone) },
                        highRiskZones = highRiskZones,
                        highRiskMonitoringState = highRiskMonitoringState,
                        safeStatusHistory = safeStatusHistory
                    )
                }

                AppDestination.ZONES -> {
                    ParentDashboardScreen(
                        studentProfile = studentProfile,
                        currentLocation = currentLocation,
                        activeEmergency = activeEmergency,
                        emergencyHistory = emergencyHistory,
                        safeZones = safeZones,
                        geofenceEvents = geofenceEvents,
                        onCallStudent = { viewModel.notificationHelper.makePhoneCall(studentProfile.mobile) },
                        onCallPolice = { viewModel.notificationHelper.makePhoneCall("112") },
                        onNavigateToChild = {
                            viewModel.notificationHelper.openMapsNavigation(
                                currentLocation.latitude,
                                currentLocation.longitude
                            )
                        },
                        onAddSafeZone = { name, radius -> viewModel.addSafeZone(name, radius) },
                        onRemoveSafeZone = { id -> viewModel.removeSafeZone(id) },
                        onOpenGoogleMaps = {
                            viewModel.notificationHelper.openMapsNavigation(
                                currentLocation.latitude,
                                currentLocation.longitude
                            )
                        },
                        nearbyContacts = nearbyResponders,
                        onCallContact = { phone -> viewModel.notificationHelper.makePhoneCall(phone) },
                        highRiskZones = highRiskZones,
                        highRiskMonitoringState = highRiskMonitoringState,
                        safeStatusHistory = safeStatusHistory
                    )
                }

                AppDestination.DISPATCH -> {
                    TeacherAdminScreen(
                        currentRole = currentSession.role,
                        allStudents = allStudents,
                        activeEmergency = activeEmergency,
                        onCallStudent = { phone -> viewModel.notificationHelper.makePhoneCall(phone) },
                        onNavigateToLocation = { lat, lng ->
                            viewModel.notificationHelper.openMapsNavigation(lat, lng)
                        },
                        onResolveEmergency = { id, notes -> viewModel.resolveEmergency(id, notes) },
                        nearbyContacts = nearbyResponders,
                        onCallContact = { phone -> viewModel.notificationHelper.makePhoneCall(phone) }
                    )
                }

                AppDestination.REGISTER -> {
                    RegistrationScreen(
                        onStudentRegistered = { profile ->
                            viewModel.registerStudent(profile)
                        },
                        onSaveEmergencyContact = { name, relation, phone, isPrimary ->
                            viewModel.saveEmergencyContact(name, relation, phone, isPrimary)
                        },
                        onRoleRegistered = { role, identifier, name ->
                            viewModel.selectRole(role, identifier, name)
                        },
                        onNavigateToHome = {
                            currentDestination = AppDestination.HOME
                        }
                    )
                }

                AppDestination.PROFILE -> {
                    ProfileHistoryScreen(
                        profile = studentProfile,
                        emergencyHistory = emergencyHistory,
                        onUpdateProfile = { updated -> viewModel.updateStudentProfile(updated) },
                        onSendTestSms = { phone ->
                            viewModel.notificationHelper.sendEmergencySmsIntent(
                                listOf(phone),
                                "SELR TEST DISPATCH: Emergency rescue communication line verified for ${studentProfile.fullName}."
                            )
                        },
                        savedContacts = emergencyContacts,
                        onAddContact = { name, relation, phone, isPrimary ->
                            viewModel.saveEmergencyContact(name, relation, phone, isPrimary)
                        },
                        onUpdateContact = { contact ->
                            viewModel.updateEmergencyContact(contact)
                        },
                        onDeleteContact = { id ->
                            viewModel.deleteEmergencyContact(id)
                        },
                        isDarkTheme = isDarkTheme,
                        isDynamicColor = isDynamicColor,
                        onToggleDarkTheme = { viewModel.toggleDarkTheme() },
                        onToggleDynamicColor = { viewModel.toggleDynamicColor() },
                        onOpenRegistration = { currentDestination = AppDestination.REGISTER }
                    )
                }
            }
        }
    }
}
