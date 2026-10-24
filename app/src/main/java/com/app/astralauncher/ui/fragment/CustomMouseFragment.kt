package com.app.astralauncher.ui.fragment
import com.app.astralauncher.utils.anim.AstraTransitions

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.DrawableImageViewTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.app.anim.AnimPlayer
import com.app.anim.animations.Animations
import com.app.astralauncher.R
import com.app.astralauncher.databinding.FragmentCustomMouseBinding
import com.app.astralauncher.feature.log.Logging
import com.app.astralauncher.setting.AllSettings
import com.app.astralauncher.task.Task
import com.app.astralauncher.task.TaskExecutors
import com.app.astralauncher.ui.dialog.FilesDialog
import com.app.astralauncher.ui.dialog.FilesDialog.FilesButton
import com.app.astralauncher.ui.dialog.PixelEditorDialog
import com.app.astralauncher.ui.subassembly.filelist.FileIcon
import com.app.astralauncher.ui.subassembly.filelist.FileItemBean
import com.app.astralauncher.ui.subassembly.filelist.FileRecyclerViewCreator
import com.app.astralauncher.utils.NewbieGuideUtils
import com.app.astralauncher.utils.path.PathManager
import com.app.astralauncher.utils.ZHTools
import com.app.astralauncher.utils.file.FileTools
import com.app.astralauncher.utils.file.FileTools.Companion.mkdirs
import com.app.astralauncher.feature.astra.cursor.CustomCursorLoader
import com.app.astralauncher.utils.stringutils.StringUtils
import net.endiq.launcher.Tools
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CustomMouseFragment : FragmentWithAnim(R.layout.fragment_custom_mouse) {
    companion object {
        const val TAG: String = "CustomMouseFragment"
    }

    private lateinit var binding: FragmentCustomMouseBinding
    private lateinit var openDocumentLauncher: ActivityResultLauncher<Array<String>>
    private var fileRecyclerViewCreator: FileRecyclerViewCreator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openDocumentLauncher = registerForActivityResult<Array<String>, Uri>(ActivityResultContracts.OpenDocument()) { result: Uri? ->
            result?.let { uri ->
                val dialog = ZHTools.showTaskRunningDialog(requireContext())
                // Snapshot the context up front: requireActivity() from the worker
                // thread would crash if the user navigates away mid-copy.
                val appContext = requireContext().applicationContext
                val destPath = mousePath().absolutePath
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
        binding = FragmentCustomMouseBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews()

        binding.actionBar.apply {
            returnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
            addFileButton.setOnClickListener { openDocumentLauncher.launch(arrayOf("*/*")) }
            refreshButton.setOnClickListener { loadData() }
        }

        binding.drawCursorButton.setOnClickListener { openPixelEditor() }

        loadData()

        startNewbieGuide()
    }

    private fun startNewbieGuide() {
        if (NewbieGuideUtils.showOnlyOne(TAG)) return
        val fragmentActivity = requireActivity()
        binding.actionBar.apply {
            TapTargetSequence(fragmentActivity)
                .targets(
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, refreshButton, getString(R.string.generic_refresh), getString(R.string.newbie_guide_general_refresh)),
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, addFileButton, getString(R.string.custom_mouse_add), getString(R.string.newbie_guide_mouse_import)),
                    NewbieGuideUtils.getSimpleTarget(fragmentActivity, returnButton, getString(R.string.generic_close), getString(R.string.newbie_guide_general_close)))
                .start()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadData() {
        val fileItemBeans = FileRecyclerViewCreator.loadItemBeansFromPath(
            requireActivity(),
            mousePath(),
            FileIcon.FILE,
            showFile = true,
            showFolder = false
        )
        fileItemBeans.add(0, FileItemBean(
            getString(R.string.custom_mouse_default),
            ContextCompat.getDrawable(requireActivity(), R.drawable.ic_mouse_pointer)
        ))
        TaskExecutors.runInUIThread {
            fileRecyclerViewCreator?.loadData(fileItemBeans)
            // Show the currently selected mouse by default.
            refreshIcon()
        }
    }

    private fun mousePath(): File {
        val path = File(PathManager.DIR_CUSTOM_MOUSE)
        if (!path.exists()) mkdirs(path)
        return path
    }

    private fun openPixelEditor() {
        val dialog = PixelEditorDialog(requireContext())
            .setOnSaveListener { bitmap -> saveCursorBitmap(bitmap) }
        ZHTools.getCustomMouse()?.let { file ->
            val existing = runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
            if (existing != null) dialog.withInitialBitmap(existing)
        }
        dialog.show()
    }

    /** Saves the drawn bitmap as a new virtual mouse file and immediately selects it. */
    private fun saveCursorBitmap(bitmap: Bitmap?) {
        if (bitmap == null) return
        val fileName = "drawn_cursor_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ROOT).format(Date()) + ".png"
        val dest = File(mousePath(), fileName)
        try {
            FileOutputStream(dest).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: IOException) {
            Logging.e("CustomMouseFragment", "Failed to save the drawn cursor", e)
            Tools.showErrorRemote(e)
            return
        }
        AllSettings.customMouse.put(fileName).save()
        loadData()
        Toast.makeText(requireActivity(),
            StringUtils.insertSpace(getString(R.string.custom_mouse_added), fileName),
            Toast.LENGTH_SHORT).show()
    }

    private fun refreshIcon() {
        binding.mouseIcon.apply {
            ZHTools.getCustomMouse()?.let { file ->
                Glide.with(requireActivity())
                    .load(file)
                    .override(width, height)
                    .fitCenter()
                    .into(DrawableImageViewTarget(this))
                return@apply
            }
            setImageDrawable(ZHTools.customMouse(context))
        }
    }

    private fun initViews() {
        binding.actionBar.apply {
            addFileButton.setContentDescription(getString(R.string.custom_mouse_add))
            searchButton.visibility = View.GONE
            pasteButton.visibility = View.GONE
            createFolderButton.visibility = View.GONE

            ZHTools.setTooltipText(
                returnButton,
                addFileButton,
                refreshButton
            )
        }

        fileRecyclerViewCreator = FileRecyclerViewCreator(requireActivity(), binding.recyclerView, { position: Int, fileItemBean: FileItemBean ->
                val file = fileItemBean.file
                val fileName = file?.name
                val isDefaultMouse = position == 0

                val filesButton = FilesButton()
                filesButton.setButtonVisibility(false, false,
                    !isDefaultMouse, !isDefaultMouse, !isDefaultMouse, (isDefaultMouse || CustomCursorLoader.isSupportedCursorFile(file))) // the default virtual mouse cannot be shared, renamed or deleted

                // Selecting the default virtual mouse adds an extra hint.
                var message = getString(R.string.file_message)
                if (isDefaultMouse) message += """
     
     ${getString(R.string.custom_mouse_message_default)}
     """.trimIndent()
                filesButton.setMessageText(message)
                filesButton.setMoreButtonText(getString(R.string.generic_select))

                val filesDialog = FilesDialog(requireActivity(), filesButton, Task.runTask { loadData() }, mousePath(), file)
                filesDialog.setMoreButtonClick {
                    AllSettings.customMouse.put(fileName ?: "").save()
                    refreshIcon()
                    Toast.makeText(requireActivity(),
                        StringUtils.insertSpace(getString(R.string.custom_mouse_added), (fileName ?: getString(R.string.custom_mouse_default))),
                        Toast.LENGTH_SHORT).show()
                    filesDialog.dismiss()
                }
                filesDialog.show()
            },
            null
        )
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.mouseLayout, AstraTransitions.enter()))
            .apply(AnimPlayer.Entry(binding.operateLayout, AstraTransitions.enter()))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.mouseLayout, AstraTransitions.exit()))
            .apply(AnimPlayer.Entry(binding.operateLayout, AstraTransitions.exit()))
    }
}
