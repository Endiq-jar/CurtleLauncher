package com.app.crutlelauncher.ui.fragment.download.resource

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.app.anim.animations.Animations
import com.app.crutlelauncher.R
import com.app.crutlelauncher.event.value.InstallLocalModpackEvent
import com.app.crutlelauncher.feature.download.enums.Classify
import com.app.crutlelauncher.feature.download.utils.CategoryUtils
import com.app.crutlelauncher.feature.mod.modpack.install.InstallExtra
import com.app.crutlelauncher.task.Task
import com.app.crutlelauncher.task.TaskExecutors
import com.app.crutlelauncher.utils.path.PathManager
import com.app.crutlelauncher.utils.ZHTools
import com.app.crutlelauncher.utils.anim.ViewAnimUtils.Companion.setViewAnim
import com.app.crutlelauncher.utils.file.FileTools
import net.endiq.launcher.Tools
import net.endiq.launcher.contracts.OpenDocumentWithExtension
import org.greenrobot.eventbus.EventBus

class ModPackDownloadFragment(parentFragment: Fragment? = null) : AbstractResourceDownloadFragment(
    parentFragment,
    Classify.MODPACK,
    CategoryUtils.getModPackCategory(),
    true
) {
    private var openDocumentLauncher: ActivityResultLauncher<Any>? = null

    companion object {
        const val TAG: String = "ModPackDownloadFragment"
        /** Bundle key: pre-fills the search box, e.g. from InstallGameFragment's featured modpack row. */
        const val ARG_INITIAL_QUERY: String = "initial_query"
    }

    override fun initialSearchQuery(): String? = arguments?.getString(ARG_INITIAL_QUERY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult(OpenDocumentWithExtension(null)) { uris: List<Uri>? ->
            uris?.let { uriList ->
                uriList[0].let { result ->
                    if (!isTaskRunning()) {
                        val dialog = ZHTools.showTaskRunningDialog(requireContext())
                        Task.runTask {
                            FileTools.copyFileInBackground(requireContext(), result, PathManager.DIR_CACHE.absolutePath)
                        }.ended(TaskExecutors.getAndroidUI()) { modPackFile ->
                            modPackFile?.let {
                                EventBus.getDefault().post(InstallLocalModpackEvent(InstallExtra(true, it.absolutePath)))
                            }
                        }.onThrowable { e ->
                            Tools.showErrorRemote(e)
                        }.finallyTask(TaskExecutors.getAndroidUI()) {
                            dialog.dismiss()
                        }.execute()
                    }
                }
            }
        }
    }

    override fun initInstallButton(installButton: Button) {
        installButton.setOnClickListener {
            if (!isTaskRunning()) {
                Toast.makeText(requireActivity(), getString(R.string.select_modpack_local_tip), Toast.LENGTH_SHORT).show()
                openDocumentLauncher?.launch(null)
            } else {
                setViewAnim(installButton, Animations.Shake)
                Toast.makeText(requireActivity(), getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT).show()
            }
        }
    }
}