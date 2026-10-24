package com.app.anim.animations.fade

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.app.anim.animations.BaseAnimator

class FadeOutLeftAnimator : BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        return arrayOf(
            fadeOut(target),
            translate(target, "translationX", 0f, -drift(target), easeIn)
        )
    }
}
