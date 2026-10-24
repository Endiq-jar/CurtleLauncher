package com.app.astralauncher.feature.download.enums

import com.app.astralauncher.feature.download.platform.AbstractPlatformHelper
import com.app.astralauncher.feature.download.platform.modrinth.ModrinthHelper

enum class Platform(val pName: String, val helper: AbstractPlatformHelper) {
    MODRINTH("Modrinth", ModrinthHelper())
}
