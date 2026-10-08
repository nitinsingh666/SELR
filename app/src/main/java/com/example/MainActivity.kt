package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SelrMainApp
import com.example.ui.theme.*
import com.example.viewmodel.SelrViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

class MainActivity : ComponentActivity() {

    private val viewModel: SelrViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            val isDynamicColor by viewModel.isDynamicColor.collectAsStateWithLifecycle()

            SELRTheme(
                darkTheme = isDarkTheme,
                dynamicColor = isDynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LocationPermissionHandler(
                        viewModel = viewModel,
                        content = {
                            SelrMainApp(viewModel = viewModel)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    viewModel: SelrViewModel,
    content: @Composable () -> Unit
) {
    val locationPermissions = buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissionsState: MultiplePermissionsState = rememberMultiplePermissionsState(
        permissions = locationPermissions
    )

    val isLocationGranted = permissionsState.permissions.any {
        (it.permission == Manifest.permission.ACCESS_FINE_LOCATION ||
                it.permission == Manifest.permission.ACCESS_COARSE_LOCATION) &&
                it.status is PermissionStatus.Granted
    }

    LaunchedEffect(isLocationGranted) {
        if (isLocationGranted) {
            viewModel.refreshLocation()
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        content()

        // Tactical Non-blocking Rationale / Permission Request Banner if location was denied
        if (!isLocationGranted && permissionsState.shouldShowRationale) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF221115))
                    .border(1.5.dp, SelrRed, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .testTag("location_permission_rationale")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = SelrRedBright,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "GPS PERMISSION REQUIRED FOR SOS",
                            fontFamily = AntonFontFamily,
                            fontSize = 14.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "SELR uses High Accuracy GPS to transmit your live coordinate beacon to Parents and Faculty during an emergency.",
                        fontFamily = PoppinsFontFamily,
                        fontSize = 12.sp,
                        color = SelrGold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { permissionsState.launchMultiplePermissionRequest() },
                        colors = ButtonDefaults.buttonColors(containerColor = SelrRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grant_location_permission_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ENABLE RESCUE GPS ACCESS",
                            fontFamily = AntonFontFamily,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
