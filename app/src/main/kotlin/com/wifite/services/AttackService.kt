package com.wifite.security.services
import timber.log.Timber

class AttackService {
    fun deauth() = Timber.d("Deauth attack")
    fun fakeAuth() = Timber.d("Fake auth")
    fun wpsAttack() = Timber.d("WPS attack")
}
