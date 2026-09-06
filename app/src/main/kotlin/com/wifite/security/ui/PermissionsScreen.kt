package com.wifite.security.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class PermissionItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isGranted: Boolean = false,
    val isRequired: Boolean = true
)

@Composable
fun PermissionsScreen(onGrantPermissions: () -> Unit) {
    val scrollState = rememberScrollState()
    var permissions by remember {
        mutableStateOf(
            listOf(
                PermissionItem(
                    id = "wifi",
                    title = "Wi-Fi State Access",
                    description = "Access and monitor Wi-Fi networks for scanning",
                    icon = Icons.Filled.Lock,
                    isGranted = false,
                    isRequired = true
                ),
                PermissionItem(
                    id = "location",
                    title = "Location Permission",
                    description = "Required by Android for Wi-Fi scanning functionality",
                    icon = Icons.Filled.LocationOn,
                    isGranted = false,
                    isRequired = true
                ),
                PermissionItem(
                    id = "storage",
                    title = "File Storage Access",
                    description = "Read wordlists and save attack results",
                    icon = Icons.Filled.Lock,
                    isGranted = false,
                    isRequired = false
                )
            )
        )
    }

    val allRequired = permissions.filter { it.isRequired }.all { it.isGranted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .verticalScroll(scrollState)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2a2a2a))
                .padding(vertical = 24.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = "Permissions",
                tint = Color(0xFF00FF00),
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Required Permissions",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF00),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Wifite Security needs the following permissions to function properly",
                fontSize = 13.sp,
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1a4a1a)
            ),
            border = androidx.compose.foundation.border(
                1.dp,
                Color(0xFF00FF00),
                RoundedCornerShape(8.dp)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Why These Permissions?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF00),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Android requires specific permissions for Wi-Fi network access, scanning, and location services. These are necessary for proper attack execution and network monitoring.",
                    fontSize = 12.sp,
                    color = Color(0xFFCCCCCC),
                    lineHeight = 18.sp
                )
            }
        }

        // Permissions List
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            permissions.forEach { permission ->
                PermissionCard(
                    permission = permission,
                    onToggle = { granted ->
                        permissions = permissions.map {
                            if (it.id == permission.id) {
                                it.copy(isGranted = granted)
                            } else {
                                it
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Status Message
        val statusMessage = when {
            permissions.filter { it.isRequired }.all { it.isGranted } ->
                "✓ All required permissions granted"
            else ->
                "⚠️ Some required permissions are missing"
        }

        val statusColor = if (allRequired) Color(0xFF00FF00) else Color(0xFFFF6B6B)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (allRequired) Color(0xFF1a4a1a) else Color(0xFF4a1a1a)
            ),
            border = androidx.compose.foundation.border(
                1.dp,
                statusColor,
                RoundedCornerShape(8.dp)
            )
        ) {
            Text(
                text = statusMessage,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // Continue Button
        Button(
            onClick = onGrantPermissions,
            enabled = allRequired,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00FF00),
                disabledContainerColor = Color(0xFF444444)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "CONTINUE",
                color = if (allRequired) Color(0xFF000000) else Color(0xFF666666),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Disclaimer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2a2a2a)
            ),
            border = androidx.compose.foundation.border(
                1.dp,
                Color(0xFF444444),
                RoundedCornerShape(8.dp)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Privacy Notice",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF00),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Wifite Security does not collect, transmit, or store any personal data. All operations are performed locally on your device. This application is for authorized security testing only.",
                    fontSize = 11.sp,
                    color = Color(0xFFAAAAAA),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    permission: PermissionItem,
    onToggle: (Boolean) -> Unit
) {
    val backgroundColor = if (permission.isGranted) {
        Color(0xFF1a4a1a)
    } else {
        Color(0xFF2a2a2a)
    }

    val borderColor = if (permission.isGranted) {
        Color(0xFF00FF00)
    } else {
        Color(0xFF444444)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.border(
            1.dp,
            borderColor,
            RoundedCornerShape(8.dp)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left content
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = permission.icon,
                    contentDescription = permission.title,
                    tint = if (permission.isGranted) Color(0xFF00FF00) else Color(0xFF888888),
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 12.dp)
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = permission.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (permission.isGranted) Color(0xFF00FF00) else Color(0xFFCCCCCC)
                    )

                    Text(
                        text = permission.description,
                        fontSize = 11.sp,
                        color = Color(0xFF888888),
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    if (!permission.isRequired) {
                        Text(
                            text = "(Optional)",
                            fontSize = 10.sp,
                            color = Color(0xFF666666),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Toggle Button
            Spacer(modifier = Modifier.width(8.dp))

            PermissionToggleButton(
                isGranted = permission.isGranted,
                onToggle = onToggle
            )
        }
    }
}

@Composable
fun PermissionToggleButton(
    isGranted: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Button(
        onClick = { onToggle(!isGranted) },
        modifier = Modifier
            .height(48.dp)
            .width(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isGranted) Color(0xFF00FF00) else Color(0xFF444444)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        if (isGranted) {
            Icon(
                Icons.Filled.Check,
                contentDescription = "Granted",
                tint = Color(0xFF000000),
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(
                text = "?",
                fontSize = 20.sp,
                color = Color(0xFF888888),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
