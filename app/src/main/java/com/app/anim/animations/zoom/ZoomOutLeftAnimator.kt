package com.app.anim.animations.zoom

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import com.app.anim.animations.BaseAnimator

class ZoomOutLeftAnimator : BaseAnimator() {
    override fun getAnimators(target: View): Array<Animator> {
        return arrayOf(
            fadeOut(target),
            *scale(target, 1f, 0.62f, easeIn),
            translate(target, "translationX", 0f, -travel(target, 0.5f), easeIn)
        )
    }
}
