package com.wifite.security.tools
import timber.log.Timber

class AireplayTool {
    fun deauth(bssid: String, client: String = "FF:FF:FF:FF:FF:FF") = 
        Timber.d("Deauth $bssid")
    fun injectAuthFrames(bssid: String) = Timber.d("Injected authentication to $bssid")
    fun stopAttack() = Timber.d("Attack stopped")
}
