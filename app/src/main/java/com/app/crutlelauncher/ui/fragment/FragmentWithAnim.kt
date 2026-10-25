package com.app.crutlelauncher.ui.fragment

import com.app.anim.AnimPlayer
import com.app.crutlelauncher.utils.anim.SlideAnimation
import com.app.crutlelauncher.utils.anim.CrutleTransitions

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
        view?.let { CrutleTransitions.applyEnter(animPlayer, it) }
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        view?.let { CrutleTransitions.applyExit(animPlayer, it) }
    }

    private fun playAnimation(animationAction: (AnimPlayer) -> Unit) {
        if (CrutleTransitions.isEnabled()) {
            animPlayer.clearEntries()
            animPlayer.apply {
                animationAction(this)
                start()
            }
        }
    }
}
