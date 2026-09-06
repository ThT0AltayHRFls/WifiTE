package com.wifite.security.wifite2
class AttackAdapter {
    private val bridge = Wifite2Bridge()
    fun wpa(ssid: String, wordlist: String) = bridge.attackWPA(ssid, wordlist)
}
