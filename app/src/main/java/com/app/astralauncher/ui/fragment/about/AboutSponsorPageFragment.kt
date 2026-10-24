package com.app.astralauncher.ui.fragment.about
import com.app.astralauncher.utils.anim.AstraTransitions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.astralauncher.R
import com.app.astralauncher.databinding.FragmentAboutSponsorPageBinding
import com.app.astralauncher.feature.CheckSponsor
import com.app.astralauncher.feature.CheckSponsor.Companion.check
import com.app.astralauncher.feature.CheckSponsor.Companion.getSponsorData
import com.app.astralauncher.feature.log.Logging
import com.app.astralauncher.task.TaskExecutors
import com.app.astralauncher.ui.subassembly.about.SponsorMeta
import com.app.astralauncher.ui.subassembly.about.SponsorRecyclerAdapter

class AboutSponsorPageFragment : Fragment(R.layout.fragment_about_sponsor_page) {
    private lateinit var binding: FragmentAboutSponsorPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutSponsorPageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        check(object : CheckSponsor.CheckListener {
            override fun onFailure() {
                setSponsorVisible(false)
            }

            override fun onSuccessful(data: SponsorMeta?) {
                setSponsorVisible(true)
            }
        })
    }

    private fun setSponsorVisible(visible: Boolean) {
        TaskExecutors.runInUIThread {
            try {
                binding.sponsorLayout.visibility = if (visible) {
                    binding.sponsorRecycler.apply {
                        layoutManager = LinearLayoutManager(requireContext())
                        adapter = SponsorRecyclerAdapter(getSponsorData())
                    }
                    binding.loadingProgress.visibility = View.GONE
                    AstraTransitions.setVisibilityAnimated(binding.sponsorLayout, true)
                    View.VISIBLE
                } else View.GONE
            } catch (e: Exception) {
                Logging.e("setSponsorVisible", e.toString())
            }
        }
    }
}

