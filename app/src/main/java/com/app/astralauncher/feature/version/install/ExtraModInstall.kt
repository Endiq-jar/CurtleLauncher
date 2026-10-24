package com.app.astralauncher.feature.version.install

import com.app.astralauncher.feature.log.Logging

enum class ExtraModInstall(val displayName: String) {
    ASTRA_CLIENT("Astra Client"),
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
