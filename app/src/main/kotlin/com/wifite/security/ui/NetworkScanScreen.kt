package com.wifite.security.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifite.security.logic.WifiNetwork
import kotlinx.coroutines.delay

@Composable
fun NetworkScanScreen(
    networks: List<WifiNetwork>,
    isScanning: Boolean,
    onNetworkSelected: (WifiNetwork) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    var selectedNetwork by remember { mutableStateOf<WifiNetwork?>(null) }
    var refreshCount by remember { mutableStateOf(0) }

    LaunchedEffect(isScanning) {
        if (isScanning) {
            while (isScanning) {
                delay(1000)
                refreshCount++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    "WiFi Networks",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF00FF00)
                    )
                }
            },
            actions = {
                IconButton(onClick = onRefresh, enabled = !isScanning) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = if (isScanning) Color(0xFF666666) else Color(0xFF00FF00)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2a2a2a)
            )
        )

        // Scanning Status
        if (isScanning) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF00FF00)
            )
        }

        // Network List
        if (networks.isEmpty() && !isScanning) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No networks found",
                        fontSize = 16.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Button(
                        onClick = onRefresh,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00FF00)
                        )
                    ) {
                        Text("Scan Again", color = Color.Black)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                items(networks) { network ->
                    NetworkCard(
                        network = network,
                        isSelected = network == selectedNetwork,
                        onSelect = {
                            selectedNetwork = network
                            onNetworkSelected(network)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NetworkCard(
    network: WifiNetwork,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onSelect() }
            .then(
                if (isSelected) {
                    Modifier.background(Color(0xFF00FF00), alpha = 0.1f)
                } else {
                    Modifier.background(Color(0xFF2a2a2a))
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF2a2a2a) else Color(0xFF222222)
        ),
        border = androidx.compose.foundation.border(
            1.dp,
            if (isSelected) Color(0xFF00FF00) else Color(0xFF444444)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (network.isHidden) "[Hidden Network]" else network.ssid,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF00),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = network.bssid,
                        fontSize = 11.sp,
                        color = Color(0xFF888888)
                    )
                }
                SignalStrengthIndicator(network.signalStrength)
            }

            Divider(
                color = Color(0xFF444444),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NetworkInfo(
                    label = "Security",
                    value = network.securityType,
                    color = getSecurityColor(network.securityType)
                )
                NetworkInfo(
                    label = "Frequency",
                    value = if (network.frequency5GHz) "5GHz" else "2.4GHz",
                    color = Color(0xFFCCCCCC)
                )
                NetworkInfo(
                    label = "Signal",
                    value = "${network.level} dBm",
                    color = Color(0xFFCCCCCC)
                )
            }
        }
    }
}

@Composable
fun NetworkInfo(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color(0xFF888888)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun SignalStrengthIndicator(strength: Int) {
    val bars = minOf(strength, 4)
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height((8 + (index + 1) * 4).dp)
                    .background(
                        if (index < bars) Color(0xFF00FF00) else Color(0xFF444444)
                    )
            )
        }
    }
}

fun getSecurityColor(security: String): Color {
    return when (security) {
        "WPA3" -> Color(0xFF00FF00) // Green
        "WPA2" -> Color(0xFFFFDD00) // Yellow
        "WPA" -> Color(0xFFFF9800) // Orange
        "WEP" -> Color(0xFFFF6B6B) // Red
        "OPEN" -> Color(0xFFFF1744) // Dark Red
        else -> Color(0xFFCCCCCC)
    }
}
