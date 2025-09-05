package com.amiwatch.parsers.anime

import com.amiwatch.client
import com.amiwatch.parsers.AnimeParser
import com.amiwatch.parsers.Episode
import com.amiwatch.parsers.ShowResponse
import com.amiwatch.parsers.VideoServer

class AnimeDao : AnimeParser() {
    override val name = "AnimeDao"
    override val saveName = "anime_dao_bz"
    override val hostUrl = "https://animedao.bz"
    override val isDubAvailableSeparately = true

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?): List<Episode> {
        val res = client.get(animeLink).document
        return res.select(".episode_well_link").map {
            Episode(
                it.select(".anime-title").text().substringAfter("Episode "),
                hostUrl + it.attr("href")
            )
        }.reversed()
    }

    override suspend fun loadVideoServers(episodeLink: String, extra: Map<String, String>?): List<VideoServer> {
        return client.get(episodeLink)
            .document
            .select(".anime_muti_link a")
            .map {
                VideoServer(it.text(), it.attr("data-video"))
            }
    }

    override suspend fun search(query: String): List<ShowResponse> {
        return client.get("$hostUrl/search.html?keyword=$query${if(selectDub) " (Dub)" else ""}").document
            .select(".col-lg-4 a").map {
                ShowResponse(
                    it.attr("title"),
                    hostUrl + it.attr("href"),
                    it.select("img").attr("src")
                )
            }
    }
}
