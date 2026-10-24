package com.app.astralauncher.ui.subassembly.screenshot

import java.io.File

data class ScreenshotItem(
    val file: File,
    val lastModified: Long,
    val sizeBytes: Long
)
