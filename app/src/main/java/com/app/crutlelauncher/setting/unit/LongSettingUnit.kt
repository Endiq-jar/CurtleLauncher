package com.app.crutlelauncher.setting.unit

import com.app.crutlelauncher.setting.Settings.Manager

class LongSettingUnit(key: String, defaultValue: Long) : AbstractSettingUnit<Long>(key, defaultValue) {
    override fun getValue() = Manager.getValue(key, defaultValue) { it.toLongOrNull() }
}