package com.commonmsm.engine

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi

data class ThermalSnapshot(
    val status: String,
    val temperatureCelsius: Float,
    val isThrottling: Boolean,
    val recommendedThreads: Int
)

class ThermalGovernor(private val context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    fun getSnapshot(defaultThreads: Int = 4): ThermalSnapshot {
        var statusStr = "NOMINAL"
        var isThrottling = false
        var recommendedThreads = defaultThreads

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            val status = powerManager.currentThermalStatus
            when (status) {
                PowerManager.THERMAL_STATUS_NONE -> {
                    statusStr = "NOMINAL"
                    recommendedThreads = defaultThreads
                }
                PowerManager.THERMAL_STATUS_LIGHT -> {
                    statusStr = "LIGHT"
                    recommendedThreads = defaultThreads
                }
                PowerManager.THERMAL_STATUS_MODERATE -> {
                    statusStr = "MODERATE"
                    recommendedThreads = (defaultThreads - 1).coerceAtLeast(2)
                }
                PowerManager.THERMAL_STATUS_SEVERE,
                PowerManager.THERMAL_STATUS_CRITICAL -> {
                    statusStr = "THROTTLED"
                    isThrottling = true
                    recommendedThreads = (defaultThreads / 2).coerceAtLeast(1)
                }
                PowerManager.THERMAL_STATUS_EMERGENCY,
                PowerManager.THERMAL_STATUS_SHUTDOWN -> {
                    statusStr = "CRITICAL"
                    isThrottling = true
                    recommendedThreads = 1
                }
            }
        }

        val batteryTemp = getBatteryTemperature()

        return ThermalSnapshot(
            status = statusStr,
            temperatureCelsius = batteryTemp,
            isThrottling = isThrottling,
            recommendedThreads = recommendedThreads
        )
    }

    private fun getBatteryTemperature(): Float {
        return try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 320) ?: 320
            tempTenths / 10.0f
        } catch (e: Exception) {
            32.5f
        }
    }
}
