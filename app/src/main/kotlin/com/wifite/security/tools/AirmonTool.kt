package com.wifite.security.tools
import timber.log.Timber

class AirmonTool {
    fun getStatus() = "Monitor mode active"
    fun startMonitoring(interfaceName: String) = Timber.d("Starting $interfaceName")
    fun stopMonitoring(interfaceName: String) = Timber.d("Stopping $interfaceName")
    fun getMonitorInterfaces() = listOf<String>()
}
