package com.app.astralauncher.ui.fragment.settings

import com.app.astralauncher.utils.anim.AstraTransitions
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.anim.AnimPlayer
import com.app.anim.animations.Animations
import com.app.astralauncher.R
import com.app.astralauncher.databinding.SettingsFragmentAccessibilityBinding
import com.app.astralauncher.setting.AllSettings
import com.app.astralauncher.ui.fragment.settings.wrapper.ListSettingsWrapper
import com.app.astralauncher.ui.fragment.settings.wrapper.SeekBarSettingsWrapper
import com.app.astralauncher.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import com.app.astralauncher.utils.ZHTools


class AccessibilitySettingsFragment : AbstractSettingsFragment(R.layout.settings_fragment_accessibility, SettingCategory.ACCESSIBILITY) {
    companion object {
        const val TAG: String = "AccessibilitySettingsFragment"
    }

    private lateinit var binding: SettingsFragmentAccessibilityBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentAccessibilityBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()
        binding.subSettingsBackButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }

        SwitchSettingsWrapper(context, AllSettings.highContrastMode, binding.highContrastModeLayout, binding.highContrastMode)
            .setRequiresReboot()

        SeekBarSettingsWrapper(
            context,
            AllSettings.fontScale,
            binding.fontScaleLayout,
            binding.fontScaleTitle,
            binding.fontScaleSummary,
            binding.fontScaleValue,
            binding.fontScale,
            "%"
        ).setRequiresReboot()

        ListSettingsWrapper(
            context,
            AllSettings.fontFamily,
            binding.fontFamilyLayout,
            binding.fontFamilyTitle,
            binding.fontFamilyValue,
            R.array.all_font_family, R.array.all_font_family_value
        ).setRequiresReboot()
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.root, AstraTransitions.enter()))
    }
}
