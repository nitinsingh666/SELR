package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EmergencyContact
import com.example.model.StudentProfile
import com.example.model.UserRole
import com.example.ui.components.SelrEmblemBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onStudentRegistered: (StudentProfile) -> Unit,
    onSaveEmergencyContact: (name: String, relation: String, phone: String, isPrimary: Boolean) -> Unit,
    onRoleRegistered: (UserRole, String, String) -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var selectedRegistrationRole by remember { mutableStateOf(UserRole.STUDENT) }

    // Student Form Fields
    var fullName by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var classDepartment by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    var isHosteller by remember { mutableStateOf(false) }
    var hostelWingOrBusRoute by remember { mutableStateOf("") }
    var medicalNotes by remember { mutableStateOf("") }

    // Emergency Contact 1 (Primary)
    var contact1Name by remember { mutableStateOf("") }
    var contact1Relation by remember { mutableStateOf("Father") }
    var contact1Phone by remember { mutableStateOf("") }

    // Emergency Contact 2 (Secondary)
    var contact2Name by remember { mutableStateOf("") }
    var contact2Relation by remember { mutableStateOf("Mother") }
    var contact2Phone by remember { mutableStateOf("") }

    // Parent Form Specific Fields
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("") }
    var studentWardRollNo by remember { mutableStateOf("") }
    var parentRelation by remember { mutableStateOf("Father") }

    // Faculty Form Specific Fields
    var facultyName by remember { mutableStateOf("") }
    var facultyEmployeeId by remember { mutableStateOf("") }
    var facultyDepartment by remember { mutableStateOf("") }
    var facultyPhone by remember { mutableStateOf("") }
    var facultyCampusPost by remember { mutableStateOf("") }

    // Validation State
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")
    val relationOptions = listOf("Father", "Mother", "Guardian", "Brother", "Sister", "Teacher", "Doctor")

    fun fillSampleData() {
        when (selectedRegistrationRole) {
            UserRole.STUDENT -> {
                fullName = "Rohan Varma"
                rollNo = "IITD-2024-CS088"
                mobile = "+91 98765 12345"
                email = "rohan.varma@iitd.ac.in"
                institution = "Indian Institute of Technology Delhi"
                classDepartment = "Computer Science & Engg (3rd Year)"
                bloodGroup = "B+"
                isHosteller = true
                hostelWingOrBusRoute = "Nilgiri Hostel, Room 312"
                medicalNotes = "Asthma (Carries inhaler in backpack). No penicillin allergy."
                contact1Name = "Sanjay Varma"
                contact1Relation = "Father"
                contact1Phone = "+91 98111 55667"
                contact2Name = "Meenakshi Varma"
                contact2Relation = "Mother"
                contact2Phone = "+91 98222 66778"
            }
            UserRole.PARENT -> {
                parentName = "Sanjay Varma"
                parentRelation = "Father"
                parentPhone = "+91 98111 55667"
                parentEmail = "sanjay.varma@gmail.com"
                studentWardRollNo = "IITD-2024-CS088 (Rohan Varma)"
            }
            UserRole.TEACHER, UserRole.ADMIN -> {
                facultyName = "Prof. Anita Deshmukh"
                facultyEmployeeId = "FAC-CS-409"
                facultyDepartment = "Computer Science & Security Cell"
                facultyPhone = "+91 11 2659 1010"
                facultyCampusPost = "Academic Block II, Chief Proctor Office"
            }
        }
    }

    fun clearForm() {
        fullName = ""
        rollNo = ""
        mobile = ""
        email = ""
        institution = ""
        classDepartment = ""
        bloodGroup = "O+"
        isHosteller = false
        hostelWingOrBusRoute = ""
        medicalNotes = ""
        contact1Name = ""
        contact1Phone = ""
        contact2Name = ""
        contact2Phone = ""
        parentName = ""
        parentPhone = ""
        parentEmail = ""
        studentWardRollNo = ""
        facultyName = ""
        facultyEmployeeId = ""
        facultyDepartment = ""
        facultyPhone = ""
        facultyCampusPost = ""
    }

    fun validateAndSubmit() {
        focusManager.clearFocus()
        when (selectedRegistrationRole) {
            UserRole.STUDENT -> {
                if (fullName.isBlank()) {
                    errorMessage = "Please enter student's full name."
                    showErrorDialog = true
                    return
                }
                if (rollNo.isBlank()) {
                    errorMessage = "Please enter student's Roll No or Enrollment ID."
                    showErrorDialog = true
                    return
                }
                if (mobile.isBlank()) {
                    errorMessage = "Please enter student's mobile number for SOS verification."
                    showErrorDialog = true
                    return
                }
                if (contact1Name.isBlank() || contact1Phone.isBlank()) {
                    errorMessage = "Primary emergency contact (Name & Phone) is required for safety dispatch."
                    showErrorDialog = true
                    return
                }

                val newProfile = StudentProfile(
                    id = "stu_${System.currentTimeMillis().toString().takeLast(6)}",
                    fullName = fullName.trim(),
                    classRollNo = "${classDepartment.ifBlank { "Dept General" }} / ${rollNo.trim()}",
                    schoolCollegeName = institution.ifBlank { "Delhi Technological University" }.trim(),
                    bloodGroup = bloodGroup,
                    isHosteller = isHosteller,
                    mobile = mobile.trim(),
                    emergencyContact1 = EmergencyContact(
                        name = contact1Name.trim(),
                        relation = contact1Relation,
                        phone = contact1Phone.trim()
                    ),
                    emergencyContact2 = EmergencyContact(
                        name = contact2Name.ifBlank { "Campus Security 112" }.trim(),
                        relation = contact2Relation,
                        phone = contact2Phone.ifBlank { "112" }.trim()
                    ),
                    medicalNotes = medicalNotes.ifBlank { "No known drug allergies reported." },
                    avatarInitial = fullName.trim().firstOrNull()?.uppercase() ?: "S"
                )

                // Save profile to ViewModel
                onStudentRegistered(newProfile)

                // Save contacts to Room & Firestore
                onSaveEmergencyContact(contact1Name.trim(), contact1Relation, contact1Phone.trim(), true)
                if (contact2Name.isNotBlank() && contact2Phone.isNotBlank()) {
                    onSaveEmergencyContact(contact2Name.trim(), contact2Relation, contact2Phone.trim(), false)
                }

                successMessage = "Student ${fullName.trim()} registered successfully! Emergency shield activated with GPS live tracking."
                showSuccessDialog = true
            }

            UserRole.PARENT -> {
                if (parentName.isBlank() || parentPhone.isBlank()) {
                    errorMessage = "Please enter Parent / Guardian Name and Phone."
                    showErrorDialog = true
                    return
                }
                onRoleRegistered(UserRole.PARENT, parentPhone.trim(), parentName.trim())
                onSaveEmergencyContact(parentName.trim(), parentRelation, parentPhone.trim(), true)
                successMessage = "Parent $parentName registered and linked to ward monitoring dashboard!"
                showSuccessDialog = true
            }

            UserRole.TEACHER, UserRole.ADMIN -> {
                if (facultyName.isBlank() || facultyPhone.isBlank()) {
                    errorMessage = "Please enter Faculty / Officer Name and Official Contact."
                    showErrorDialog = true
                    return
                }
                onRoleRegistered(selectedRegistrationRole, facultyPhone.trim(), facultyName.trim())
                successMessage = "${selectedRegistrationRole.displayName} profile ($facultyName) registered successfully with dispatch privileges!"
                showSuccessDialog = true
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SelrNavyBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        SelrEmblemBadge(size = 54.dp, showBannerText = false)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SELR ENROLLMENT REGISTRY",
                                fontFamily = AntonFontFamily,
                                fontSize = 20.sp,
                                color = SelrRed,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Emergency Student Rescue & Contact Registration",
                                fontFamily = PoppinsFontFamily,
                                fontSize = 11.sp,
                                color = SelrTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Role Selection Segmented Buttons
                    Text(
                        text = "SELECT REGISTRATION CATEGORY",
                        fontFamily = AntonFontFamily,
                        fontSize = 12.sp,
                        color = SelrGold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RegistrationRoleChip(
                            label = "STUDENT",
                            icon = Icons.Default.School,
                            isSelected = selectedRegistrationRole == UserRole.STUDENT,
                            onClick = { selectedRegistrationRole = UserRole.STUDENT },
                            modifier = Modifier.weight(1f)
                        )
                        RegistrationRoleChip(
                            label = "PARENT",
                            icon = Icons.Default.FamilyRestroom,
                            isSelected = selectedRegistrationRole == UserRole.PARENT,
                            onClick = { selectedRegistrationRole = UserRole.PARENT },
                            modifier = Modifier.weight(1f)
                        )
                        RegistrationRoleChip(
                            label = "FACULTY",
                            icon = Icons.Default.LocalPolice,
                            isSelected = selectedRegistrationRole == UserRole.TEACHER || selectedRegistrationRole == UserRole.ADMIN,
                            onClick = { selectedRegistrationRole = UserRole.TEACHER },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Action Helper Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { fillSampleData() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SelrGold),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SelrGold.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("fill_sample_data_btn")
                        ) {
                            Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Auto-Fill Demo", fontSize = 11.sp, fontFamily = AntonFontFamily)
                        }

                        TextButton(
                            onClick = { clearForm() },
                            colors = ButtonDefaults.textButtonColors(contentColor = SelrTextSecondary),
                            modifier = Modifier.testTag("clear_form_btn")
                        ) {
                            Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear", fontSize = 11.sp, fontFamily = PoppinsFontFamily)
                        }
                    }
                }
            }
        }

        // STUDENT REGISTRATION FORM
        if (selectedRegistrationRole == UserRole.STUDENT) {
            item {
                SectionHeader(title = "1. STUDENT PERSONAL INFORMATION", icon = Icons.Default.Person)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name *") },
                            placeholder = { Text("e.g. Aarav Sharma") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = SelrRed) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth().testTag("reg_student_name_input"),
                            colors = registrationTextFieldColors()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = rollNo,
                                onValueChange = { rollNo = it },
                                label = { Text("Roll No / ID *") },
                                placeholder = { Text("CS-3A-42") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier.weight(1f).testTag("reg_student_roll_input"),
                                colors = registrationTextFieldColors()
                            )

                            OutlinedTextField(
                                value = mobile,
                                onValueChange = { mobile = it },
                                label = { Text("Mobile *") },
                                placeholder = { Text("+91 98765...") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                modifier = Modifier.weight(1.2f).testTag("reg_student_phone_input"),
                                colors = registrationTextFieldColors()
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Student Email (Optional)") },
                            placeholder = { Text("aarav.sharma@campus.edu") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = SelrGold) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth().testTag("reg_student_email_input"),
                            colors = registrationTextFieldColors()
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "2. ACADEMIC & CAMPUS DETAILS", icon = Icons.Default.AccountBalance)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = institution,
                            onValueChange = { institution = it },
                            label = { Text("College / University / School Name") },
                            placeholder = { Text("Delhi Institute of Technology & Engineering") },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = SelrGold) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_student_institution_input"),
                            colors = registrationTextFieldColors()
                        )

                        OutlinedTextField(
                            value = classDepartment,
                            onValueChange = { classDepartment = it },
                            label = { Text("Department / Branch & Section") },
                            placeholder = { Text("Computer Science & Engineering - Sem 5") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_student_department_input"),
                            colors = registrationTextFieldColors()
                        )

                        // Hosteller vs Day Scholar Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SelrNavyCard)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isHosteller) "Campus Hosteller" else "Day Scholar Commuter",
                                    fontFamily = AntonFontFamily,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isHosteller) "Resides inside university campus hostel" else "Travels daily via public/private transit",
                                    fontFamily = PoppinsFontFamily,
                                    fontSize = 10.sp,
                                    color = SelrTextSecondary
                                )
                            }
                            Switch(
                                checked = isHosteller,
                                onCheckedChange = { isHosteller = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = SelrGold,
                                    checkedTrackColor = SelrRed
                                ),
                                modifier = Modifier.testTag("reg_hosteller_switch")
                            )
                        }

                        OutlinedTextField(
                            value = hostelWingOrBusRoute,
                            onValueChange = { hostelWingOrBusRoute = it },
                            label = { Text(if (isHosteller) "Hostel Wing & Room No" else "Transit Bus Route / Metro Station") },
                            placeholder = { Text(if (isHosteller) "Tagore Hall - Room 204" else "Route 14 (Hauz Khas - Rohini)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = registrationTextFieldColors()
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "3. MEDICAL & BLOOD GROUP", icon = Icons.Default.MedicalServices)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Blood group dropdown selector
                        Box {
                            OutlinedTextField(
                                value = bloodGroup,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Blood Group *") },
                                trailingIcon = {
                                    IconButton(onClick = { bloodGroupExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = SelrGold)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { bloodGroupExpanded = true }
                                    .testTag("reg_blood_group_dropdown"),
                                colors = registrationTextFieldColors()
                            )

                            DropdownMenu(
                                expanded = bloodGroupExpanded,
                                onDismissRequest = { bloodGroupExpanded = false },
                                modifier = Modifier.background(SelrNavyCard)
                            ) {
                                bloodGroups.forEach { bg ->
                                    DropdownMenuItem(
                                        text = { Text(bg, color = Color.White, fontFamily = AntonFontFamily) },
                                        onClick = {
                                            bloodGroup = bg
                                            bloodGroupExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = medicalNotes,
                            onValueChange = { medicalNotes = it },
                            label = { Text("Allergies, Medical Conditions or Inhaler Info") },
                            placeholder = { Text("e.g. Mild Asthma, carries blue inhaler. Allergic to peanuts.") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("reg_medical_notes_input"),
                            colors = registrationTextFieldColors()
                        )
                    }
                }
            }

            item {
                SectionHeader(title = "4. EMERGENCY RESCUE CONTACTS (SOS RECIPIENTS)", icon = Icons.Default.ContactPhone)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SelrRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "PRIMARY GUARDIAN (AUTO-SMS & CALL)",
                                fontFamily = AntonFontFamily,
                                fontSize = 13.sp,
                                color = SelrGold
                            )
                        }

                        OutlinedTextField(
                            value = contact1Name,
                            onValueChange = { contact1Name = it },
                            label = { Text("Guardian Name *") },
                            placeholder = { Text("e.g. Ramesh Sharma") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_contact1_name_input"),
                            colors = registrationTextFieldColors()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = contact1Relation,
                                onValueChange = { contact1Relation = it },
                                label = { Text("Relation *") },
                                placeholder = { Text("Father") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("reg_contact1_relation_input"),
                                colors = registrationTextFieldColors()
                            )

                            OutlinedTextField(
                                value = contact1Phone,
                                onValueChange = { contact1Phone = it },
                                label = { Text("Phone Number *") },
                                placeholder = { Text("+91 98111 22334") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1.3f).testTag("reg_contact1_phone_input"),
                                colors = registrationTextFieldColors()
                            )
                        }

                        Divider(color = SelrNavyBorder, modifier = Modifier.padding(vertical = 4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(SelrNavyCard),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", color = SelrTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "SECONDARY CONTACT (OPTIONAL)",
                                fontFamily = AntonFontFamily,
                                fontSize = 13.sp,
                                color = SelrTextSecondary
                            )
                        }

                        OutlinedTextField(
                            value = contact2Name,
                            onValueChange = { contact2Name = it },
                            label = { Text("Contact Name") },
                            placeholder = { Text("Sunita Sharma") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_contact2_name_input"),
                            colors = registrationTextFieldColors()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = contact2Relation,
                                onValueChange = { contact2Relation = it },
                                label = { Text("Relation") },
                                placeholder = { Text("Mother") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("reg_contact2_relation_input"),
                                colors = registrationTextFieldColors()
                            )

                            OutlinedTextField(
                                value = contact2Phone,
                                onValueChange = { contact2Phone = it },
                                label = { Text("Phone Number") },
                                placeholder = { Text("+91 98222 33445") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1.3f).testTag("reg_contact2_phone_input"),
                                colors = registrationTextFieldColors()
                            )
                        }
                    }
                }
            }
        } else if (selectedRegistrationRole == UserRole.PARENT) {
            // PARENT / GUARDIAN FORM
            item {
                SectionHeader(title = "PARENT / GUARDIAN DETAILS", icon = Icons.Default.FamilyRestroom)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = parentName,
                            onValueChange = { parentName = it },
                            label = { Text("Parent / Guardian Name *") },
                            placeholder = { Text("e.g. Ramesh Sharma") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_parent_name_input"),
                            colors = registrationTextFieldColors()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = parentRelation,
                                onValueChange = { parentRelation = it },
                                label = { Text("Relationship *") },
                                placeholder = { Text("Father") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = registrationTextFieldColors()
                            )

                            OutlinedTextField(
                                value = parentPhone,
                                onValueChange = { parentPhone = it },
                                label = { Text("Mobile Number *") },
                                placeholder = { Text("+91 98111...") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1.3f).testTag("reg_parent_phone_input"),
                                colors = registrationTextFieldColors()
                            )
                        }

                        OutlinedTextField(
                            value = studentWardRollNo,
                            onValueChange = { studentWardRollNo = it },
                            label = { Text("Student Ward Roll No / Name *") },
                            placeholder = { Text("e.g. CS-3A-42 (Aarav Sharma)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_parent_ward_input"),
                            colors = registrationTextFieldColors()
                        )

                        OutlinedTextField(
                            value = parentEmail,
                            onValueChange = { parentEmail = it },
                            label = { Text("Email for Safety Reports") },
                            placeholder = { Text("parent@gmail.com") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            colors = registrationTextFieldColors()
                        )
                    }
                }
            }
        } else {
            // FACULTY / DISPATCHER FORM
            item {
                SectionHeader(title = "FACULTY / DISPATCHER CREDENTIALS", icon = Icons.Default.Security)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SelrNavyBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = facultyName,
                            onValueChange = { facultyName = it },
                            label = { Text("Officer / Teacher Name *") },
                            placeholder = { Text("Prof. Rajesh Kumar") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("reg_faculty_name_input"),
                            colors = registrationTextFieldColors()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = facultyEmployeeId,
                                onValueChange = { facultyEmployeeId = it },
                                label = { Text("Employee / Badge ID *") },
                                placeholder = { Text("FAC-901") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = registrationTextFieldColors()
                            )

                            OutlinedTextField(
                                value = facultyPhone,
                                onValueChange = { facultyPhone = it },
                                label = { Text("Campus Hotline / Phone *") },
                                placeholder = { Text("+91 11 2659...") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.weight(1.3f).testTag("reg_faculty_phone_input"),
                                colors = registrationTextFieldColors()
                            )
                        }

                        OutlinedTextField(
                            value = facultyDepartment,
                            onValueChange = { facultyDepartment = it },
                            label = { Text("Department / Assigned Security Unit") },
                            placeholder = { Text("Campus Quick Reaction Force") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = registrationTextFieldColors()
                        )

                        OutlinedTextField(
                            value = facultyCampusPost,
                            onValueChange = { facultyCampusPost = it },
                            label = { Text("Campus Post / Station Location") },
                            placeholder = { Text("Gate 3 Security Post / Academic Block") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = registrationTextFieldColors()
                        )
                    }
                }
            }
        }

        // SUBMIT REGISTRATION BUTTON
        item {
            Button(
                onClick = { validateAndSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_registration_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = when (selectedRegistrationRole) {
                        UserRole.STUDENT -> "ENROLL STUDENT & ACTIVATE SHIELD"
                        UserRole.PARENT -> "REGISTER GUARDIAN PROFILE"
                        UserRole.TEACHER, UserRole.ADMIN -> "ACTIVATE DISPATCH PRIVILEGES"
                    },
                    fontFamily = AntonFontFamily,
                    fontSize = 15.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Validation Error Dialog
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SelrRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Incomplete Form", fontFamily = AntonFontFamily, color = Color.White)
                }
            },
            text = {
                Text(errorMessage, fontFamily = PoppinsFontFamily, color = SelrTextSecondary)
            },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SelrRed)
                ) {
                    Text("OK", fontFamily = AntonFontFamily)
                }
            },
            containerColor = SelrNavySurface
        )
    }

    // Registration Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onNavigateToHome()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = SelrGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("REGISTRATION ACTIVATED", fontFamily = AntonFontFamily, color = SelrGold)
                }
            },
            text = {
                Column {
                    Text(
                        text = successMessage,
                        fontFamily = PoppinsFontFamily,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Live GPS tracking enabled (±3.8m)\n• 30-min High-Risk auto Safe updates active\n• Emergency contact registry synchronized",
                        fontFamily = PoppinsFontFamily,
                        color = SelrOliveAccent,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateToHome()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelrRed)
                ) {
                    Text("GO TO DEFENSE DASHBOARD", fontFamily = AntonFontFamily)
                }
            },
            containerColor = SelrNavySurface
        )
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = SelrGold, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontFamily = AntonFontFamily,
            fontSize = 12.sp,
            color = SelrGold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun RegistrationRoleChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) SelrRed else SelrNavyCard)
            .border(
                1.dp,
                if (isSelected) SelrRedBright else SelrNavyBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else SelrTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontFamily = AntonFontFamily,
                fontSize = 11.sp,
                color = if (isSelected) Color.White else SelrTextMuted,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun registrationTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = SelrRed,
    unfocusedBorderColor = SelrNavyBorder,
    focusedLabelColor = SelrGold,
    unfocusedLabelColor = SelrTextSecondary,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = SelrRed,
    focusedPlaceholderColor = SelrTextMuted,
    unfocusedPlaceholderColor = SelrTextMuted
)
