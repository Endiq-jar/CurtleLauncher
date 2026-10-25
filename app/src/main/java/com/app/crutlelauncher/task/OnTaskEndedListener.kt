package com.app.crutlelauncher.task

fun interface OnTaskEndedListener<V> {
    @Throws(Throwable::class)
    fun onEnded(result: V?)
}