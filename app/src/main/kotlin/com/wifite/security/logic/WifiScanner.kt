package com.wifite.security.logic

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val level: Int,
    val frequency: Int,
    val capabilities: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val securityType: String
        get() = when {
            capabilities.contains("WPA3") -> "WPA3"
            capabilities.contains("WPA2") -> "WPA2"
            capabilities.contains("WPA") -> "WPA"
            capabilities.contains("WEP") -> "WEP"
            else -> "OPEN"
        }

    val isHidden: Boolean
        get() = ssid.isEmpty()

    val signalStrength: Int
        get() = when {
            level >= -50 -> 4
            level >= -60 -> 4
            level >= -70 -> 3
            level >= -80 -> 2
            else -> 1
        }

    val frequency5GHz: Boolean
        get() = frequency in 5000..6000
}

class WifiScanner(private val context: Context) {
    
    private val wifiManager: WifiManager? by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    }

    private var isScanning = false

    suspend fun scanNetworks(): List<WifiNetwork> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ requires location permission
                wifiManager?.scanResults?.mapNotNull { result ->
                    parseWifiNetwork(result)
                }?.filter { it.ssid.isNotEmpty() }?.distinctBy { it.bssid }
                    ?: emptyList()
            } else {
                wifiManager?.scanResults?.mapNotNull { result ->
                    parseWifiNetwork(result)
                }?.filter { it.ssid.isNotEmpty() }?.distinctBy { it.bssid }
                    ?: emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "WiFi scan error")
            emptyList()
        }
    }

    fun startScan(): Boolean {
        return try {
            isScanning = true
            val result = wifiManager?.startScan() ?: false
            Timber.d("WiFi scan started: $result")
            result
        } catch (e: Exception) {
            Timber.e(e, "WiFi start scan error")
            isScanning = false
            false
        }
    }

    fun stopScan() {
        isScanning = false
        Timber.d("WiFi scan stopped")
    }

    fun isScanningActive(): Boolean = isScanning

    private fun parseWifiNetwork(result: ScanResult): WifiNetwork? {
        return try {
            WifiNetwork(
                ssid = result.SSID.takeIf { it.isNotBlank() } ?: "[Hidden]",
                bssid = result.BSSID,
                level = result.level,
                frequency = result.frequency,
                capabilities = result.capabilities
            )
        } catch (e: Exception) {
            Timber.e(e, "Error parsing WiFi network")
            null
        }
    }

    fun getConnectedNetwork(): WifiNetwork? {
        return try {
            val connectionInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                wifiManager?.connectionInfo
            } else {
                wifiManager?.connectionInfo
            }

            connectionInfo?.let {
                WifiNetwork(
                    ssid = it.ssid.removeSurrounding("\""),
                    bssid = it.bssid ?: "",
                    level = it.rssi,
                    frequency = 0,
                    capabilities = ""
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting connected network")
            null
        }
    }

    fun isWifiEnabled(): Boolean {
        return wifiManager?.isWifiEnabled ?: false
    }

    fun enableWifi(): Boolean {
        return try {
            wifiManager?.isWifiEnabled?.let {
                if (!it) wifiManager?.isWifiEnabled = true
                true
            } ?: false
        } catch (e: Exception) {
            Timber.e(e, "Error enabling WiFi")
            false
        }
    }

    /**
     * Get detailed network info for cracking
     */
    fun getNetworkDetails(bssid: String): Map<String, String> {
        return try {
            val scanResult = wifiManager?.scanResults?.find { it.BSSID == bssid }
            if (scanResult != null) {
                mapOf(
                    "SSID" to (scanResult.SSID.ifEmpty { "[Hidden]" }),
                    "BSSID" to scanResult.BSSID,
                    "Level" to "${scanResult.level}dBm",
                    "Frequency" to "${scanResult.frequency}MHz",
                    "Capabilities" to scanResult.capabilities,
                    "Timestamp" to "${scanResult.timestamp}μs"
                )
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting network details")
            emptyMap()
        }
    }
}
