package com.amiwatch.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amiwatch.BottomSheetDialogFragment
import com.amiwatch.R
import com.amiwatch.databinding.BottomSheetDiscordRpcBinding
import com.amiwatch.settings.saving.PrefManager
import com.amiwatch.settings.saving.PrefName

class DiscordDialogFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetDiscordRpcBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetDiscordRpcBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (PrefManager.getCustomVal("discord_mode", "Amiwatch")) {
            "nothing" -> binding.radioNothing.isChecked = true
            "Amiwatch" -> binding.radioAmiwatch.isChecked = true
            "anilist" -> binding.radioAnilist.isChecked = true
            else -> binding.radioAnilist.isChecked = true
        }
        binding.showIcon.isChecked = PrefManager.getVal(PrefName.ShowAniListIcon)
        binding.showIcon.setOnCheckedChangeListener { _, isChecked ->
            PrefManager.setVal(PrefName.ShowAniListIcon, isChecked)
        }
        binding.anilistLinkPreview.text =
            getString(R.string.anilist_link, PrefManager.getVal<String>(PrefName.AnilistUserName))

        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                binding.radioNothing.id -> "nothing"
                binding.radioAmiwatch.id -> "Amiwatch"
                binding.radioAnilist.id -> "anilist"
                else -> "Amiwatch"
            }
            PrefManager.setCustomVal("discord_mode", mode)
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
