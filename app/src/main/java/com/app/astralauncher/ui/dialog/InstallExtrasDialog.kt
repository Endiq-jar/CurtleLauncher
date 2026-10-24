package com.app.astralauncher.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.astralauncher.R
import com.app.astralauncher.databinding.DialogInstallExtrasBinding
import com.app.astralauncher.feature.log.Logging
import net.endiq.launcher.Tools

class InstallExtrasDialog(
    context: Context,
    private val onInstall: (includeAstraClient: Boolean, includeFpsBoost: Boolean) -> Unit
) : FullScreenDialog(context) {
    private val binding = DialogInstallExtrasBinding.inflate(layoutInflater)

    private var astraClientSelected = false
    private var fpsBoostSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setCancelable(true)
        setContentView(binding.root)

        window?.apply {
            val dimension = Tools.dpToPx(context.resources.getDimension(R.dimen._12sdp)).toInt()
            attributes.width = Tools.currentDisplayMetrics.widthPixels - 2 * dimension
            attributes.height = Tools.currentDisplayMetrics.heightPixels - 2 * dimension

            setGravity(Gravity.CENTER)

            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        runCatching {
            init()
        }.getOrElse {
            dismiss()
            Logging.e("InstallExtrasDialog", "Initialization failed, dismiss attempted.", it)
        }
    }

    private fun init() {
        binding.apply {
            astraClientOption.setOnClickListener {
                astraClientSelected = !astraClientSelected
                applySelected(astraClientOption, astraClientCheck, astraClientSelected)
            }
            fpsBoostOption.setOnClickListener {
                fpsBoostSelected = !fpsBoostSelected
                applySelected(fpsBoostOption, fpsBoostCheck, fpsBoostSelected)
            }

            closeButton.setOnClickListener { dismiss() }
            installButton.setOnClickListener {
                onInstall(astraClientSelected, fpsBoostSelected)
                dismiss()
            }
        }
    }

    private fun applySelected(row: ConstraintLayout, check: View, selected: Boolean) {
        row.isSelected = selected
        check.visibility = if (selected) View.VISIBLE else View.INVISIBLE
    }
}
