package com.app.astralauncher.ui.fragment

import com.app.anim.AnimPlayer
import com.app.astralauncher.utils.anim.SlideAnimation
import com.app.astralauncher.utils.anim.AstraTransitions

abstract class FragmentWithAnim : BaseFragment, SlideAnimation {
    private var animPlayer: AnimPlayer = AnimPlayer()

    constructor() : super()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun onStart() {
        super.onStart()
        slideIn()
    }

    fun slideIn() {
        playAnimation { slideIn(it) }
    }

    fun slideOut() {
        playAnimation { slideOut(it) }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        view?.let { AstraTransitions.applyEnter(animPlayer, it) }
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        view?.let { AstraTransitions.applyExit(animPlayer, it) }
    }

    private fun playAnimation(animationAction: (AnimPlayer) -> Unit) {
        if (AstraTransitions.isEnabled()) {
            animPlayer.clearEntries()
            animPlayer.apply {
                animationAction(this)
                start()
            }
        }
    }
}
