package com.amiwatch.connections

import com.amiwatch.R
import com.amiwatch.Refresh
import com.amiwatch.connections.anilist.Anilist
import com.amiwatch.connections.mal.MAL
import com.amiwatch.currContext
import com.amiwatch.media.Media
import com.amiwatch.settings.saving.PrefManager
import com.amiwatch.settings.saving.PrefName
import com.amiwatch.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun updateProgress(media: Media, number: String) {
    val incognito: Boolean = PrefManager.getVal(PrefName.Incognito)
    if (!incognito) {
        if (Anilist.userid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val a = number.toFloatOrNull()?.toInt()
                if ((a ?: 0) > (media.userProgress ?: -1)) {
                    Anilist.mutation.editList(
                        media.id,
                        a,
                        status = if (media.userStatus == "REPEATING") media.userStatus else "CURRENT"
                    )
                    MAL.query.editList(
                        media.idMAL,
                        media.anime != null,
                        a, null,
                        if (media.userStatus == "REPEATING") media.userStatus!! else "CURRENT"
                    )
                    toast(currContext()?.getString(R.string.setting_progress, a))
                }
                media.userProgress = a
                Refresh.all()
            }
        } else {
            toast(currContext()?.getString(R.string.login_anilist_account))
        }
    } else {
        toast("Sneaky sneaky :3")
    }
}
