package com.app.astralauncher.ui.fragment

import androidx.fragment.app.Fragment
import com.app.astralauncher.utils.anim.AstraTransitions
import net.endiq.launcher.progresskeeper.ProgressKeeper
import net.endiq.launcher.progresskeeper.TaskCountListener

abstract class BaseFragment : Fragment, TaskCountListener {
    private var mIsTaskRunning: Boolean = false

    constructor() : super()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    open fun onBackPressed(): Boolean = true

    fun isTaskRunning() = mIsTaskRunning

    fun forceBack() {
        requireActivity().supportFragmentManager.popBackStackImmediate()
    }

    override fun onStart() {
        super.onStart()
        ProgressKeeper.addTaskCountListener(this)
        // Every screen gets the shared list motion (item animator + staggered first
        // population) without each fragment having to remember to wire its own lists.
        view?.let { AstraTransitions.installMotion(it) }
    }

    override fun onStop() {
        super.onStop()
        ProgressKeeper.removeTaskCountListener(this)
    }

    override fun onUpdateTaskCount(taskCount: Int) {
        this.mIsTaskRunning = taskCount != 0
    }
}