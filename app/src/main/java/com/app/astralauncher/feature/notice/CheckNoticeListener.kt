package com.app.astralauncher.feature.notice

fun interface CheckNoticeListener {
    fun onSuccessful(noticeInfo: NoticeInfo?)
}