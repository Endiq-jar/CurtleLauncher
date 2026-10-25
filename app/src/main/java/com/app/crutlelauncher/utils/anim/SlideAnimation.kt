package com.app.crutlelauncher.utils.anim

import com.app.anim.AnimPlayer

interface SlideAnimation {
    fun slideIn(animPlayer: AnimPlayer)
    fun slideOut(animPlayer: AnimPlayer)
}