package com.amiwatch.settings

import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Build.BRAND
import android.os.Build.DEVICE
import android.os.Build.SUPPORTED_ABIS
import android.os.Build.VERSION.CODENAME
import android.os.Build.VERSION.RELEASE
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.amiwatch.BuildConfig
import com.amiwatch.R
import com.amiwatch.copyToClipboard
import com.amiwatch.databinding.ActivitySettingsBinding
import com.amiwatch.initActivity
import com.amiwatch.navBarHeight
import com.amiwatch.openLinkInBrowser
import com.amiwatch.others.AppUpdater
import com.amiwatch.others.CustomBottomDialog
import com.amiwatch.pop
import com.amiwatch.setSafeOnClickListener
import com.amiwatch.settings.saving.PrefManager
import com.amiwatch.settings.saving.PrefName
import com.amiwatch.snackString
import com.amiwatch.startMainActivity
import com.amiwatch.statusBarHeight
import com.amiwatch.themes.ThemeManager
import com.amiwatch.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random


class SettingsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingsBinding
    private var cursedCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager(this).applyTheme()
        initActivity(this)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val context = this
        binding.apply {
            settingsVersion.apply {
                text = getString(R.string.version_current, BuildConfig.VERSION_NAME)

                setOnLongClickListener {
                    copyToClipboard(getDeviceInfo(), false)
                    toast(getString(R.string.copied_device_info))
                    return@setOnLongClickListener true
                }
            }
            settingsContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = statusBarHeight
                bottomMargin = navBarHeight
            }

            onBackPressedDispatcher.addCallback(context) {
                if (PrefManager.getCustomVal("reload", false)) {
                    startMainActivity(context)
                    PrefManager.setCustomVal("reload", false)
                } else {
                    finish()
                }
            }

            settingsBack.setOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }

            binding.settingsRecyclerView.adapter = SettingsAdapter(
                arrayListOf(
                    Settings(
                        type = 1,
                        name = getString(R.string.accounts),
                        desc = getString(R.string.accounts_desc),
                        icon = R.drawable.ic_round_person_24,
                        onClick = {
                            startActivity(Intent(context, SettingsAccountActivity::class.java))
                        },
                        isActivity = true
                    ),
                    Settings(
                        type = 1,
                        name = getString(R.string.theme),
                        desc = getString(R.string.theme_desc),
                        icon = R.drawable.ic_palette,
                        onClick = {
                            startActivity(Intent(context, SettingsThemeActivity::class.java))
                        },
                        isActivity = true
                    ),
                    Settings(
                        type = 1,
                        name = getString(R.string.common),
                        desc = getString(R.string.common_desc),
                        icon = R.drawable.ic_lightbulb_24,
                        onClick = {
                            startActivity(Intent(context, SettingsCommonActivity::class.java))
                        },
                        isActivity = true
                    ),
                    Settings(
                        type = 1,
                        name = getString(R.string.anime),
                        desc = getString(R.string.anime_desc),
                        icon = R.drawable.ic_round_movie_filter_24,
                        onClick = {
                            startActivity(Intent(context, SettingsAnimeActivity::class.java))
                        },
                        isActivity = true
                    ),
                    Settings(
                        type = 1,
                        name = getString(R.string.manga),
                        desc = getString(R.string.manga_desc),
                        icon = R.drawable.ic_round_import_contacts_24,
                        onClick = {
                            startActivity(Intent(context, SettingsMangaActivity::class.java))
                        },
                        isActivity = true
                    ),
                    Settings(
                        type = 1,
                        name = getString(R.string.extensions),
                        desc = getString(R.string.extensions_desc),
                        icon = R.drawable.ic_extension,
                        onClick = {
                            startActivity(Intent(context, SettingsExtensionsActivity::class.java))
                        },
                        isActivity = true
                    ),
                    Settings(
                        type = 1,
                        name = getString(R.string.addons),
                        desc = getString(R.string.addons_desc),
                        icon = R.drawable.ic_round_restaurant_24,
                        onClick = {
                            startActivity(Intent(context, SettingsAddonActivity::class.java))
                        },
                        isActivity = true
                    ),
                    Settings(
                        type = 1,
                        name = getString(R.string.notifications),
                        desc = getString(R.string.notifications_desc),
                        icon = R.drawable.ic_round_notifications_none_24,
                        onClick = {
                            startActivity(Intent(context, SettingsNotificationActivity::class.java))
                        },
                        isActivity = true
                    ),
                    Settings(
                        type = 1,
                        name = getString(R.string.about),
                        desc = getString(R.string.about_desc),
                        icon = R.drawable.ic_round_info_24,
                        onClick = {
                            startActivity(Intent(context, SettingsAboutActivity::class.java))
                        },
                        isActivity = true
                    )
                )
            )

            settingsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                setHasFixedSize(true)
            }

            if (!BuildConfig.FLAVOR.contains("fdroid")) {
                settingsLogo.setOnLongClickListener {
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppUpdater.check(this@SettingsActivity, true)
                    }
                    true
                }
            }

            settingBuyMeCoffee.setOnClickListener {
                lifecycleScope.launch {
                    it.pop()
                }
                openLinkInBrowser(getString(R.string.coffee))
            }
            lifecycleScope.launch {
                settingBuyMeCoffee.pop()
            }

            loginDiscord.setOnClickListener {
                openLinkInBrowser(getString(R.string.discord))
            }
            loginGithub.setOnClickListener {
                openLinkInBrowser(getString(R.string.github))
            }
            loginTelegram.setOnClickListener {
                openLinkInBrowser(getString(R.string.telegram))
            }


            (settingsLogo.drawable as Animatable).start()
            val array = resources.getStringArray(R.array.tips)

            settingsLogo.setSafeOnClickListener {
                cursedCounter++
                (settingsLogo.drawable as Animatable).start()
                if (cursedCounter % 16 == 0) {
                    val oldVal: Boolean = PrefManager.getVal(PrefName.OC)
                    if (!oldVal) {
                        toast(R.string.omega_cursed)
                    } else {
                        toast(R.string.omega_freed)
                    }
                    PrefManager.setVal(PrefName.OC, !oldVal)
                } else {
                    snackString(array[(Math.random() * array.size).toInt()], context)
                }

            }

            lifecycleScope.launch(Dispatchers.IO) {
                delay(2000)
                runOnUiThread {
                    if (Random.nextInt(0, 100) > 69) {
                        CustomBottomDialog.newInstance().apply {
                            title = this@SettingsActivity.getString(R.string.enjoying_app)
                            addView(TextView(this@SettingsActivity).apply {
                                text = context.getString(R.string.consider_donating)
                            })

                            setNegativeButton(this@SettingsActivity.getString(R.string.no_moners)) {
                                snackString(R.string.you_be_rich)
                                dismiss()
                            }

                            setPositiveButton(this@SettingsActivity.getString(R.string.donate)) {
                                settingBuyMeCoffee.performClick()
                                dismiss()
                            }
                            show(supportFragmentManager, "dialog")
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun getDeviceInfo(): String {
            return """
                Amiwatch Version: ${BuildConfig.VERSION_NAME}
                Device: $BRAND $DEVICE
                Architecture: ${getArch()}
                OS Version: $CODENAME $RELEASE ($SDK_INT)
            """.trimIndent()
        }

        private fun getArch(): String {
            SUPPORTED_ABIS.forEach {
                when (it) {
                    "arm64-v8a" -> return "aarch64"
                    "armeabi-v7a" -> return "arm"
                    "x86_64" -> return "x86_64"
                    "x86" -> return "i686"
                }
            }
            return System.getProperty("os.arch") ?: System.getProperty("os.product.cpu.abi")
            ?: "Unknown Architecture"
        }
    }

    override fun onResume() {
        ThemeManager(this).applyTheme()
        super.onResume()
    }
}
