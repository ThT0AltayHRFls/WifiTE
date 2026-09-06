package com.wifite.security.services
import timber.log.Timber

class CaptureService {
    fun startCapture() = Timber.d("Capture started")
    fun stopCapture() = Timber.d("Capture stopped")
    fun getHandshake() = byteArrayOf()
}
