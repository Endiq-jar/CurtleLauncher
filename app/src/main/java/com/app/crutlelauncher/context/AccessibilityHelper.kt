package com.app.crutlelauncher.context

import android.content.Context
import android.content.res.Configuration
import com.app.crutlelauncher.R
import com.app.crutlelauncher.setting.AllSettings

object AccessibilityHelper {
    @JvmStatic
    fun wrapContext(context: Context): Context {
        val percent = AllSettings.fontScale.getValue().coerceIn(50, 200)
        if (percent == 100) return context

        val configuration = Configuration(context.resources.configuration)
        configuration.fontScale = percent / 100f
        return context.createConfigurationContext(configuration)
    }

    @JvmStatic
    fun applyHighContrastOverlay(context: Context) {
        if (AllSettings.highContrastMode.getValue()) {
            context.theme.applyStyle(R.style.ThemeOverlay_Crutle_HighContrast, true)
        }
    }

    @JvmStatic
    fun applyFontFamilyOverride(context: Context) {
        val styleRes = when (AllSettings.fontFamily.getValue()) {
            "sans-serif" -> R.style.ThemeOverlay_Crutle_Font_Sans
            "sans-serif-condensed" -> R.style.ThemeOverlay_Crutle_Font_Condensed
            "serif" -> R.style.ThemeOverlay_Crutle_Font_Serif
            "monospace" -> R.style.ThemeOverlay_Crutle_Font_Monospace
            else -> null // "default" (or unset) - keep AppTheme's own branding font, nothing to do
        }
        styleRes?.let { context.theme.applyStyle(it, true) }
    }
}
