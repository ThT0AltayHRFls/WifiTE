package com.wifite.security.tools
import timber.log.Timber

class John {
    fun crack(hash: String) = Timber.d("John the ripper")
    fun wordlist(file: String) = Timber.d("John wordlist: $file")
}
