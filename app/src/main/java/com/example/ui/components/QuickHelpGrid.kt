package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.ShareLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun QuickHelpGrid(
    onShareLocation: () -> Unit,
    onMedicalEmergency: () -> Unit,
    onNeedHelp: () -> Unit,
    onFakeSafeCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "TACTICAL QUICK ACTIONS",
            fontFamily = AntonFontFamily,
            fontSize = 14.sp,
            color = SelrTextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionCard(
                title = "Share Location",
                subtitle = "SMS & WhatsApp",
                icon = Icons.Default.ShareLocation,
                accentColor = SelrOliveAccent,
                onClick = onShareLocation,
                testTag = "quick_share_location_btn",
                modifier = Modifier.weight(1f)
            )

            QuickActionCard(
                title = "Medical SOS",
                subtitle = "Blood & Health Alert",
                icon = Icons.Default.LocalHospital,
                accentColor = Color(0xFFE53935),
                onClick = onMedicalEmergency,
                testTag = "quick_medical_emergency_btn",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionCard(
                title = "Need Help",
                subtitle = "Discreet / Silent Alert",
                icon = Icons.Default.PanTool,
                accentColor = SelrGold,
                onClick = onNeedHelp,
                testTag = "quick_need_help_btn",
                modifier = Modifier.weight(1f)
            )

            QuickActionCard(
                title = "Fake Safe Call",
                subtitle = "Girls Safety Exit",
                icon = Icons.Default.Call,
                accentColor = Color(0xFF26A69A),
                onClick = onFakeSafeCall,
                testTag = "quick_fake_call_btn",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SelrNavyBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = SelrNavySurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = title,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontFamily = PoppinsFontFamily,
                    fontSize = 10.sp,
                    color = SelrTextSecondary
                )
            }
        }
    }
}
