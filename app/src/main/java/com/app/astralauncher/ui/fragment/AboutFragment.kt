package com.app.astralauncher.ui.fragment

import com.app.astralauncher.utils.anim.AstraTransitions
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.app.anim.AnimPlayer
import com.app.anim.animations.Animations
import com.app.astralauncher.R
import com.app.astralauncher.databinding.FragmentAboutBinding
import com.app.astralauncher.ui.dialog.TipDialog
import com.app.astralauncher.ui.fragment.about.AboutInfoPageFragment
import com.app.astralauncher.utils.ZHTools
import com.app.astralauncher.utils.path.UrlManager
import com.app.astralauncher.utils.stringutils.StringUtils


class AboutFragment : FragmentWithAnim(R.layout.fragment_about) {
    companion object {
        const val TAG: String = "AboutFragment"
    }

    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViewPager()

        binding.apply {
            appInfo.text = StringUtils.insertNewline(StringUtils.insertSpace(getString(R.string.about_version_name), ZHTools.getVersionName()),
                StringUtils.insertSpace(getString(R.string.about_version_code), ZHTools.getVersionCode()),
                StringUtils.insertSpace(getString(R.string.about_last_update_time), ZHTools.getLastUpdateTime(requireContext())),
                StringUtils.insertSpace(getString(R.string.about_version_status), ZHTools.getVersionStatus(requireContext())))
            appInfo.setOnClickListener{ StringUtils.copyText("text", appInfo.text.toString(), requireContext()) }

            returnButton.setOnClickListener { ZHTools.onBackPressed(requireActivity()) }
            supportDevelopment.setOnClickListener {
                TipDialog.Builder(requireActivity())
                    .setTitle(R.string.request_sponsorship_title)
                    .setMessage(R.string.request_sponsorship_message)
                    .setConfirm(R.string.about_button_support_development)
                    .setConfirmClickListener { ZHTools.openLink(requireActivity(), UrlManager.URL_SUPPORT) }
                    .showDialog()
            }
        }
    }

    private fun initViewPager() {
        binding.infoViewPager.apply {
            adapter = ViewPagerAdapter(requireActivity(), this)
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 1
        }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.infoViewPager, AstraTransitions.enter()))
            .apply(AnimPlayer.Entry(binding.operateLayout, AstraTransitions.enter()))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.infoViewPager, AstraTransitions.exit()))
        animPlayer.apply(AnimPlayer.Entry(binding.operateLayout, AstraTransitions.exit()))
    }

    private class ViewPagerAdapter(
        fragmentActivity: FragmentActivity,
        private val viewPager: ViewPager2
    ): FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 1
        override fun createFragment(position: Int): Fragment {
            return AboutInfoPageFragment(viewPager)
        }
    }
}

