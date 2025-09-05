package com.amiwatch.parsers.anime

import com.amiwatch.FileUrl
import com.amiwatch.client
import com.amiwatch.parsers.AnimeParser
import com.amiwatch.parsers.Episode
import com.amiwatch.parsers.ShowResponse
import com.amiwatch.parsers.VideoServer

class Gogo : AnimeParser() {
    override val name = "Gogo"
    override val saveName = "gogo_anime_hu"
    override val hostUrl = "https://gogoanime.hu"
    override val malSyncBackupName = "Gogoanime"
    override val isDubAvailableSeparately = true

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?): List<Episode> {
        val pageBody = client.get("$hostUrl/category/$animeLink").document
        val lastEpisode = pageBody.select("ul#episode_page > li:last-child > a").attr("ep_end").toString()
        val animeId = pageBody.select("input#movie_id").attr("value").toString()

        val epList = client
            .get("https://ajax.gogo-load.com/ajax/load-list-episode?ep_start=0&ep_end=$lastEpisode&id=$animeId").document
            .select("ul > li > a").reversed()
        return epList.map {
            val num = it.select(".name").text().replace("EP", "").trim()
            Episode(num, hostUrl + it.attr("href").trim())
        }
    }

    private fun httpsIfy(text: String): String {
        return if (text.take(2) == "//") "https:$text"
        else text
    }

    override suspend fun loadVideoServers(episodeLink: String, extra: Map<String, String>?): List<VideoServer> {
        return client.get(episodeLink).document.select("div.anime_muti_link > ul > li").map {
            val name = it.select("a").text().replace("Choose this server", "")
            val url = httpsIfy(it.select("a").attr("data-video"))
            val embed = FileUrl(url, mapOf("referer" to hostUrl))

            VideoServer(name, embed)
        }
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val encoded = encode(query + if (selectDub) " (Dub)" else "")
        return client.get("$hostUrl/search.html?keyword=$encoded").document
            .select(".last_episodes > ul > li div.img > a").map {
                val link = it.attr("href").toString().replace("/category/", "")
                val title = it.attr("title")
                val cover = it.select("img").attr("src")
                ShowResponse(title, link, cover)
            }
    }
}
