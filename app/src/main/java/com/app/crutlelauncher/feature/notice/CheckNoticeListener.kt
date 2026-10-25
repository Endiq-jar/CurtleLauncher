package com.app.crutlelauncher.feature.notice

fun interface CheckNoticeListener {
    fun onSuccessful(noticeInfo: NoticeInfo?)
}