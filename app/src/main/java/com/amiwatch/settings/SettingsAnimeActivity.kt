package com.amiwatch.settings

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import com.amiwatch.R
import com.amiwatch.databinding.ActivitySettingsAnimeBinding
import com.amiwatch.download.DownloadsManager
import com.amiwatch.initActivity
import com.amiwatch.media.MediaType
import com.amiwatch.navBarHeight
import com.amiwatch.restartApp
import com.amiwatch.settings.saving.PrefManager
import com.amiwatch.settings.saving.PrefName
import com.amiwatch.statusBarHeight
import com.amiwatch.themes.ThemeManager
import com.amiwatch.util.customAlertDialog
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class SettingsAnimeActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsAnimeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager(this).applyTheme()
        initActivity(this)
        val context = this
        binding = ActivitySettingsAnimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {

            settingsAnimeLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = statusBarHeight
                bottomMargin = navBarHeight
            }
            animeSettingsBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

            settingsRecyclerView.adapter = SettingsAdapter(
                arrayListOf(
                    Settings(
                        type = 1,
                        name = getString(R.string.player_settings),
                        desc = getString(R.string.player_settings_desc),
                        icon = R.drawable.ic_round_video_settings_24,
                        onClick = {
                            startActivity(Intent(context, PlayerSettingsActivity::class.java))
                        },
                        isActivity = true
                    ),
                    Settings(
                        type = 1,
                        name = getString(R.string.purge_anime_downloads),
                        desc = getString(R.string.purge_anime_downloads_desc),
                        icon = R.drawable.ic_round_delete_24,
                        onClick = {
                            context.customAlertDialog().apply {
                                setTitle(R.string.purge_anime_downloads)
                                setMessage(R.string.purge_confirm, getString(R.string.anime))
                                setPosButton(R.string.yes, onClick = {
                                    val downloadsManager = Injekt.get<DownloadsManager>()
                                    downloadsManager.purgeDownloads(MediaType.ANIME)
                                })
                                setNegButton(R.string.no)
                                show()
                            }
                        }

                    ),
                    Settings(
                        type = 2,
                        name = getString(R.string.prefer_dub),
                        desc = getString(R.string.prefer_dub_desc),
                        icon = R.drawable.ic_round_audiotrack_24,
                        isChecked = PrefManager.getVal(PrefName.SettingsPreferDub),
                        switch = { isChecked, _ ->
                            PrefManager.setVal(PrefName.SettingsPreferDub, isChecked)
                        }
                    ),
                    Settings(
                        type = 2,
                        name = getString(R.string.show_yt),
                        desc = getString(R.string.show_yt_desc),
                        icon = R.drawable.ic_round_play_circle_24,
                        isChecked = PrefManager.getVal(PrefName.ShowYtButton),
                        switch = { isChecked, _ ->
                            PrefManager.setVal(PrefName.ShowYtButton, isChecked)
                        }
                    ),
                    Settings(
                        type = 2,
                        name = getString(R.string.include_list),
                        desc = getString(R.string.include_list_anime_desc),
                        icon = R.drawable.view_list_24,
                        isChecked = PrefManager.getVal(PrefName.IncludeAnimeList),
                        switch = { isChecked, _ ->
                            PrefManager.setVal(PrefName.IncludeAnimeList, isChecked)
                            restartApp()
                        }
                    ),
                )
            )
            settingsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                setHasFixedSize(true)
            }

            var previousEp: View = when (PrefManager.getVal<Int>(PrefName.AnimeDefaultView)) {
                0 -> settingsEpList
                1 -> settingsEpGrid
                2 -> settingsEpCompact
                else -> settingsEpList
            }
            previousEp.alpha = 1f
            fun uiEp(mode: Int, current: View) {
                previousEp.alpha = 0.33f
                previousEp = current
                current.alpha = 1f
                PrefManager.setVal(PrefName.AnimeDefaultView, mode)
            }

            settingsEpList.setOnClickListener {
                uiEp(0, it)
            }

            settingsEpGrid.setOnClickListener {
                uiEp(1, it)
            }

            settingsEpCompact.setOnClickListener {
                uiEp(2, it)
            }

        }
    }
}
