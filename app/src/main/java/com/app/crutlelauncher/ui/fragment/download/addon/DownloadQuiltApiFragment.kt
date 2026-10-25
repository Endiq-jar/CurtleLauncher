package com.app.crutlelauncher.ui.fragment.download.addon

import com.app.crutlelauncher.R
import com.app.crutlelauncher.feature.version.install.Addon

class DownloadQuiltApiFragment: DownloadFabricLikeApiModFragment(
    Addon.QSL,
    "qvIfYCYJ",
    "https://modrinth.com/mod/qsl",
    "https://www.mcmod.cn/class/6246.html",
    R.drawable.ic_quilt
) {
    companion object {
        const val TAG: String = "DownloadQuiltApiFragment"
    }
}