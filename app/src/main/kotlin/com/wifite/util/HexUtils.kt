package com.wifite.security.util
object HexUtils {
    fun toHex(bytes: ByteArray) = bytes.joinToString("") { "%02x".format(it) }
    fun fromHex(hex: String) = byteArrayOf()
}
