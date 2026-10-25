package com.app.crutlelauncher.ui.fragment
import com.app.crutlelauncher.utils.anim.CrutleTransitions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.anim.AnimPlayer
import com.app.anim.animations.Animations
import com.app.crutlelauncher.R
import com.app.crutlelauncher.databinding.FragmentVersionManagerBinding
import com.app.crutlelauncher.feature.version.NoVersionException
import com.app.crutlelauncher.feature.version.Version
import com.app.crutlelauncher.feature.version.VersionsManager
import com.app.crutlelauncher.task.Task
import com.app.crutlelauncher.task.TaskExecutors
import com.app.crutlelauncher.ui.dialog.TipDialog
import com.app.crutlelauncher.utils.ZHTools
import com.app.crutlelauncher.utils.file.FileDeletionHandler
import net.endiq.launcher.Tools
import java.io.File

class VersionManagerFragment : FragmentWithAnim(R.layout.fragment_version_manager), View.OnClickListener {
    companion object {
        const val TAG: String = "VersionManagerFragment"
    }

    private lateinit var binding: FragmentVersionManagerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVersionManagerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fragment = this
        binding.apply {
            shortcutsMods.setOnClickListener(fragment)
            gamePath.setOnClickListener(fragment)
            resourcePath.setOnClickListener(fragment)
            worldPath.setOnClickListener(fragment)
            shaderPath.setOnClickListener(fragment)
            screenshotPath.setOnClickListener(fragment)
            logsPath.setOnClickListener(fragment)
            crashReportPath.setOnClickListener(fragment)
            versionSettings.setOnClickListener(fragment)
            versionRename.setOnClickListener(fragment)
            versionCopy.setOnClickListener(fragment)
            versionDelete.setOnClickListener(fragment)
        }
    }

    private fun File.mustExists(): File {
        if (!exists()) {
            mkdirs()
        }
        return this
    }

    private fun swapFilesFragment(lockPath: File, listPath: File) {
        val bundle = Bundle()
        bundle.putString(FilesFragment.BUNDLE_LOCK_PATH, lockPath.mustExists().absolutePath)
        bundle.putString(FilesFragment.BUNDLE_LIST_PATH, listPath.mustExists().absolutePath)
        bundle.putBoolean(FilesFragment.BUNDLE_QUICK_ACCESS_PATHS, false)

        ZHTools.swapFragmentWithAnim(this, FilesFragment::class.java, FilesFragment.TAG, bundle)
    }

    override fun onClick(v: View) {
        val activity = requireActivity()
        val version: Version = VersionsManager.getCurrentVersion() ?: run {
            Tools.showError(activity, getString(R.string.version_manager_no_installed_version), NoVersionException("There is no installed version"))
            return
        }
        val gameDirPath = version.getGameDir()

        binding.apply {
            when (v) {
                shortcutsMods -> {
                    val bundle = Bundle()
                    bundle.putString(ModsFragment.BUNDLE_ROOT_PATH, File(gameDirPath, "/mods").mustExists().absolutePath)
                    ZHTools.swapFragmentWithAnim(this@VersionManagerFragment, ModsFragment::class.java, ModsFragment.TAG, bundle)
                }
                gamePath -> swapFilesFragment(gameDirPath, gameDirPath)
                resourcePath -> swapFilesFragment(gameDirPath, File(gameDirPath, "/resourcepacks"))
                worldPath -> swapFilesFragment(gameDirPath, File(gameDirPath, "/saves"))
                shaderPath -> swapFilesFragment(gameDirPath, File(gameDirPath, "/shaderpacks"))
                screenshotPath -> swapFilesFragment(gameDirPath, File(gameDirPath, "/screenshots"))
                logsPath -> swapFilesFragment(gameDirPath, File(gameDirPath, "/logs"))
                crashReportPath -> swapFilesFragment(gameDirPath, File(gameDirPath, "/crash-reports"))

                versionSettings -> ZHTools.swapFragmentWithAnim(this@VersionManagerFragment, VersionConfigFragment::class.java, VersionConfigFragment.TAG, null)
                versionRename -> {
                    VersionsManager.openRenameDialog(activity, version) {
                        Tools.backToMainMenu(activity) // leave the current Fragment before renaming
                    }
                }
                versionCopy -> VersionsManager.openCopyDialog(activity, version)
                versionDelete -> {
                    TipDialog.Builder(activity)
                        .setTitle(R.string.generic_warning)
                        .setMessage(activity.getString(R.string.version_manager_delete_tip, version.getVersionName()))
                        .setWarning()
                        .setConfirmClickListener {
                            FileDeletionHandler(
                                activity,
                                listOf(version.getVersionPath()),
                                Task.runTask {
                                    VersionsManager.refresh("VersionManagerFragment:versionDelete")
                                }.ended(TaskExecutors.getAndroidUI()) {
                                    if (!isAdded) return@ended
                                    Tools.backToMainMenu(activity)
                                }
                            ).start()
                        }
                        .showDialog()
                }
                else -> {}
            }
        }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(shortcutsLayout, CrutleTransitions.enter()))
                .apply(AnimPlayer.Entry(editLayout, CrutleTransitions.enter()))
        }
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(shortcutsLayout, CrutleTransitions.exit()))
                .apply(AnimPlayer.Entry(editLayout, CrutleTransitions.exit()))
        }
    }
}
