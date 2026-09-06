package com.wifite.security

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.wifite.security.logic.WifiNetwork
import com.wifite.security.logic.WifiScanner
import com.wifite.security.logic.WordlistManager
import com.wifite.security.ui.*
import com.wifite.security.ui.PermissionsScreen
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Wifite Android Security Tool
 * WiFi Penetration Testing Suite
 * 
 * Developer: AltayHR
 * License: GPL-3.0
 * Version: 2.0 (Production Ready)
 */
class MainActivity : ComponentActivity() {

    private lateinit var wifiScanner: WifiScanner
    private lateinit var wordlistManager: WordlistManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            Timber.e("WiFi permissions not granted")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize managers
        wifiScanner = WifiScanner(this)
        wordlistManager = WordlistManager(this)

        // Request permissions
        requestPermissions()

        setContent {
            WifiteSecurityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1a1a1a)
                ) {
                    MainScreen(
                        wifiScanner = wifiScanner,
                        wordlistManager = wordlistManager
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest)
        }
    }
}

@Composable
fun MainScreen(
    wifiScanner: WifiScanner,
    wordlistManager: WordlistManager
) {
    var currentScreen by remember { mutableStateOf(Screen.PERMISSIONS) }
    var wifiNetworks by remember { mutableStateOf(listOf<WifiNetwork>()) }
    var isScanning by remember { mutableStateOf(false) }
    var selectedNetwork by remember { mutableStateOf<WifiNetwork?>(null) }
    var selectedWordlist by remember { mutableStateOf<List<String>?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            Screen.SCANNING -> {
                wifiScanner.enableWifi()
                isScanning = true
                scope.launch {
                    wifiScanner.startScan()
                    val results = wifiScanner.scanNetworks()
                    wifiNetworks = results.distinctBy { it.bssid }
                    isScanning = false
                }
            }
            else -> {}
        }
    }

    when (currentScreen) {
        Screen.PERMISSIONS -> {
            PermissionsScreen(
                onGrantPermissions = {
                    currentScreen = Screen.WELCOME
                }
            )
        }

        Screen.WELCOME -> {
            WelcomeScreen(
                onStartClick = {
                    currentScreen = Screen.SCANNING
                }
            )
        }

        Screen.SCANNING -> {
            NetworkScanScreen(
                networks = wifiNetworks,
                isScanning = isScanning,
                onNetworkSelected = { network ->
                    selectedNetwork = network
                    currentScreen = Screen.WORDLIST
                },
                onRefresh = {
                    scope.launch {
                        isScanning = true
                        wifiScanner.startScan()
                        val results = wifiScanner.scanNetworks()
                        wifiNetworks = results.distinctBy { it.bssid }
                        isScanning = false
                    }
                },
                onBack = {
                    currentScreen = Screen.WELCOME
                }
            )
        }

        Screen.WORDLIST -> {
            WordlistScreen(
                wordlistManager = wordlistManager,
                onWordlistSelected = { wordlist ->
                    selectedWordlist = wordlist
                    currentScreen = Screen.CRACKING
                },
                onBack = {
                    currentScreen = Screen.SCANNING
                }
            )
        }

        Screen.CRACKING -> {
            if (selectedNetwork != null && selectedWordlist != null) {
                CrackingScreen(
                    network = selectedNetwork!!,
                    wordlistSize = selectedWordlist!!.size,
                    onCancel = {
                        currentScreen = Screen.SCANNING
                    },
                    onComplete = { password ->
                        // Could save result or show summary
                        currentScreen = Screen.WELCOME
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1a1a1a)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Error: Missing data", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}

enum class Screen {
    PERMISSIONS, WELCOME, SCANNING, WORDLIST, CRACKING
}

@Composable
fun WifiteSecurityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = androidx.compose.material3.darkColorScheme(
            primary = Color(0xFF00FF00),
            secondary = Color(0xFFFFDD00),
            background = Color(0xFF1a1a1a),
            surface = Color(0xFF2a2a2a)
        )
    ) {
        content()
    }
}
