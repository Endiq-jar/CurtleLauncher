package com.app.astralauncher.task

fun interface OnTaskEndedListener<V> {
    @Throws(Throwable::class)
    fun onEnded(result: V?)
}