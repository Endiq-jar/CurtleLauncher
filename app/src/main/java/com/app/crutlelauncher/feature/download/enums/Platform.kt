package com.app.crutlelauncher.feature.download.enums

import com.app.crutlelauncher.feature.download.platform.AbstractPlatformHelper
import com.app.crutlelauncher.feature.download.platform.modrinth.ModrinthHelper

enum class Platform(val pName: String, val helper: AbstractPlatformHelper) {
    MODRINTH("Modrinth", ModrinthHelper())
}
