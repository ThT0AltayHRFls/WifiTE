package com.wifite.security.wifite2

class ConfigAdapter {
    fun getWifiteConfig() = mapOf(
        "monitor_mode" to true,
        "interface" to "wlan0",
        "timeout" to 60,
        "power" to 30
    )
}
