package com.amiwatch.parsers.anime

import com.amiwatch.FileUrl
import com.amiwatch.asyncMap
import com.amiwatch.client
import com.amiwatch.parsers.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import java.net.URLEncoder

@Suppress("BlockingMethodInNonBlockingContext")
class Kaido : AnimeParser() {

    override val name = "Kaido"
    override val saveName = "kaido_to"
    override val hostUrl = "https://kaido.to"
    override val isDubAvailableSeparately = false

    private val header = mapOf("X-Requested-With" to "XMLHttpRequest", "referer" to hostUrl)

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?): List<Episode> {
        val res = client.get("$hostUrl/ajax/episode/list/$animeLink", header).parsed<HtmlResponse>()
        val element = Jsoup.parse(res.html ?: return listOf())
        return element.select(".detail-infor-content > div > a").map {
            val title = it.attr("title")
            val num = it.attr("data-number").replace("\n", "")
            val id = it.attr("data-id")
            val filler = it.attr("class").contains("ssl-item-filler")

            Episode(number = num, link = id, title = title, isFiller = filler)
        }
    }

    private val embedHeaders = mapOf("referer" to "$hostUrl/")

    override suspend fun loadVideoServers(episodeLink: String, extra: Map<String,String>?): List<VideoServer> {
        val res = client.get("$hostUrl/ajax/episode/servers?episodeId=$episodeLink", header).parsed<HtmlResponse>()
        val element = Jsoup.parse(res.html ?: return listOf())

        return element.select("div.server-item").asyncMap {
            val serverName = "${it.attr("data-type").uppercase()} - ${it.text()}"
            val link = client.get("$hostUrl/ajax/episode/sources?id=${it.attr("data-id")}", header)
                .parsed<SourceResponse>()
                .link
            VideoServer(serverName, FileUrl(link, embedHeaders))
        }
    }

    override suspend fun search(query: String): List<ShowResponse> {
        var url = URLEncoder.encode(query, "utf-8")
        if (query.startsWith("$!")) {
            val a = query.replace("$!", "").split(" | ")
            url = URLEncoder.encode(a[0], "utf-8") + a[1]
        }

        val document = client.get("${hostUrl}/search?keyword=$url").document

        return document.select(".film_list-wrap > .flw-item > .film-poster").map {
            val link = it.select("a").attr("data-id")
            val title = it.select("a").attr("title")
            val cover = it.select("img").attr("data-src")
            ShowResponse(title, link, FileUrl(cover))
        }
    }

    @Serializable
    data class SourceResponse(
        @SerialName("link") val link: String
    )

    @Serializable
    private data class HtmlResponse(
        @SerialName("status") val status: Boolean,
        @SerialName("html") val html: String? = null,
    )

}
