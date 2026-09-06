package com.wifite.security.wifite2
class ScannerAdapter {
    fun scan() = Wifite2Bridge().scanNetworks()
}
