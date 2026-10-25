package com.app.crutlelauncher.feature.download

import androidx.lifecycle.ViewModel
import com.app.crutlelauncher.feature.download.item.InfoItem
import com.app.crutlelauncher.feature.download.platform.AbstractPlatformHelper


class InfoViewModel : ViewModel() {
    var platformHelper: AbstractPlatformHelper? = null
    var infoItem: InfoItem? = null
}