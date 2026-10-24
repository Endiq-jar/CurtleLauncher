package com.app.astralauncher.plugins.feature

import android.content.Context
import android.content.pm.ApplicationInfo

object FeaturePluginManager {
    private const val META_IS_FEATURE_PLUGIN = "astraLauncherFeaturePlugin"
    private const val META_FEATURE_NAME = "astraLauncherFeatureName"
    private const val META_FEATURE_DESCRIPTION = "astraLauncherFeatureDescription"

    private val featurePluginList: MutableList<FeaturePlugin> = mutableListOf()

    @JvmStatic
    fun getFeaturePluginList(): List<FeaturePlugin> = featurePluginList

    @JvmStatic
    fun clearPlugin() {
        featurePluginList.clear()
    }

    /**
     * Registers [info] as a feature plugin if - and only if - it declares
     * `astraLauncherFeaturePlugin=true` in its own manifest meta-data. Called once per
     * MAIN/DEFAULT activity [com.app.astralauncher.plugins.PluginLoader] already finds
     * while scanning for renderer/driver plugins; safe to call on every installed app since
     * everything except the explicit opt-in is ignored.
     */
    @JvmStatic
    fun parseApkPlugin(context: Context, info: ApplicationInfo) {
        if (info.packageName == context.packageName) return // never surface AstraLauncher itself
        val metaData = info.metaData ?: return
        if (!metaData.getBoolean(META_IS_FEATURE_PLUGIN, false)) return

        // A plugin app can show up more than once if it declares multiple MAIN/DEFAULT
        // activities - only register its package once.
        if (featurePluginList.any { it.packageName == info.packageName }) return

        val label = metaData.getString(META_FEATURE_NAME)
            ?: runCatching { context.packageManager.getApplicationLabel(info).toString() }
                .getOrElse { info.packageName }
        val description = metaData.getString(META_FEATURE_DESCRIPTION) ?: ""

        featurePluginList.add(FeaturePlugin(info.packageName, label, description, info))
    }
}
