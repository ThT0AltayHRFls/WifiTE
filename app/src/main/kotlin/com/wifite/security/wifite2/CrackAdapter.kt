package com.wifite.security.wifite2
class CrackAdapter {
    fun crack(cap: String, wordlist: String) = 
        executeCmd("aircrack-ng", "-w", wordlist, cap)
    private fun executeCmd(vararg args: String) = 
        try { ProcessBuilder(*args).start().inputStream.bufferedReader().use.readText() } 
        catch (e: Exception) { "" }
}
