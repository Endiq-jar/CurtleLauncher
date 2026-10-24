package com.app.astralauncher.ui.fragment.download.addon

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.astralauncher.R
import com.app.astralauncher.event.sticky.SelectInstallTaskEvent
import com.app.astralauncher.feature.log.Logging
import com.app.astralauncher.feature.mod.modloader.ModVersionListAdapter
import com.app.astralauncher.task.TaskExecutors
import com.app.astralauncher.ui.subassembly.modlist.ModListFragment
import com.app.astralauncher.utils.ZHTools
import net.endiq.launcher.Tools
import com.app.astralauncher.feature.mod.modloader.ForgeDownloadTask
import com.app.astralauncher.feature.version.install.Addon
import com.app.astralauncher.ui.fragment.InstallGameFragment.Companion.BUNDLE_MC_VERSION
import net.endiq.launcher.modloaders.ForgeUtils
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.Future
import java.util.function.Consumer

class DownloadForgeFragment : ModListFragment() {
    companion object {
        const val TAG: String = "DownloadForgeFragment"
    }

    override fun refreshCreatedView() {
        fragmentActivity?.let { setIcon(ContextCompat.getDrawable(it, R.drawable.ic_anvil)) }
        setTitleText("Forge")
        setLink("https://forums.minecraftforge.net/")
        setMCMod("https://www.mcmod.cn/class/30.html")
        setReleaseCheckBoxGone() // hide the "releases only" checkbox, useless here
    }

    override fun initRefresh(): Future<*> {
        return refresh(false)
    }

    override fun refresh(): Future<*> {
        return refresh(true)
    }

    private fun refresh(force: Boolean): Future<*> {
        return TaskExecutors.getDefault().submit {
            runCatching {
                TaskExecutors.runInUIThread {
                    cancelFailedToLoad()
                    componentProcessing(true)
                }
                val forgeVersions = ForgeUtils.downloadForgeVersions(force)
                processModDetails(forgeVersions)
            }.getOrElse { e ->
                TaskExecutors.runInUIThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
                Logging.e("DownloadForge", Tools.printToString(e))
            }
        }
    }

    private fun empty() {
        TaskExecutors.runInUIThread {
            componentProcessing(false)
            setFailedToLoad(getString(R.string.version_install_no_versions))
        }
    }

    private fun processModDetails(forgeVersions: List<String>?) {
        forgeVersions ?: run {
            empty()
            return
        }

        val mcVersion = arguments?.getString(BUNDLE_MC_VERSION) ?: throw IllegalArgumentException("The Minecraft version is not passed")

        val mForgeVersions: MutableMap<String, MutableList<String>> = HashMap()
        forgeVersions.forEach(Consumer { forgeVersion: String ->
            currentTask?.apply { if (isCancelled) return@Consumer }

            // Find and group Minecraft and Forge versions (a version string without a dash
            // would make substring(0, -1) crash - skip the malformed entry)
            val dashIndex = forgeVersion.indexOf("-")
            if (dashIndex <= 0) return@Consumer
            val gameVersion = forgeVersion.substring(0, dashIndex)
            addIfAbsent(mForgeVersions, gameVersion, forgeVersion)
        })

        currentTask?.apply { if (isCancelled) return }

        val mcForgeVersions = mForgeVersions[mcVersion] ?: run {
            empty()
            return
        }

        val adapter = ModVersionListAdapter(R.drawable.ic_anvil, mcForgeVersions)
        adapter.setOnItemClickListener { version: Any ->
            if (isTaskRunning()) return@setOnItemClickListener false

            val versionString = version.toString()
            EventBus.getDefault().postSticky(
                SelectInstallTaskEvent(
                    Addon.FORGE,
                    versionString,
                    ForgeDownloadTask(versionString)
                )
            )

            ZHTools.onBackPressed(requireActivity())
            true
        }

        currentTask?.apply { if (isCancelled) return }

        TaskExecutors.runInUIThread {
            val recyclerView = recyclerView
            runCatching {
                fragmentActivity?.let { recyclerView.layoutManager = LinearLayoutManager(it) }
                recyclerView.adapter = adapter
            }.getOrElse { e ->
                Logging.e("Set Adapter", Tools.printToString(e))
            }

            componentProcessing(false)
            recyclerView.scheduleLayoutAnimation()
        }
    }
}
