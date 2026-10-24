package com.app.astralauncher.ui.fragment.download.resource

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.app.astralauncher.R
import com.app.astralauncher.feature.download.enums.Classify
import com.app.astralauncher.feature.download.platform.AbstractPlatformHelper.Companion.getModsPath
import com.app.astralauncher.feature.download.utils.CategoryUtils
import com.app.astralauncher.task.Task
import com.app.astralauncher.task.TaskExecutors
import com.app.astralauncher.utils.ZHTools
import com.app.astralauncher.utils.file.FileTools
import net.endiq.launcher.Tools
import net.endiq.launcher.contracts.OpenDocumentWithExtension

class ModDownloadFragment(parentFragment: Fragment? = null) : AbstractResourceDownloadFragment(
    parentFragment,
    Classify.MOD,
    CategoryUtils.getModCategory(),
    true
) {
    private var openDocumentLauncher: ActivityResultLauncher<Any>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult(OpenDocumentWithExtension("jar", true)) { uris: List<Uri>? ->
            uris?.let { uriList ->
                val dialog = ZHTools.showTaskRunningDialog((requireContext()))
                Task.runTask {
                    uriList.forEach { uri ->
                        FileTools.copyFileInBackground(requireActivity(), uri, getModsPath().absolutePath)
                    }
                }.onThrowable { e ->
                    Tools.showErrorRemote(e)
                }.finallyTask(TaskExecutors.getAndroidUI()) {
                    dialog.dismiss()
                }.execute()
            }
        }
    }

    override fun initInstallButton(installButton: Button) {
        installButton.setOnClickListener {
            val suffix = ".jar"
            Toast.makeText(
                requireActivity(),
                String.format(getString(R.string.file_add_file_tip), suffix),
                Toast.LENGTH_SHORT
            ).show()
            openDocumentLauncher?.launch(suffix)
        }
    }
}