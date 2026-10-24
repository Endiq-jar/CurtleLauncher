package com.app.astralauncher.event.value

import com.app.astralauncher.feature.version.install.Addon
import com.app.astralauncher.feature.version.install.InstallTaskItem

/**
 * Broadcast when an install task starts.
 * @see com.app.astralauncher.ui.fragment.InstallGameFragment
 * @param minecraftVersion the vanilla MC version
 * @param customVersionName custom version folder name
 * @param taskMap the install tasks
 */
class InstallGameEvent(
    val minecraftVersion: String,
    val customVersionName: String,
    val taskMap: Map<Addon, InstallTaskItem>
)