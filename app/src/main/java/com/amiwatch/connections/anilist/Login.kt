package com.amiwatch.connections.anilist

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amiwatch.logError
import com.amiwatch.settings.saving.PrefManager
import com.amiwatch.settings.saving.PrefName
import com.amiwatch.startMainActivity
import com.amiwatch.themes.ThemeManager

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeManager(this).applyTheme()
        val data: Uri? = intent?.data
        try {
            Anilist.token =
                Regex("""(?<=access_token=).+(?=&token_type)""").find(data.toString())!!.value
            PrefManager.setVal(PrefName.AnilistToken, Anilist.token ?: "")
        } catch (e: Exception) {
            logError(e)
        }
        startMainActivity(this)
    }
}
