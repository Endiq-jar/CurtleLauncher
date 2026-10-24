package com.app.astralauncher.ui.fragment.settings

import com.app.astralauncher.utils.anim.AstraTransitions
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.anim.AnimPlayer
import com.app.anim.animations.Animations
import com.app.astralauncher.R
import com.app.astralauncher.databinding.SettingsFragmentOptimizationBinding
import com.app.astralauncher.setting.AllSettings
import com.app.astralauncher.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import com.app.astralauncher.utils.ZHTools


class OptimizationSettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_optimization, SettingCategory.OPTIMIZATION) {
    companion object {
        const val TAG: String = "OptimizationSettingsFragment"
    }

    private lateinit var binding: SettingsFragmentOptimizationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentOptimizationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        binding.subSettingsBackButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }

        SwitchSettingsWrapper(context, AllSettings.unlimitedFps,
            binding.unlimitedFpsLayout, binding.unlimitedFps)

        SwitchSettingsWrapper(context, AllSettings.lowLatencyRendering,
            binding.lowLatencyRenderingLayout, binding.lowLatencyRendering)

        SwitchSettingsWrapper(context, AllSettings.framePacing,
            binding.framePacingLayout, binding.framePacing)

        SwitchSettingsWrapper(context, AllSettings.frameSkipping,
            binding.frameSkippingLayout, binding.frameSkipping)

        SwitchSettingsWrapper(context, AllSettings.adaptiveFrameTiming,
            binding.adaptiveFrameTimingLayout, binding.adaptiveFrameTiming)

        SwitchSettingsWrapper(context, AllSettings.autoMemoryCleanup,
            binding.autoMemoryCleanupLayout, binding.autoMemoryCleanup)

        SwitchSettingsWrapper(context, AllSettings.rendererShaderCacheEnabled,
            binding.rendererShaderCacheLayout, binding.rendererShaderCache)

        SwitchSettingsWrapper(context, AllSettings.rendererDebugLogging,
            binding.rendererDebugLoggingLayout, binding.rendererDebugLogging)
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.root, AstraTransitions.enter()))
    }
}
