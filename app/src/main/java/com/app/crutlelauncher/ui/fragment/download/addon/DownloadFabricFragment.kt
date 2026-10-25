package com.app.crutlelauncher.ui.fragment.download.addon

import com.app.crutlelauncher.R
import com.app.crutlelauncher.feature.mod.modloader.FabricLikeUtils

class DownloadFabricFragment : DownloadFabricLikeFragment(FabricLikeUtils.FABRIC_UTILS, R.drawable.ic_fabric) {
    companion object {
        const val TAG: String = "DownloadFabricFragment"
    }
}