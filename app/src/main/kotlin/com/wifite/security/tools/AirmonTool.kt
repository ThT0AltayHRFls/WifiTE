package com.wifite.security.tools
import timber.log.Timber

class AirmonTool {
    fun getStatus() = "Monitor mode active"
    fun startMonitoring(interface: String) = Timber.d("Starting $interface")
    fun stopMonitoring(interface: String) = Timber.d("Stopping $interface")
    fun getMonitorInterfaces() = listOf<String>()
}
