package com.wifite.security.extension
fun String.isValidMAC() = this.length == 17
fun String.toMAC() = this.uppercase()
