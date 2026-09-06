package com.wifite.security.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifite.security.logic.WordlistManager

@Composable
fun WordlistScreen(
    wordlistManager: WordlistManager,
    onWordlistSelected: (List<String>) -> Unit,
    onBack: () -> Unit
) {
    var selectedWordlist by remember { mutableStateOf<List<String>?>(null) }
    var wordlistInfo by remember { mutableStateOf<String>("")  }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isLoading = true
            // Wordlist loading would happen here in real implementation
            isLoading = false
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
                    "Select Wordlist",
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF2a2a2a)
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2a2a2a)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "📋 Please select a wordlist",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF00),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "The wordlist will be used to crack WiFi passwords via dictionary attack.",
                        fontSize = 12.sp,
                        color = Color(0xFFCCCCCC)
                    )
                }
            }

            // Upload Custom Wordlist Button
            Button(
                onClick = { filePickerLauncher.launch("text/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FF00)
                )
            ) {
                Text(
                    "📁 Upload Wordlist File",
                    color = Color(0xFF000000),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Divider
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    color = Color(0xFF444444),
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                )
                Text(
                    "OR",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Divider(
                    color = Color(0xFF444444),
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                )
            }

            // Built-in Wordlists
            Text(
                "Built-in Wordlists",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF00),
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 12.dp)
            )

            BuiltinWordlistOption(
                title = "Common Passwords (4,800)",
                description = "Top common WiFi passwords",
                selected = selectedWordlist?.size == 4800,
                onSelect = {
                    selectedWordlist = wordlistManager.getBuiltInWordlist()
                    wordlistInfo = "4,800 passwords loaded"
                }
            )

            BuiltinWordlistOption(
                title = "Turkish Passwords (3,500)",
                description = "Türkçe yaygın şifreler - Türk kullanıcılarına özgü",
                selected = selectedWordlist?.size == 3500,
                onSelect = {
                    selectedWordlist = wordlistManager.getTurkishWordlist()
                    wordlistInfo = "3,500 Türkçe şifre yüklendi"
                }
            )

            BuiltinWordlistOption(
                title = "Advanced Brute Force",
                description = "Karakterler + sayılar - Daha kapsamlı test",
                selected = selectedWordlist?.size == 5000,
                onSelect = {
                    selectedWordlist = wordlistManager.getBruteForceWordlist()
                    wordlistInfo = "5,000+ kombinasyon yüklendi"
                }
            )

            // Error Message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4a1a1a)
                    )
                ) {
                    Text(
                        errorMessage!!,
                        fontSize = 12.sp,
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Wordlist Info
            if (wordlistInfo.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1a4a1a)
                    ),
                    border = androidx.compose.foundation.border(
                        1.dp,
                        Color(0xFF00FF00)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            wordlistInfo,
                            fontSize = 13.sp,
                            color = Color(0xFF00FF00),
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = Color(0xFF00FF00)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Start Button
            if (selectedWordlist != null) {
                Button(
                    onClick = { onWordlistSelected(selectedWordlist!!) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FF00)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "PROCEED TO CRACKING",
                            color = Color(0xFF000000),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuiltinWordlistOption(
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(enabled) { onSelect() }
            .then(
                if (selected) {
                    Modifier.background(Color(0xFF00FF00), alpha = 0.1f)
                } else {
                    Modifier.background(Color.Transparent)
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a2a2a)
        ),
        border = androidx.compose.foundation.border(
            1.dp,
            if (selected) Color(0xFF00FF00) else Color(0xFF444444)
        ),
        enabled = enabled
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color(0xFF00FF00) else Color(0xFF666666)
                )
                Text(
                    description,
                    fontSize = 11.sp,
                    color = if (enabled) Color(0xFFCCCCCC) else Color(0xFF666666)
                )
            }

            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFF00FF00),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
