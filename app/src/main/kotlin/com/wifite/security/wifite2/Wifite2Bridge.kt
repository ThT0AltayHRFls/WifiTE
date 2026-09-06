package com.wifite.security.wifite2
import java.lang.ProcessBuilder

class Wifite2Bridge {
    fun scanNetworks(): List<String> {
        return try {
            val process = ProcessBuilder("python3", "/system/wifite/Wifite.py", "--scan").start()
            process.inputStream.bufferedReader().use.readLines()
        } catch (e: Exception) { emptyList() }
    }
    fun attackWPA(ssid: String, wordlist: String): String {
        return try {
            ProcessBuilder("python3", "/system/wifite/Wifite.py", "-ssid", ssid, "-wordlist", wordlist).start().inputStream.bufferedReader().use.readText()
        } catch (e: Exception) { "" }
    }
}
