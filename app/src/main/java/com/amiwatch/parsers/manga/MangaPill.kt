package com.amiwatch.parsers.manga

import com.amiwatch.FileUrl
import com.amiwatch.client
import com.amiwatch.parsers.MangaChapter
import com.amiwatch.parsers.MangaImage
import com.amiwatch.parsers.MangaParser
import com.amiwatch.parsers.ShowResponse

class MangaPill : MangaParser() {

    override val name = "MangaPill"
    override val saveName = "manga_pill"
    override val hostUrl = "https://mangapill.com"

    override suspend fun loadChapters(mangaLink: String, extra: Map<String, String>?): List<MangaChapter> {
        return client.get(mangaLink).document.select("#chapters > div > a").reversed().map {
            val chap = it.text().replace("Chapter ", "")
            MangaChapter(chap, hostUrl + it.attr("href"))
        }
    }

    override suspend fun loadImages(chapterLink: String): List<MangaImage> {
        return client.get(chapterLink).document.select("img.js-page").map {
            MangaImage(FileUrl(it.attr("data-src"), mapOf("referer" to chapterLink)))
        }
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val link = "$hostUrl/quick-search?q=${encode(query)}"
        return client.get(link).document.select(".bg-card").map {
            ShowResponse(
                it.select(".font-black").text(),
                hostUrl + it.attr("href"),
                it.select("img").attr("src")
            )
        }
    }
}
