package com.app.crutlelauncher.utils.platform

import android.content.Context
import android.os.PowerManager
import com.app.crutlelauncher.feature.log.Logging

object BatterySaverManager {
    private const val TAG = "BatterySaverManager"

    fun isPowerSaveMode(context: Context): Boolean {
        return try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isPowerSaveMode ?: false
        } catch (e: Exception) {
            Logging.e(TAG, "Failed to read power save mode: $e")
            false
        }
    }
}
