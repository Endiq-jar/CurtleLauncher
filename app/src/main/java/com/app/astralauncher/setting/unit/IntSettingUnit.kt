package com.app.astralauncher.setting.unit

import com.app.astralauncher.setting.Settings.Manager

class IntSettingUnit(key: String, defaultValue: Int) : AbstractSettingUnit<Int>(key, defaultValue) {
    override fun getValue() = Manager.getValue(key, defaultValue) { it.toIntOrNull() }
}