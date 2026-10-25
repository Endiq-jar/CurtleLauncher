package com.app.crutlelauncher.startup

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.Initializer
import com.google.android.material.color.DynamicColors
import com.app.crutlelauncher.feature.log.Logging
import com.app.crutlelauncher.feature.log.NativeCrashCapture
import com.app.crutlelauncher.feature.crutle.AnrWatchdog

class CrutleStartupInitializer : Initializer<Unit> {
    companion object {
        private const val TAG = "CrutleStartupInitializer"
    }

    override fun create(context: Context) {
        // Dark-only launcher. CrutleApplication already forces this first thing in
        // onCreate(); repeating it here keeps the guarantee if that call is ever moved,
        // because this initializer still runs before the first Activity is created.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Material You dynamic color: on supported devices (Android 12+), derives the theme
        // from the system wallpaper; a no-op safe fallback to the static theme elsewhere.
        try {
            DynamicColors.applyToActivitiesIfAvailable(context.applicationContext as Application)
        } catch (t: Throwable) {
            Logging.e(TAG, "Failed to apply dynamic colors", t)
        }

        AnrWatchdog.start()

        NativeCrashCapture.checkAndReport(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
