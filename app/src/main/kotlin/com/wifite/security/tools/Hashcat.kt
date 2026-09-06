package com.wifite.security.tools
import timber.log.Timber

class Hashcat {
    fun bruteForce(hash: String) = Timber.d("GPU brute force")
    fun dictionaryAttack(hash: String, wordlist: String) = 
        Timber.d("Hashcat dictionary attack")
}
