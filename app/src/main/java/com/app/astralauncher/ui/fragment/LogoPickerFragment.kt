package com.app.astralauncher.ui.fragment
import com.app.astralauncher.utils.anim.AstraTransitions

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import com.app.anim.AnimPlayer
import com.app.astralauncher.R
import com.app.astralauncher.databinding.FragmentLogoPickerBinding
import com.app.astralauncher.feature.LogoPackUnpacker
import com.app.astralauncher.feature.logo.HomeScreenIcon
import com.app.astralauncher.setting.AllSettings
import com.app.astralauncher.task.Task
import com.app.astralauncher.task.TaskExecutors
import com.app.astralauncher.ui.subassembly.logo.LogoEntry
import com.app.astralauncher.ui.subassembly.logo.LogoEntryAdapter
import com.app.astralauncher.utils.ZHTools
import com.app.astralauncher.utils.file.FileTools
import com.app.astralauncher.utils.file.FileTools.Companion.mkdirs
import com.app.astralauncher.utils.path.PathManager
import net.endiq.launcher.Tools
import java.io.File

/**
 * Launcher logo picker - Discord "App Icon" settings style: a plain scrolling
 * list of thumbnail + name + selected checkmark, where tapping any row applies
 * it immediately. No separate preview pane, no per-row rename/share/delete menu
 * (unlike the file-browser-style pickers elsewhere in this app, e.g. the virtual
 * mouse picker) - "Add Logo" in the header is the only extra action, for
 * importing a logo of the user's own alongside the bundled pack.
 */
class LogoPickerFragment : FragmentWithAnim(R.layout.fragment_logo_picker) {
    companion object {
        const val TAG: String = "LogoPickerFragment"
        // Static column count (matches ScreenshotsGalleryActivity's GRID_COLUMNS
        // convention) rather than a width-derived one - the launcher is always
        // sensorLandscape, so a fixed count is fine and keeps this simple.
        private const val GRID_COLUMNS = 6
    }

    private lateinit var binding: FragmentLogoPickerBinding
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>
    private var adapter: LogoEntryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult<Array<String>, Uri>(ActivityResultContracts.OpenDocument()) { result: Uri? ->
            result?.let { uri ->
                val dialog = ZHTools.showTaskRunningDialog(requireContext())
                // Snapshot the context up front: requireActivity() from the worker
                // thread would crash if the user navigates away mid-copy.
                val appContext = requireContext().applicationContext
                val destPath = logoDir().absolutePath
                Task.runTask {
                    FileTools.copyFileInBackground(appContext, uri, destPath)
                }.ended(TaskExecutors.getAndroidUI()) {
                    if (!isAdded) return@ended
                    Toast.makeText(requireActivity(), getString(R.string.file_added), Toast.LENGTH_SHORT).show()
                    loadData()
                }.onThrowable { e ->
                    Tools.showErrorRemote(e)
                }.finallyTask(TaskExecutors.getAndroidUI()) {
                    dialog.dismiss()
                }.execute()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogoPickerBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.returnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
        binding.logoAddButton.setOnClickListener { openDocumentLauncher.launch(arrayOf("image/*")) }

        binding.logoRecycler.layoutManager = GridLayoutManager(requireContext(), GRID_COLUMNS)
        adapter = LogoEntryAdapter(
            mutableListOf(),
            thumbnailCornerRadiusPx = resources.getDimensionPixelSize(R.dimen.radius_item),
            selectedValue = AllSettings.selectedLogo.getValue()
        ) { entry ->
            AllSettings.selectedLogo.put(entry.settingValue).save()
            // Also switch the home-screen icon when this logo is one of the presets.
            val appContext = requireContext().applicationContext
            val bundled = HomeScreenIcon.isBundled(appContext, entry.file?.name)
            val switched = HomeScreenIcon.apply(appContext, entry.file?.name)
            val message = when {
                !switched -> R.string.logo_home_icon_failed
                bundled -> R.string.logo_home_icon_changed
                else -> R.string.logo_home_icon_default
            }
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
        binding.logoRecycler.adapter = adapter

        ensurePackUnpacked()
    }

    /** Extracts the bundled icon pack into DIR_CUSTOM_LOGO on first open only. */
    private fun ensurePackUnpacked() {
        if (AllSettings.logoPackUnpacked.getValue()) {
            loadData()
            return
        }
        val dialog = ZHTools.showTaskRunningDialog(requireContext())
        val appContext = requireContext().applicationContext
        Task.runTask {
            LogoPackUnpacker.unpackIfNeeded(appContext)
        }.ended(TaskExecutors.getAndroidUI()) {
            if (!isAdded) return@ended
            loadData()
        }.onThrowable { e ->
            Tools.showErrorRemote(e)
        }.finallyTask(TaskExecutors.getAndroidUI()) {
            dialog.dismiss()
        }.execute()
    }

    private fun loadData() {
        Task.runTask {
            // Logos that can be the home-screen icon come first; imported ones follow by name.
            val bundled = LogoPackUnpacker.bundledNames(requireContext().applicationContext)
            val files = logoDir().listFiles { f -> f.isFile }
                ?.sortedWith(compareBy({ it.name !in bundled }, { it.name }))
                ?: emptyList()
            val entries = mutableListOf(LogoEntry(null, getString(R.string.custom_logo_default)))
            entries.addAll(files.map { LogoEntry(it, displayName(it)) })
            entries
        }.ended(TaskExecutors.getAndroidUI()) { entries: List<LogoEntry>? ->
            if (!isAdded) return@ended
            adapter?.replaceAll(entries.orEmpty())
        }.execute()
    }

    private fun logoDir(): File {
        val path = File(PathManager.DIR_CUSTOM_LOGO)
        if (!path.exists()) mkdirs(path)
        return path
    }

    /** "0001-original-studio-clean.png" -> "Original Studio Clean"; falls back to the
     *  raw name (e.g. "variant-1722" -> "Variant 1722") when nothing's left to title-case. */
    private fun displayName(file: File): String {
        val base = file.nameWithoutExtension
        val slug = base.replace(Regex("^(variant-)?\\d+-?"), "").ifBlank { base }
        return slug.split('-', '_')
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
            .ifBlank { base }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.logoTitle, AstraTransitions.enter()))
            .apply(AnimPlayer.Entry(binding.logoListCard, AstraTransitions.enter()))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.logoTitle, AstraTransitions.exit()))
            .apply(AnimPlayer.Entry(binding.logoListCard, AstraTransitions.exit()))
    }
}
