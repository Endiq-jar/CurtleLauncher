package com.app.astralauncher.feature.download

import androidx.lifecycle.ViewModel
import com.app.astralauncher.feature.download.item.InfoItem
import com.app.astralauncher.feature.download.platform.AbstractPlatformHelper


class InfoViewModel : ViewModel() {
    var platformHelper: AbstractPlatformHelper? = null
    var infoItem: InfoItem? = null
}