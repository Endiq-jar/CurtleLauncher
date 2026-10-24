package com.app.astralauncher.ui.dialog

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.app.astralauncher.R
import com.app.astralauncher.launch.AstraJREAutoInstaller
import com.app.astralauncher.task.TaskExecutors
import net.endiq.launcher.multirt.MultiRTConfigDialog

/** Same four explicit runtimes as TeamPojavLauncher, using the launcher's shared verified installer. */
object ManualJavaDownloadDialog {
    @JvmStatic
    fun show(context: Context) {
        val majors = intArrayOf(8, 17, 21, 25)
        AlertDialog.Builder(context).setTitle(R.string.java_download_manual)
            .setItems(majors.map { "Java $it" }.toTypedArray()) { _, index ->
                val progress = AlertDialog.Builder(context).setTitle(R.string.java_download_manual)
                    .setMessage(context.getString(R.string.java_download_progress, majors[index]))
                    .setCancelable(false).create()
                progress.show()
                TaskExecutors.getDefault().execute {
                    val result = runCatching { AstraJREAutoInstaller.downloadJava(context.applicationContext, majors[index]) }
                    TaskExecutors.runInUIThread {
                        progress.dismiss()
                        AlertDialog.Builder(context).setTitle(R.string.java_download_manual)
                            .setMessage(result.fold(
                                { context.getString(R.string.java_download_success, it) },
                                { context.getString(R.string.java_download_failure, it.message) }))
                            .setPositiveButton(android.R.string.ok, null).show()
                    }
                }
            }.setNegativeButton(android.R.string.cancel, null).show()
    }
}
