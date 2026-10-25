package com.app.crutlelauncher.utils.runtime

import android.content.Context
import com.app.crutlelauncher.setting.AllSettings
import com.app.crutlelauncher.task.TaskExecutors
import com.app.crutlelauncher.ui.dialog.SelectRuntimeDialog

class SelectRuntimeUtils {
    companion object {
        @JvmStatic
        fun selectRuntime(context: Context, dialogTitle: String?, selectedListener: RuntimeSelectedListener) {
            TaskExecutors.runInUIThread {
                when (AllSettings.selectRuntimeMode.getValue()) {
                    "ask_me" -> SelectRuntimeDialog(context, selectedListener).apply {
                        dialogTitle?.let { setTitleText(it) }
                    }.show()
                    "default" -> selectedListener.onSelected(AllSettings.defaultRuntime.getValue().takeIf { it.isNotEmpty() })
                    "auto" -> selectedListener.onSelected(null)
                    else -> {}
                }
            }
        }
    }
}