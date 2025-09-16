package ani.saikou.anilist

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import ani.saikou.openLinkInBrowser
import java.io.File

object Anilist {
    val query: AnilistQueries = AnilistQueries()
    val mutation: AnilistMutations = AnilistMutations()

    var token: String? = null
    var username: String? = null
    var adult: Boolean = false
    var userid: Int? = null
    var avatar: String? = null
    var bg: String? = null
    var episodesWatched: Int? = null
    var chapterRead: Int? = null

    var genres: ArrayList<String>? = null
    var tags: Map<Boolean, List<String>>? = null

    val sortBy = mapOf(
        "Score" to "SCORE_DESC",
        "Popular" to "POPULARITY_DESC",
        "Trending" to "TRENDING_DESC",
        "A-Z" to "TITLE_ENGLISH",
        "Z-A" to "TITLE_ENGLISH_DESC",
        "What?" to "SCORE",
    )

    val seasons = listOf(
        "SPRING", "WINTER", "SUMMER", "FALL"
    )

    val anime_formats = listOf(
        "TV", "TV SHORT", "MOVIE", "SPECIAL", "OVA", "ONA", "MUSIC"
    )

    val manga_formats = listOf(
        "MANGA", "NOVEL", "ONE SHOT"
    )

    //Need to make a dynamic way to make this list
    val currentSeasons = listOf(
        "SUMMER" to 2022,
        "FALL" to 2022,
        "SPRING" to 2023
    )

    fun loginIntent(context: Context) {
        val clientID = 6818
        try {
            CustomTabsIntent.Builder().build().launchUrl(
                context,
                Uri.parse("https://anilist.co/api/v2/oauth/authorize?client_id=$clientID&response_type=token")
            )
        } catch (e: ActivityNotFoundException) {
            openLinkInBrowser("https://anilist.co/api/v2/oauth/authorize?client_id=$clientID&response_type=token")
        }
    }

    fun getSavedToken(context: Context): Boolean {
        if ("anilistToken" in context.fileList()) {
            token = File(context.filesDir, "anilistToken").readText()
            return true
        }
        return false
    }

    fun removeSavedToken(context: Context) {
        token = null
        username = null
        adult = false
        userid = null
        avatar = null
        bg = null
        episodesWatched = null
        chapterRead = null
        if ("anilistToken" in context.fileList()) {
            File(context.filesDir, "anilistToken").delete()
        }
    }
}
