package com.app.crutlelauncher.ui.fragment.settings

import com.app.crutlelauncher.utils.anim.CrutleTransitions
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.anim.AnimPlayer
import com.app.anim.animations.Animations
import com.app.crutlelauncher.R
import com.app.crutlelauncher.databinding.SettingsFragmentRecordingBinding
import com.app.crutlelauncher.setting.AllSettings
import com.app.crutlelauncher.ui.activity.RecordingsGalleryActivity
import com.app.crutlelauncher.ui.fragment.settings.wrapper.SeekBarSettingsWrapper
import com.app.crutlelauncher.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import com.app.crutlelauncher.utils.ZHTools
import android.content.Intent


class RecordingSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_recording, SettingCategory.RECORDING) {
    companion object {
        const val TAG: String = "RecordingSettingsFragment"
    }

    private lateinit var binding: SettingsFragmentRecordingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentRecordingBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        binding.subSettingsBackButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }

        binding.openRecordingsGalleryLayout.setOnClickListener {
            startActivity(Intent(context, RecordingsGalleryActivity::class.java))
        }

        SwitchSettingsWrapper(context, AllSettings.recordingCaptureAudio, binding.recordingCaptureAudioLayout, binding.recordingCaptureAudio)

        SeekBarSettingsWrapper(
            context, AllSettings.recordingFrameRate,
            binding.recordingFrameRateLayout, binding.recordingFrameRateTitle, binding.recordingFrameRateSummary,
            binding.recordingFrameRateValue, binding.recordingFrameRate, "fps"
        )

        SeekBarSettingsWrapper(
            context, AllSettings.recordingBitrateMbps,
            binding.recordingBitrateMbpsLayout, binding.recordingBitrateMbpsTitle, binding.recordingBitrateMbpsSummary,
            binding.recordingBitrateMbpsValue, binding.recordingBitrateMbps, "Mbps"
        )

        SeekBarSettingsWrapper(
            context, AllSettings.recordingResolutionScale,
            binding.recordingResolutionScaleLayout, binding.recordingResolutionScaleTitle, binding.recordingResolutionScaleSummary,
            binding.recordingResolutionScaleValue, binding.recordingResolutionScale, "%"
        )

        SeekBarSettingsWrapper(
            context, AllSettings.recordingMaxDurationMin,
            binding.recordingMaxDurationMinLayout, binding.recordingMaxDurationMinTitle, binding.recordingMaxDurationMinSummary,
            binding.recordingMaxDurationMinValue, binding.recordingMaxDurationMin, "min"
        )
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.root, CrutleTransitions.enter()))
    }
}
