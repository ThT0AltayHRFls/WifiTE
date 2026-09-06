package com.wifite.security.model
data class Target(
    val bssid: String,
    val ssid: String,
    val power: Int,
    val channel: Int,
    val security: String
)
