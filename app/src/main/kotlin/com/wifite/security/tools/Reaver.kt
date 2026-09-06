package com.wifite.security.tools
import timber.log.Timber

class Reaver {
    fun wpsAttack(bssid: String) = Timber.d("WPS attack on $bssid")
    fun getPin() = ""
    fun cancel() = Timber.d("WPS cancelled")
}
