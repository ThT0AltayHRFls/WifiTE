package com.wifite.security.tools
import timber.log.Timber

class AircrackTool {
    fun crack(capFile: String, wordlist: String) = 
        Timber.d("Cracking $capFile")
    fun getProgress() = 0.0
    fun cancel() = Timber.d("Crack cancelled")
}
