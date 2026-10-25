package com.app.crutlelauncher.ui.fragment.settings

import com.app.crutlelauncher.utils.anim.CrutleTransitions
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.anim.AnimPlayer
import com.app.anim.animations.Animations
import com.app.crutlelauncher.R
import com.app.crutlelauncher.databinding.SettingsFragmentAccessibilityBinding
import com.app.crutlelauncher.setting.AllSettings
import com.app.crutlelauncher.ui.fragment.settings.wrapper.ListSettingsWrapper
import com.app.crutlelauncher.ui.fragment.settings.wrapper.SeekBarSettingsWrapper
import com.app.crutlelauncher.ui.fragment.settings.wrapper.SwitchSettingsWrapper
import com.app.crutlelauncher.utils.ZHTools


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
        animPlayer.apply(AnimPlayer.Entry(binding.root, CrutleTransitions.enter()))
    }
}
