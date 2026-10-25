package com.app.crutlelauncher.feature.crutle.cursor

import android.graphics.Bitmap

data class CursorFrame(
    val bitmap: Bitmap,
    val hotspotX: Int,
    val hotspotY: Int,
    val delayMs: Long
)
