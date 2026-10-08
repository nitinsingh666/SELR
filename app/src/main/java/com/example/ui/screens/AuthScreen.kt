package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun AuthScreen(
    onRoleAuthenticated: (UserRole, String, String?) -> Unit,
    onStudentRegistered: (StudentProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var isRegisterMode by remember { mutableStateOf(false) }

    // Student Registration Form State
    var studentName by remember { mutableStateOf("Aarav Sharma") }
    var classRollNo by remember { mutableStateOf("CS-3A / Roll 42") }
    var schoolCollegeName by remember { mutableStateOf("Delhi Institute of Engineering & Technology") }
    var bloodGroup by remember { mutableStateOf("O+") }
    var isHosteller by remember { mutableStateOf(false) }
    var studentMobile by remember { mutableStateOf("+91 98765 43210") }

    var contact1Name by remember { mutableStateOf("Ramesh Sharma") }
    var contact1Relation by remember { mutableStateOf("Father") }
    var contact1Phone by remember { mutableStateOf("+91 98111 22334") }

    var contact2Name by remember { mutableStateOf("Sunita Sharma") }
    var contact2Relation by remember { mutableStateOf("Mother") }
    var contact2Phone by remember { mutableStateOf("+91 98222 33445") }

    var medicalNotes by remember { mutableStateOf("Mild Asthma. Inhaler in side pocket.") }

    // Credentials for Parent/Teacher/Admin
    var emailOrPhone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Blood group dropdown expanded
    var bloodGroupDropdownExpanded by remember { mutableStateOf(false) }
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SelrNavyBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Crest
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SelrEmblemBadge(size = 64.dp, showBannerText = false)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "SELR AUTHENTICATION",
                    fontFamily = AntonFontFamily,
                    fontSize = 22.sp,
                    color = SelrRed,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Secure Military-Grade Rescue Access",
                    fontFamily = PoppinsFontFamily,
                    fontSize = 12.sp,
                    color = SelrTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Role Selection Segmented Grid
        Text(
            text = "SELECT YOUR SYSTEM ROLE",
            fontFamily = AntonFontFamily,
            fontSize = 13.sp,
            color = SelrTextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RoleCard(
                role = UserRole.STUDENT,
                isSelected = selectedRole == UserRole.STUDENT,
                onClick = { selectedRole = UserRole.STUDENT },
                modifier = Modifier.weight(1f)
            )
            RoleCard(
                role = UserRole.PARENT,
                isSelected = selectedRole == UserRole.PARENT,
                onClick = { selectedRole = UserRole.PARENT },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RoleCard(
                role = UserRole.TEACHER,
                isSelected = selectedRole == UserRole.TEACHER,
                onClick = { selectedRole = UserRole.TEACHER },
                modifier = Modifier.weight(1f)
            )
            RoleCard(
                role = UserRole.ADMIN,
                isSelected = selectedRole == UserRole.ADMIN,
                onClick = { selectedRole = UserRole.ADMIN },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Mode Switch (Login vs Sign-up)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SelrNavySurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (!isRegisterMode) SelrRed else Color.Transparent)
                    .clickable { isRegisterMode = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SIGN IN",
                    fontFamily = AntonFontFamily,
                    fontSize = 13.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isRegisterMode) SelrRed else Color.Transparent)
                    .clickable { isRegisterMode = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedRole == UserRole.STUDENT) "REGISTER STUDENT" else "CREATE ACCOUNT",
                    fontFamily = AntonFontFamily,
                    fontSize = 13.sp,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ROLE FORM CONTENT
        if (selectedRole == UserRole.STUDENT && isRegisterMode) {
            // FULL STUDENT REGISTRATION FORM
            Text(
                text = "STUDENT ENROLLMENT FORM",
                fontFamily = AntonFontFamily,
                fontSize = 16.sp,
                color = SelrGold,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = studentName,
                onValueChange = { studentName = it },
                label = { Text("Full Name *") },
                modifier = Modifier.fillMaxWidth().testTag("student_name_input"),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = classRollNo,
                onValueChange = { classRollNo = it },
                label = { Text("Class / Roll No *") },
                modifier = Modifier.fillMaxWidth().testTag("student_class_input"),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = schoolCollegeName,
                onValueChange = { schoolCollegeName = it },
                label = { Text("School / College Name *") },
                modifier = Modifier.fillMaxWidth().testTag("student_school_input"),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Blood Group Dropdown
            ExposedDropdownMenuBox(
                expanded = bloodGroupDropdownExpanded,
                onExpandedChange = { bloodGroupDropdownExpanded = !bloodGroupDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "Blood Group: $bloodGroup",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodGroupDropdownExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    colors = textFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = bloodGroupDropdownExpanded,
                    onDismissRequest = { bloodGroupDropdownExpanded = false },
                    modifier = Modifier.background(SelrNavyCard)
                ) {
                    bloodGroups.forEach { bg ->
                        DropdownMenuItem(
                            text = { Text(bg, color = Color.White) },
                            onClick = {
                                bloodGroup = bg
                                bloodGroupDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day Scholar vs Hosteller Toggle
            Text(
                text = "RESIDENCE STATUS",
                fontFamily = PoppinsFontFamily,
                fontSize = 11.sp,
                color = SelrTextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SelrNavySurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = !isHosteller,
                    onClick = { isHosteller = false },
                    label = { Text("Day-Scholar", fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SelrOlive,
                        selectedLabelColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = isHosteller,
                    onClick = { isHosteller = true },
                    label = { Text("Hosteller (Campus Resident)", fontWeight = FontWeight.SemiBold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SelrOlive,
                        selectedLabelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = studentMobile,
                onValueChange = { studentMobile = it },
                label = { Text("Student Mobile *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth().testTag("student_mobile_input"),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency Contacts
            Text(
                text = "EMERGENCY CONTACTS (PARENTS / GUARDIANS)",
                fontFamily = AntonFontFamily,
                fontSize = 13.sp,
                color = SelrTextSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Primary Contact 1 (Auto-SMS on SOS)",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = SelrGold,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = contact1Name,
                        onValueChange = { contact1Name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = contact1Relation,
                            onValueChange = { contact1Relation = it },
                            label = { Text("Relation") },
                            modifier = Modifier.weight(0.45f),
                            colors = textFieldColors()
                        )
                        OutlinedTextField(
                            value = contact1Phone,
                            onValueChange = { contact1Phone = it },
                            label = { Text("Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(0.55f),
                            colors = textFieldColors()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Secondary Contact 2",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = SelrGold,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = contact2Name,
                        onValueChange = { contact2Name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = contact2Relation,
                            onValueChange = { contact2Relation = it },
                            label = { Text("Relation") },
                            modifier = Modifier.weight(0.45f),
                            colors = textFieldColors()
                        )
                        OutlinedTextField(
                            value = contact2Phone,
                            onValueChange = { contact2Phone = it },
                            label = { Text("Phone") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.weight(0.55f),
                            colors = textFieldColors()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = medicalNotes,
                onValueChange = { medicalNotes = it },
                label = { Text("Medical Notes / Allergies (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val newProfile = StudentProfile(
                        id = "stu_${System.currentTimeMillis()}",
                        fullName = studentName.ifBlank { "Student" },
                        classRollNo = classRollNo.ifBlank { "Class CS-1" },
                        schoolCollegeName = schoolCollegeName.ifBlank { "National Institute" },
                        bloodGroup = bloodGroup,
                        isHosteller = isHosteller,
                        mobile = studentMobile.ifBlank { "+91 98765 43210" },
                        emergencyContact1 = EmergencyContact(contact1Name, contact1Relation, contact1Phone),
                        emergencyContact2 = EmergencyContact(contact2Name, contact2Relation, contact2Phone),
                        medicalNotes = medicalNotes
                    )
                    onStudentRegistered(newProfile)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_student_registration_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "COMPLETE ENROLLMENT & ACTIVATE SHIELD",
                    fontFamily = AntonFontFamily,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }
        } else {
            // STANDARD / DEMO LOGIN FORM FOR ROLE
            Text(
                text = "${selectedRole.displayName.uppercase()} LOGIN",
                fontFamily = AntonFontFamily,
                fontSize = 16.sp,
                color = SelrGold,
                letterSpacing = 1.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = emailOrPhone,
                onValueChange = { emailOrPhone = it },
                label = {
                    Text(if (selectedRole == UserRole.STUDENT) "Student Roll No or Mobile" else "Email / Phone Number")
                },
                placeholder = {
                    Text(
                        when (selectedRole) {
                            UserRole.STUDENT -> "aarav.sharma@campus.edu or Roll No"
                            UserRole.PARENT -> "parent.ramesh@gmail.com"
                            UserRole.TEACHER -> "faculty.rajesh@campus.edu"
                            UserRole.ADMIN -> "security.admin@campus.edu"
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().testTag("auth_identifier_input"),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password",
                            tint = SelrTextSecondary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                colors = textFieldColors()
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    val id = emailOrPhone.ifBlank { "${selectedRole.name.lowercase()}@selr.org" }
                    onRoleAuthenticated(selectedRole, id, null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_login_button"),
                colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "ENTER SECURE CONSOLE",
                    fontFamily = AntonFontFamily,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Demo 1-Tap Access for evaluator
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SelrOliveAccent.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QUICK DEMO ACCESS (1-TAP)",
                        fontFamily = AntonFontFamily,
                        fontSize = 12.sp,
                        color = SelrGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val id = "demo_${selectedRole.name.lowercase()}@selr.edu"
                            val name = when (selectedRole) {
                                UserRole.STUDENT -> "Aarav Sharma"
                                UserRole.PARENT -> "Dr. Ramesh Sharma (Parent)"
                                UserRole.TEACHER -> "Prof. Rajesh Kumar (HOD CS)"
                                UserRole.ADMIN -> "Chief Security Officer K. S. Rathore"
                            }
                            onRoleAuthenticated(selectedRole, id, name)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SelrOlive),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("demo_quick_access_btn")
                    ) {
                        Text(
                            text = "LAUNCH AS ${selectedRole.displayName.uppercase()}",
                            fontFamily = AntonFontFamily,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun RoleCard(
    role: UserRole,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.5.dp,
                if (isSelected) SelrRed else SelrNavyBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .testTag("role_select_${role.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SelrNavyCard else SelrNavySurface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = when (role) {
                    UserRole.STUDENT -> Icons.Default.School
                    UserRole.PARENT -> Icons.Default.FamilyRestroom
                    UserRole.TEACHER -> Icons.Default.CastForEducation
                    UserRole.ADMIN -> Icons.Default.Shield
                },
                contentDescription = role.displayName,
                tint = if (isSelected) SelrRedBright else SelrTextSecondary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = role.displayName,
                fontFamily = AntonFontFamily,
                fontSize = 13.sp,
                color = if (isSelected) Color.White else SelrTextSecondary,
                textAlign = TextAlign.Center
            )

            Text(
                text = role.badge,
                fontFamily = PoppinsFontFamily,
                fontSize = 9.sp,
                color = if (isSelected) SelrGold else SelrTextMuted,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = SelrRed,
    unfocusedBorderColor = SelrNavyBorder,
    focusedLabelColor = SelrRed,
    unfocusedLabelColor = SelrTextSecondary,
    focusedContainerColor = SelrNavySurface,
    unfocusedContainerColor = SelrNavySurface
)
