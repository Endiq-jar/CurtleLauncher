package com.app.astralauncher.ui.fragment.download.addon

import com.app.astralauncher.R
import com.app.astralauncher.feature.mod.modloader.FabricLikeUtils

class DownloadQuiltFragment : DownloadFabricLikeFragment(FabricLikeUtils.QUILT_UTILS, R.drawable.ic_quilt) {
    companion object {
        const val TAG: String = "DownloadQuiltFragment"
    }
}