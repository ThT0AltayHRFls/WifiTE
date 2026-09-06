package com.wifite.security.crypto

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * PMKID Attack Implementation
 * Dictionary attack against PMKID hashes extracted from WiFi frames
 */
class PMKIDAttack {

    data class PMKIDData(
        val ssid: String,
        val bssid: String,
        val pmkid: ByteArray,
        val authenticatorMac: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PMKIDData) return false

            if (ssid != other.ssid) return false
            if (bssid != other.bssid) return false
            if (!pmkid.contentEquals(other.pmkid)) return false
            if (!authenticatorMac.contentEquals(other.authenticatorMac)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = ssid.hashCode()
            result = 31 * result + bssid.hashCode()
            result = 31 * result + pmkid.contentHashCode()
            result = 31 * result + authenticatorMac.contentHashCode()
            return result
        }
    }

    data class PMKIDResult(
        val password: String,
        val pmk: ByteArray,
        val success: Boolean,
        val timeTaken: Long
    )

    /**
     * Crack PMKID using dictionary attack
     */
    suspend fun crackPMKID(
        wordlist: List<String>,
        pmkidData: PMKIDData,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): PMKIDResult? = withContext(Dispatchers.Default) {
        val startTime = System.currentTimeMillis()

        wordlist.forEachIndexed { index, password ->
            onProgress(index, wordlist.size)

            try {
                // Derive PMK from password
                val pmk = derivePMK(password, pmkidData.ssid)

                // Calculate PMKID
                val calculatedPmkid = calculatePMKID(pmk, pmkidData)

                // Compare with captured PMKID
                if (calculatedPmkid.contentEquals(pmkidData.pmkid)) {
                    val timeTaken = System.currentTimeMillis() - startTime
                    return@withContext PMKIDResult(
                        password = password,
                        pmk = pmk,
                        success = true,
                        timeTaken = timeTaken
                    )
                }
            } catch (e: Exception) {
                Timber.d("Error testing PMKID password: ${e.message}")
            }
        }

        onProgress(wordlist.size, wordlist.size)
        return@withContext null
    }

    /**
     * Derive PMK using PBKDF2-SHA1
     */
    private fun derivePMK(password: String, ssid: String): ByteArray {
        return try {
            val keyFactory = javax.crypto.KeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec = javax.crypto.spec.PBEKeySpec(
                password.toCharArray(),
                ssid.toByteArray(Charsets.UTF_8),
                4096,
                32 * 8
            )

            val key = keyFactory.generateSecret(spec)
            key.encoded
        } catch (e: Exception) {
            Timber.e(e, "Error deriving PMK")
            ByteArray(32)
        }
    }

    /**
     * Calculate PMKID: HMAC-SHA1(PMK, "PMKName" + AA + SPA)
     * AA = Authenticator MAC
     * SPA = Supplicant MAC (Source MAC from EAPOL frame)
     */
    private fun calculatePMKID(pmk: ByteArray, pmkidData: PMKIDData): ByteArray {
        return try {
            val hmac = Mac.getInstance("HmacSHA1")
            val key = SecretKeySpec(pmk, 0, pmk.size, "HmacSHA1")
            hmac.init(key)

            val pmkNameLabel = "PMKName".toByteArray(Charsets.UTF_8)
            val input = pmkNameLabel + pmkidData.authenticatorMac

            val fullHash = hmac.doFinal(input)

            // PMKID is first 16 bytes of HMAC-SHA1 output
            fullHash.sliceArray(0..15).toByteArray()
        } catch (e: Exception) {
            Timber.e(e, "Error calculating PMKID")
            ByteArray(16)
        }
    }
}

/**
 * Hashcat PMKID format converter
 */
class PMKIDHashcatFormatter {

    /**
     * Convert PMKID to Hashcat format:
     * PMKID*01:PMKID:BSSID:CLIENTMAC:ESSID
     */
    fun formatForHashcat(pmkidData: PMKIDAttack.PMKIDData): String {
        val pmkidHex = pmkidData.pmkid.joinToString("") { "%02x".format(it) }
        val bssidHex = pmkidData.bssid
        val macHex = pmkidData.authenticatorMac.joinToString(":") { "%02x".format(it) }

        return "PMKID*01:$pmkidHex:$bssidHex:$macHex:${pmkidData.ssid}"
    }

    /**
     * Parse captured PMKID from handshake file
     */
    fun parsePMKIDCapture(captureData: ByteArray): List<PMKIDAttack.PMKIDData> {
        val results = mutableListOf<PMKIDAttack.PMKIDData>()

        try {
            // Simple parser for PMKID frames
            // In production, would parse actual pcap/pcapng format
            var offset = 0

            while (offset + 32 < captureData.size) {
                val pmkid = captureData.sliceArray(offset until offset + 16)
                val authenticatorMac = captureData.sliceArray(offset + 16 until offset + 22)

                offset += 32
            }
        } catch (e: Exception) {
            Timber.e(e, "Error parsing PMKID capture")
        }

        return results
    }
}
