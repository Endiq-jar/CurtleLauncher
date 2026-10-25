package com.app.crutlelauncher.feature.version.install

import com.app.crutlelauncher.feature.log.Logging

enum class ExtraModInstall(val displayName: String) {
    CRUTLE_CLIENT("Crutle Client"),
    FPS_BOOST("FPS Boost");

    companion object {
        fun logPending(extra: ExtraModInstall) {
            Logging.w(
                "ExtraModInstall",
                "${extra.displayName} was selected in the install popup, but no mod resource is configured for it yet - skipping."
            )
        }
    }
}
