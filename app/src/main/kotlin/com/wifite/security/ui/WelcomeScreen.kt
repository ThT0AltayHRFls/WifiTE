package com.wifite.security.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(Color(0xFF1a1a1a)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // WiFi + Key Icon (simplified emoji for now)
            Text(
                text = "📡🔑",
                fontSize = 80.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = "Wifite Security",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF00),
                textAlign = TextAlign.Center
            )

            Text(
                text = "WiFi Penetration Testing Suite",
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Disclaimer Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(vertical = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2a2a2a)
            ),
            border = androidx.compose.foundation.border(
                1.dp,
                Color(0xFF00FF00)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "⚠️ LEGAL DISCLAIMER",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = """
This is a complete port of the official Linux wifite2 tool to Android.

Original Tool: Wifite2
Original Developer: derv82 / kimocoder
License: GPL-3.0
Repository: https://github.com/derv82/wifite2

⚠️ IMPORTANT:
• Use this tool ONLY on networks you own or have explicit permission to test
• Unauthorized access to computer networks is ILLEGAL in most jurisdictions
• The author is NOT responsible for misuse or damage
• You accept full legal responsibility for your actions
• This tool is for authorized security testing and educational purposes only

By proceeding, you acknowledge:
✓ You understand the legal implications
✓ You have authorization to test target networks
✓ You accept full responsibility for your actions
                    """.trimIndent(),
                    fontSize = 12.sp,
                    color = Color(0xFFCCCCCC),
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Left
                )
            }
        }

        // Features Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2a2a2a)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Supported Attack Methods:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF00),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FeatureItem("🔓 WPA/WPA2/WPA3 Dictionary Attack")
                FeatureItem("⚡ PMKID Hash Extraction & Cracking")
                FeatureItem("🎯 WPS Brute-Force (when available)")
                FeatureItem("📊 Real-time Network Scanning")
                FeatureItem("💾 Wordlist Support (custom uploads)")
                FeatureItem("🔍 Hidden Network Detection")
            }
        }

        // Start Button
        Button(
            onClick = onStartClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00FF00)
            )
        ) {
            Text(
                text = "START SECURITY AUDIT",
                color = Color(0xFF000000),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Developer Credit
        Text(
            text = "Developer: AltayHR | Version 2.0",
            fontSize = 10.sp,
            color = Color(0xFF666666),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .padding(horizontal = 24.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "•",
            fontSize = 16.sp,
            color = Color(0xFF00FF00),
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFFCCCCCC)
        )
    }
}
