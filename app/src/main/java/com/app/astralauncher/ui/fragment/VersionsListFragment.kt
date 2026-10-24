package com.app.astralauncher.ui.fragment

import com.app.astralauncher.utils.anim.AstraTransitions
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.angcyo.tablayout.DslTabLayout
import com.app.anim.AnimPlayer
import com.app.anim.animations.Animations
import com.app.astralauncher.R
import com.app.astralauncher.databinding.FragmentVersionsListBinding
import com.app.astralauncher.databinding.ItemFavoriteFolderBinding
import com.app.astralauncher.databinding.ViewSingleActionPopupBinding
import com.app.astralauncher.event.single.RefreshVersionsEvent
import com.app.astralauncher.event.single.RefreshVersionsEvent.MODE.END
import com.app.astralauncher.event.single.RefreshVersionsEvent.MODE.START
import com.app.astralauncher.event.sticky.FileSelectorEvent
import com.app.astralauncher.feature.customprofilepath.ProfilePathManager
import com.app.astralauncher.feature.version.Version
import com.app.astralauncher.feature.version.VersionsManager
import com.app.astralauncher.feature.version.favorites.FavoritesVersionUtils
import com.app.astralauncher.feature.version.pin.PinnedVersionUtils
import com.app.astralauncher.task.TaskExecutors
import com.app.astralauncher.ui.dialog.EditTextDialog
import com.app.astralauncher.ui.dialog.FavoritesVersionDialog
import com.app.astralauncher.ui.dialog.TipDialog
import com.app.astralauncher.ui.layout.AnimRelativeLayout
import com.app.astralauncher.ui.subassembly.customprofilepath.ProfileItem
import com.app.astralauncher.ui.subassembly.customprofilepath.ProfilePathAdapter
import com.app.astralauncher.ui.subassembly.version.VersionAdapter
import com.app.astralauncher.utils.path.PathManager
import com.app.astralauncher.utils.StoragePermissionsUtils
import com.app.astralauncher.utils.ZHTools
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.UUID


class VersionsListFragment : FragmentWithAnim(R.layout.fragment_versions_list) {
    companion object {
        const val TAG: String = "VersionsListFragment"
    }

    private lateinit var binding: FragmentVersionsListBinding
    private var versionsAdapter: VersionAdapter? = null
    private var profilePathAdapter: ProfilePathAdapter? = null
    private val mFavoritesFolderViewList: MutableList<View> = ArrayList()

    private val mFavoritesActionPopupWindow: PopupWindow = PopupWindow().apply {
        animationStyle = R.style.AstraPopupAnimation
        isFocusable = true
        isOutsideTouchable = true
    }

    // Drag-to-reorder for pinned versions. Only rows within the adapter's pinned prefix
    // (versionsAdapter.getPinnedCount()) may move, and only past each other - see onMove().
    // Dragging is handle-initiated only (isLongPressDragEnabled = false): the row itself already
    // has a tap-to-select click listener, so a long-press-anywhere drag would fight with that.
    private val versionReorderCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val adapter = versionsAdapter ?: return false
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) return false
            val pinnedCount = adapter.getPinnedCount()
            if (from >= pinnedCount || to >= pinnedCount) return false
            adapter.moveItem(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Swipe-to-dismiss isn't offered - dragFlags-only callback, swipeFlags is 0 above.
        }

        override fun isLongPressDragEnabled(): Boolean = false

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            versionsAdapter?.persistPinnedOrder()
        }
    }

    private val versionItemTouchHelper = ItemTouchHelper(versionReorderCallback)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val selectorEvent = EventBus.getDefault().getStickyEvent(FileSelectorEvent::class.java)

        selectorEvent?.let { event ->
            event.path?.let { path ->
                if (path.isNotEmpty() && !ProfilePathManager.containsPath(path)) {
                    EditTextDialog.Builder(requireContext())
                        .setTitle(R.string.profiles_path_create_new_title)
                        .setAsRequired()
                        .setConfirmListener { editBox, _ ->
                            ProfilePathManager.addPath(ProfileItem(UUID.randomUUID().toString(), editBox.text.toString(), path))
                            refresh()
                            true
                        }.showDialog()
                }
            }
            EventBus.getDefault().removeStickyEvent(event)
        }
        binding = FragmentVersionsListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            installNew.setOnClickListener {
                ZHTools.swapFragmentWithAnim(this@VersionsListFragment, VersionSelectorFragment::class.java, VersionSelectorFragment.TAG, null)
            }

            fun refreshFavoritesFolderTab(index: Int) {
                when(index) {
                    0 -> refreshVersions(true)
                    else -> refreshVersions(
                        false,
                        FavoritesVersionUtils.getFavoritesStructure().keys.toList()[index - 1]
                    )
                }
            }

            favoritesFolderTab.observeIndexChange { _, toIndex, reselect, fromUser ->
                if (fromUser && !reselect) {
                    refreshFavoritesFolderTab(toIndex)
                }
            }

            favoritesActions.setOnClickListener { showFavoritesActionPopupWindow(it) }

            versionsAdapter = VersionAdapter(this@VersionsListFragment, object : VersionAdapter.OnVersionItemClickListener {
                override fun showFavoritesDialog(versionName: String) {
                    if (FavoritesVersionUtils.getFavoritesStructure().isNotEmpty()) {
                        FavoritesVersionDialog(requireActivity(), versionName) {
                            refreshFavoritesFolderTab(favoritesFolderTab.currentItemIndex)
                        }.show()
                    } else Toast.makeText(requireActivity(), getString(R.string.version_manager_favorites_dialog_no_folders), Toast.LENGTH_SHORT).show()
                }

                override fun isVersionFavorited(versionName: String): Boolean {
                    // When the favorites filter is not "All", every listed version is a favorite.
                    if (favoritesFolderTab.currentItemIndex != 0) {
                        return true
                    }
                    return FavoritesVersionUtils.getFavoritesStructure().values.any { it.contains(versionName) }
                }

                override fun onPinChanged() {
                    refreshFavoritesFolderTab(favoritesFolderTab.currentItemIndex)
                }

                override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
                    versionItemTouchHelper.startDrag(viewHolder)
                }
            })

            versionItemTouchHelper.attachToRecyclerView(versions)

            versions.apply {
                layoutAnimation = AstraTransitions.listLayoutAnimationController(view.context)
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = versionsAdapter
                // Version rows have a fixed size: setHasFixedSize skips needless re-measures
				// and keeps scrolling smooth.
                setHasFixedSize(true)
                // Cache more off-screen item views to avoid create/bind storms while fast scrolling.
                setItemViewCacheSize(12)
            }

            profilePathAdapter = ProfilePathAdapter(this@VersionsListFragment, profilesPath)
            profilesPath.apply {
                layoutAnimation = AstraTransitions.listLayoutAnimationController(view.context)
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = profilePathAdapter
                setHasFixedSize(true)
            }

            refreshButton.setOnClickListener {
                refreshButton.isEnabled = false
                refresh(refreshVersions = true, refreshVersionInfo = true)
            }
            createPathButton.setOnClickListener {
                StoragePermissionsUtils.checkPermissions(
                    activity = requireActivity(),
                    title = R.string.profiles_path_create_new,
                    permissionGranted = object : StoragePermissionsUtils.PermissionGranted {
                        override fun granted() {
                            val bundle = Bundle()
                            bundle.putBoolean(FilesFragment.BUNDLE_SELECT_FOLDER_MODE, true)
                            bundle.putBoolean(FilesFragment.BUNDLE_SHOW_FILE, false)
                            bundle.putBoolean(FilesFragment.BUNDLE_REMOVE_LOCK_PATH, false)
                            ZHTools.swapFragmentWithAnim(this@VersionsListFragment, FilesFragment::class.java, FilesFragment.TAG, bundle)
                        }

                        override fun cancelled() {}
                    }
                )
            }
            returnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
        }

        refresh()
    }

    private fun refresh(refreshVersions: Boolean = false, refreshVersionInfo: Boolean = false) {
        ProfilePathManager.refreshPath()

        if (refreshVersions) VersionsManager.refresh("VersionListFragment:refresh", refreshVersionInfo)
        else refreshFavoritesFolderAndVersions()

        val path: MutableList<ProfileItem> = ArrayList<ProfileItem>().apply {
            add(ProfileItem("default", getString(R.string.profiles_path_default), PathManager.DIR_GAME_HOME))
            addAll(ProfilePathManager.getAllPath())
        }

        profilePathAdapter?.updateData(path)
    }

    private fun refreshVersions(all: Boolean = true, favoritesFolder: String? = null) {
        versionsAdapter?.let {
            val versions = VersionsManager.getVersions()

            fun getVersions(): List<Version> {
                if (all) return versions
                else {
                    val folderName = favoritesFolder ?: ""
                    val folderVersions = FavoritesVersionUtils.getValidVersions(folderName)
                    return ArrayList<Version>().apply {
                        versions.forEach { version ->
                            if (folderVersions.contains(version.getVersionName())) {
                                add(version)
                            }
                        }
                    }
                }
            }

            val currentIndex = it.refreshVersions(PinnedVersionUtils.applyPinnedOrder(getVersions()))
            if (currentIndex >= 0) binding.versions.scrollToPosition(currentIndex)
            binding.versions.scheduleLayoutAnimation()
        }
    }

    private fun refreshFavoritesFolderAndVersions() {
        binding.favoritesFolderTab.setCurrentItem(0)
        refreshVersions()

        mFavoritesFolderViewList.forEach { view ->
            binding.favoritesFolderTab.removeView(view)
        }
        mFavoritesFolderViewList.clear()

        fun createView(folderName: String): AnimRelativeLayout {
            return ItemFavoriteFolderBinding.inflate(layoutInflater).apply {
                name.text = folderName
                root.layoutParams = DslTabLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                // Long-press to delete.
                root.setOnLongClickListener {
                    showFavoritesDeletePopupWindow(root, folderName)
                    true
                }
            }.root
        }

        FavoritesVersionUtils.getFavoritesStructure().forEach { (folderName, _) ->
            val view = createView(folderName)
            mFavoritesFolderViewList.add(view)
            binding.favoritesFolderTab.addView(view)
        }
    }

    private fun refreshActionPopupWindow(anchorView: View, binding: ViewBinding) {
        mFavoritesActionPopupWindow.apply {
            binding.root.measure(0, 0)
            this.contentView = binding.root
            this.width = binding.root.measuredWidth
            this.height = binding.root.measuredHeight
            showAsDropDown(anchorView)
        }
    }

    private fun showFavoritesActionPopupWindow(anchorView: View) {
        refreshActionPopupWindow(anchorView, ViewSingleActionPopupBinding.inflate(LayoutInflater.from(requireActivity())).apply {
            icon.setImageDrawable(
                ContextCompat.getDrawable(requireActivity(), R.drawable.ic_add)
            )
            text.setText(R.string.version_manager_favorites_add_category)
            text.setOnClickListener {
                EditTextDialog.Builder(requireActivity())
                    .setTitle(R.string.version_manager_favorites_write_folder_name)
                    .setAsRequired()
                    .setConfirmListener { editText, _ ->
                        FavoritesVersionUtils.addFolder(editText.text.toString())
                        refreshFavoritesFolderAndVersions()
                        true
                    }.showDialog()
                mFavoritesActionPopupWindow.dismiss()
            }
        })
    }

    private fun showFavoritesDeletePopupWindow(anchorView: View, folderName: String) {
        refreshActionPopupWindow(anchorView, ViewSingleActionPopupBinding.inflate(LayoutInflater.from(requireActivity())).apply {
            icon.setImageDrawable(
                ContextCompat.getDrawable(requireActivity(), R.drawable.ic_menu_delete_forever)
            )
            text.setText(R.string.version_manager_favorites_remove_folder_title)
            text.setOnClickListener {
                TipDialog.Builder(requireActivity())
                    .setTitle(R.string.version_manager_favorites_remove_folder_title)
                    .setMessage(R.string.version_manager_favorites_remove_folder_message)
                    .setWarning()
                    .setConfirmClickListener {
                        FavoritesVersionUtils.removeFolder(folderName)
                        refreshFavoritesFolderAndVersions()
                    }.showDialog()
                mFavoritesActionPopupWindow.dismiss()
            }
        })
    }

    @Subscribe
    fun event(event: RefreshVersionsEvent) {
        // Completion callbacks (e.g. version deletion) can run after the Fragment was already
        // destroyed/detached (deleting the current version and backing out right away),
        // The binding views are detached from the window at this point; touching them could
        // throw and crash the app, so skip.
        if (!isAdded || view == null) return
        binding.apply {
            TaskExecutors.runInUIThread {
                if (!isAdded || view == null) return@runInUIThread
                when (event.mode) {
                    START -> {
                        refreshButton.isEnabled = false
                        favoritesFolderTab.isEnabled = false
                        versions.visibility = View.GONE
                        refreshVersions.visibility = View.VISIBLE
                    }
                    END -> {
                        refreshFavoritesFolderAndVersions()
                        favoritesFolderTab.isEnabled = true
                        refreshButton.isEnabled = true
                        versions.visibility = View.VISIBLE
                        refreshVersions.visibility = View.GONE
                    }
                }
                // Whatever the progress, dismiss every action dialog.
                closeAllPopupWindow()
            }
        }
    }

    private fun closeAllPopupWindow() {
        versionsAdapter?.closePopupWindow()
        profilePathAdapter?.closePopupWindow()
        mFavoritesActionPopupWindow.dismiss()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        super.onPause()
        closeAllPopupWindow()
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(versionsListLayout, AstraTransitions.enter()))
                .apply(AnimPlayer.Entry(versionTopBar, AstraTransitions.enter()))
                .apply(AnimPlayer.Entry(operateLayout, AstraTransitions.enter()))
        }
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        binding.apply {
            animPlayer.apply(AnimPlayer.Entry(versionsListLayout, AstraTransitions.exit()))
                .apply(AnimPlayer.Entry(versionTopBar, AstraTransitions.exit()))
                .apply(AnimPlayer.Entry(operateLayout, AstraTransitions.exit()))
        }
    }
}
