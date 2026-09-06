package com.wifite.security.model
data class Handshake(
    val bssid: String,
    val ssid: String,
    val data: ByteArray
)
