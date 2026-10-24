package com.app.astralauncher.ui.fragment.download.addon

import com.app.astralauncher.R
import com.app.astralauncher.feature.mod.modloader.FabricLikeUtils

class DownloadFabricFragment : DownloadFabricLikeFragment(FabricLikeUtils.FABRIC_UTILS, R.drawable.ic_fabric) {
    companion object {
        const val TAG: String = "DownloadFabricFragment"
    }
}