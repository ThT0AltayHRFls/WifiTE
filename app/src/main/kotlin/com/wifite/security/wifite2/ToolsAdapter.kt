package com.wifite.security.wifite2

class ToolsAdapter {
    fun startAirmon(interfaceName: String) = executeCommand("airmon-ng", "start", interfaceName)
    fun startAirodump() = executeCommand("airodump-ng", "-w", "capture")
    fun startAireplay() = executeCommand("aireplay-ng", "-0", "10", "wlan0mon")
    
    private fun executeCommand(vararg args: String): String {
        return try {
            val process = ProcessBuilder(*args).start()
            process.inputStream.bufferedReader().use.readText()
        } catch (e: Exception) {
            ""
        }
    }
}
