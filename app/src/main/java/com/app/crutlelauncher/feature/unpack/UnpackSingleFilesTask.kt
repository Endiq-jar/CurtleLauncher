package com.app.crutlelauncher.feature.unpack

import android.content.Context
import com.app.crutlelauncher.feature.log.Logging.e
import com.app.crutlelauncher.utils.CopyDefaultFromAssets.Companion.copyFromAssets
import com.app.crutlelauncher.utils.path.PathManager
import net.endiq.launcher.Tools

class UnpackSingleFilesTask(val context: Context) : AbstractUnpackTask() {
    override fun isNeedUnpack(): Boolean = true

    override fun run() {
        runCatching {
            copyFromAssets(context)
            Tools.copyAssetFile(context, "resolv.conf", PathManager.DIR_DATA, false)
        }.getOrElse { e("AsyncAssetManager", "Failed to unpack critical components !") }
    }
}