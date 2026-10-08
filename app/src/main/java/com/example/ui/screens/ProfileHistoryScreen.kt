package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.EmergencyContactEntity
import com.example.model.EmergencyAlert
import com.example.model.EmergencyContact
import com.example.model.StudentProfile
import com.example.ui.components.SelrEmblemBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHistoryScreen(
    profile: StudentProfile,
    emergencyHistory: List<EmergencyAlert>,
    onUpdateProfile: (StudentProfile) -> Unit,
    onSendTestSms: (String) -> Unit,
    savedContacts: List<EmergencyContactEntity> = emptyList(),
    onAddContact: (String, String, String, Boolean) -> Unit = { _, _, _, _ -> },
    onUpdateContact: (EmergencyContactEntity) -> Unit = {},
    onDeleteContact: (Long) -> Unit = {},
    isDarkTheme: Boolean = true,
    isDynamicColor: Boolean = false,
    onToggleDarkTheme: () -> Unit = {},
    onToggleDynamicColor: () -> Unit = {},
    onOpenRegistration: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }

    // Dialog state for adding/editing Room emergency contacts
    var showContactDialog by remember { mutableStateOf(false) }
    var editingContact by remember { mutableStateOf<EmergencyContactEntity?>(null) }
    var dialogName by remember { mutableStateOf("") }
    var dialogRelation by remember { mutableStateOf("") }
    var dialogPhone by remember { mutableStateOf("") }
    var dialogIsPrimary by remember { mutableStateOf(false) }

    var fullName by remember(profile) { mutableStateOf(profile.fullName) }
    var classRollNo by remember(profile) { mutableStateOf(profile.classRollNo) }
    var schoolCollegeName by remember(profile) { mutableStateOf(profile.schoolCollegeName) }
    var bloodGroup by remember(profile) { mutableStateOf(profile.bloodGroup) }
    var isHosteller by remember(profile) { mutableStateOf(profile.isHosteller) }
    var mobile by remember(profile) { mutableStateOf(profile.mobile) }

    var c1Name by remember(profile) { mutableStateOf(profile.emergencyContact1.name) }
    var c1Relation by remember(profile) { mutableStateOf(profile.emergencyContact1.relation) }
    var c1Phone by remember(profile) { mutableStateOf(profile.emergencyContact1.phone) }

    var c2Name by remember(profile) { mutableStateOf(profile.emergencyContact2.name) }
    var c2Relation by remember(profile) { mutableStateOf(profile.emergencyContact2.relation) }
    var c2Phone by remember(profile) { mutableStateOf(profile.emergencyContact2.phone) }

    var medicalNotes by remember(profile) { mutableStateOf(profile.medicalNotes) }

    var showSavedSnackbar by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SelrNavyBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(SelrOlive)
                            .border(2.dp, SelrGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.avatarInitial,
                            fontFamily = AntonFontFamily,
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = profile.fullName,
                        fontFamily = AntonFontFamily,
                        fontSize = 22.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "${profile.classRollNo} • ${profile.schoolCollegeName}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = SelrTextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BadgePill("BLOOD: ${profile.bloodGroup}", SelrRed)
                        BadgePill(if (profile.isHosteller) "HOSTELLER" else "DAY-SCHOLAR", SelrOlive)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isEditing = !isEditing },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isEditing) SelrNavyCard else SelrOlive
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.Default.Close else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEditing) "CANCEL" else "EDIT",
                                fontFamily = AntonFontFamily,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onOpenRegistration,
                            colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("profile_open_registration_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AppRegistration,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ENROLL NEW",
                                fontFamily = AntonFontFamily,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // EDIT FORM OR READ-ONLY VIEW
        if (isEditing) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelrRed.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "EDIT PROFILE DETAILS",
                            fontFamily = AntonFontFamily,
                            fontSize = 14.sp,
                            color = SelrGold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = classRollNo,
                            onValueChange = { classRollNo = it },
                            label = { Text("Class / Roll No") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = schoolCollegeName,
                            onValueChange = { schoolCollegeName = it },
                            label = { Text("School / College Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("Mobile Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            colors = editFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "PRIMARY EMERGENCY CONTACT",
                            fontFamily = AntonFontFamily,
                            fontSize = 12.sp,
                            color = SelrTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = c1Name,
                            onValueChange = { c1Name = it },
                            label = { Text("Contact 1 Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editFieldColors()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = c1Relation,
                                onValueChange = { c1Relation = it },
                                label = { Text("Relation") },
                                modifier = Modifier.weight(0.45f),
                                colors = editFieldColors()
                            )
                            OutlinedTextField(
                                value = c1Phone,
                                onValueChange = { c1Phone = it },
                                label = { Text("Phone") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(0.55f),
                                colors = editFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "SECONDARY EMERGENCY CONTACT",
                            fontFamily = AntonFontFamily,
                            fontSize = 12.sp,
                            color = SelrTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = c2Name,
                            onValueChange = { c2Name = it },
                            label = { Text("Contact 2 Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editFieldColors()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = c2Relation,
                                onValueChange = { c2Relation = it },
                                label = { Text("Relation") },
                                modifier = Modifier.weight(0.45f),
                                colors = editFieldColors()
                            )
                            OutlinedTextField(
                                value = c2Phone,
                                onValueChange = { c2Phone = it },
                                label = { Text("Phone") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.weight(0.55f),
                                colors = editFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = medicalNotes,
                            onValueChange = { medicalNotes = it },
                            label = { Text("Medical Information / Allergies") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editFieldColors()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val updated = profile.copy(
                                    fullName = fullName,
                                    classRollNo = classRollNo,
                                    schoolCollegeName = schoolCollegeName,
                                    mobile = mobile,
                                    emergencyContact1 = EmergencyContact(c1Name, c1Relation, c1Phone),
                                    emergencyContact2 = EmergencyContact(c2Name, c2Relation, c2Phone),
                                    medicalNotes = medicalNotes
                                )
                                onUpdateProfile(updated)
                                isEditing = false
                                showSavedSnackbar = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_profile_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("SAVE RESCUE PROFILE", fontFamily = AntonFontFamily, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            // Room Database Emergency Contacts Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "EMERGENCY CONTACTS",
                                        fontFamily = AntonFontFamily,
                                        fontSize = 14.sp,
                                        color = SelrGold,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(SelrOlive.copy(alpha = 0.3f))
                                            .border(1.dp, SelrOlive, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ROOM DB",
                                            fontFamily = AntonFontFamily,
                                            fontSize = 9.sp,
                                            color = SelrSafeGreenBright
                                        )
                                    }
                                }
                                Text(
                                    text = "Auto-alerted via SMS upon 3-sec SOS activation",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 11.sp,
                                    color = SelrTextSecondary
                                )
                            }

                            Button(
                                onClick = {
                                    editingContact = null
                                    dialogName = ""
                                    dialogRelation = "Family"
                                    dialogPhone = "+91 "
                                    dialogIsPrimary = false
                                    showContactDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("add_emergency_contact_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ADD", fontFamily = AntonFontFamily, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (savedContacts.isEmpty()) {
                            Text(
                                text = "No contacts saved. Click ADD to configure notification recipients.",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 12.sp,
                                color = SelrTextSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            savedContacts.forEach { contact ->
                                RoomContactRow(
                                    contact = contact,
                                    onEdit = {
                                        editingContact = contact
                                        dialogName = contact.name
                                        dialogRelation = contact.relation
                                        dialogPhone = contact.phone
                                        dialogIsPrimary = contact.isPrimary
                                        showContactDialog = true
                                    },
                                    onDelete = { onDeleteContact(contact.id) },
                                    onSendTest = { onSendTestSms(contact.phone) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "CRITICAL MEDICAL RECORD",
                            fontFamily = AntonFontFamily,
                            fontSize = 13.sp,
                            color = SelrRedBright,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Blood Group: ${profile.bloodGroup}",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = profile.medicalNotes,
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = SelrTextSecondary
                        )
                    }
                }
            }
        }

        // M3 THEME & NIGHT-TIME EMERGENCY MODE SETTINGS
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "NIGHT-TIME EMERGENCY MODE",
                                fontFamily = AntonFontFamily,
                                fontSize = 13.sp,
                                color = SelrGold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isDarkTheme) "High-contrast dark mode enabled for night rescue" else "Standard day-time visibility mode active",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = SelrTextSecondary
                            )
                        }

                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { onToggleDarkTheme() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SelrRed,
                                checkedTrackColor = SelrRedContainer,
                                uncheckedThumbColor = SelrTextSecondary,
                                uncheckedTrackColor = SelrNavyCard
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = SelrNavyBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DYNAMIC M3 SYSTEM COLORS",
                                fontFamily = AntonFontFamily,
                                fontSize = 13.sp,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Adapts system palette from device wallpaper (Android 12+)",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = SelrTextSecondary
                            )
                        }

                        Switch(
                            checked = isDynamicColor,
                            onCheckedChange = { onToggleDynamicColor() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SelrOlive,
                                checkedTrackColor = SelrOliveMuted,
                                uncheckedThumbColor = SelrTextSecondary,
                                uncheckedTrackColor = SelrNavyCard
                            )
                        )
                    }
                }
            }
        }

        // FULL LOG OF SOS EVENTS
        item {
            Text(
                text = "FULL SOS EMERGENCY EVENT LOG",
                fontFamily = AntonFontFamily,
                fontSize = 16.sp,
                color = Color.White,
                letterSpacing = 1.sp
            )
        }

        if (emergencyHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "No past SOS emergency incidents recorded. All safe.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = SelrSafeGreenBright,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        items(emergencyHistory) { alert ->
            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(alert.timestamp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${alert.alertType.emoji} ${alert.alertType.title}",
                            fontFamily = AntonFontFamily,
                            fontSize = 14.sp,
                            color = SelrRedBright
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SelrNavyCard)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = alert.status.label,
                                fontFamily = PoppinsFontFamily,
                                fontSize = 10.sp,
                                color = SelrSafeGreenBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Location: ${alert.location.address}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Coordinates: ${alert.location.latitude}, ${alert.location.longitude}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        color = SelrTextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "$dateStr • Resolution: ${alert.resolutionNotes ?: "Safe resolution verified."}",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 10.sp,
                        color = SelrGold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialog for Adding or Editing Emergency Contact in Room Database
    if (showContactDialog) {
        AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = {
                Text(
                    text = if (editingContact != null) "Edit Emergency Contact" else "Add Emergency Contact",
                    fontFamily = AntonFontFamily,
                    fontSize = 18.sp,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Saved securely to local Room database for SOS auto-dispatch.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 11.sp,
                        color = SelrTextSecondary
                    )

                    OutlinedTextField(
                        value = dialogName,
                        onValueChange = { dialogName = it },
                        label = { Text("Contact Name") },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_contact_name_input"),
                        colors = editFieldColors()
                    )

                    OutlinedTextField(
                        value = dialogRelation,
                        onValueChange = { dialogRelation = it },
                        label = { Text("Relationship (Father, Mother, Warden...)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = editFieldColors()
                    )

                    OutlinedTextField(
                        value = dialogPhone,
                        onValueChange = { dialogPhone = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("dialog_contact_phone_input"),
                        colors = editFieldColors()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Primary SOS Recipient",
                            fontFamily = PoppinsFontFamily,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Switch(
                            checked = dialogIsPrimary,
                            onCheckedChange = { dialogIsPrimary = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SelrGold,
                                checkedTrackColor = SelrNavyCard
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (dialogName.isNotBlank() && dialogPhone.isNotBlank()) {
                            if (editingContact != null) {
                                onUpdateContact(
                                    editingContact!!.copy(
                                        name = dialogName,
                                        relation = dialogRelation,
                                        phone = dialogPhone,
                                        isPrimary = dialogIsPrimary
                                    )
                                )
                            } else {
                                onAddContact(dialogName, dialogRelation, dialogPhone, dialogIsPrimary)
                            }
                            showContactDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_contact_dialog_btn")
                ) {
                    Text(
                        text = if (editingContact != null) "UPDATE CONTACT" else "SAVE CONTACT",
                        fontFamily = AntonFontFamily,
                        fontSize = 12.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = false }) {
                    Text("CANCEL", color = SelrTextSecondary)
                }
            },
            containerColor = SelrNavySurface
        )
    }
}

@Composable
private fun RoomContactRow(
    contact: EmergencyContactEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSendTest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SelrNavyCard)
            .border(
                1.dp,
                if (contact.isPrimary) SelrGold.copy(alpha = 0.6f) else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.name,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (contact.isPrimary) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SelrGold)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "PRIMARY",
                            fontFamily = AntonFontFamily,
                            fontSize = 8.sp,
                            color = Color.Black
                        )
                    }
                }
            }
            Text(
                text = "${contact.relation} • ${contact.phone}",
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = SelrTextSecondary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSendTest, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Test SMS",
                    tint = SelrGold,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Contact",
                    tint = SelrTextSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Contact",
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

@Composable
private fun ContactItem(
    contact: EmergencyContact,
    isPrimary: Boolean,
    onSendTest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(SelrNavyCard)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${contact.name} (${contact.relation})",
                fontFamily = PoppinsFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "${contact.phone} • ${if (isPrimary) "Primary SOS Recipient" else "Secondary Backup"}",
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = if (isPrimary) SelrGold else SelrTextSecondary
            )
        }

        IconButton(onClick = onSendTest, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Test SMS",
                tint = SelrGold,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun BadgePill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontFamily = AntonFontFamily,
            fontSize = 10.sp,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = SelrRed,
    unfocusedBorderColor = SelrNavyBorder,
    focusedLabelColor = SelrRed,
    unfocusedLabelColor = SelrTextSecondary,
    focusedContainerColor = SelrNavyCard,
    unfocusedContainerColor = SelrNavyCard
)
