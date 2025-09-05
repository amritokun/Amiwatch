package com.amiwatch.parsers.manga

import com.amiwatch.client
import com.amiwatch.parsers.MangaChapter
import com.amiwatch.parsers.MangaImage
import com.amiwatch.parsers.MangaParser
import com.amiwatch.parsers.ShowResponse



class MangaKomi : MangaParser() {

    override val name = "MangaKomi"
    override val saveName = "manga_komi"
    override val hostUrl = "https://mangakomi.io"

    override suspend fun search(query: String): List<ShowResponse> {
        val doc = client.get("$hostUrl/?s=${encode(query)}&post_type=wp-manga").document
        val mediaData = doc.select("h3.h4 a")
        val titleData = doc.select("div.tab-thumb.c-image-hover a img")
        val data = titleData.zip(mediaData)
        return data.map {
            val name = it.second.text()
            val link = it.second.attr("href")
            val cover = it.first.select("img").attr("data-src")
            ShowResponse(name= name, link = link, coverUrl = cover)
        }
    }

    override suspend fun loadChapters(mangaLink: String, extra: Map<String, String>?): List<MangaChapter> {
        val doc = client.get(mangaLink).document
        val data = doc.select("li.wp-manga-chapter a")
        val chapNumRe = Regex("([0-9]+(?:\\.[0-9]+)?)")
        return data.reversed().mapNotNull {
            val number = chapNumRe.find(it.text())?.groups?.get(1)?.value ?: return@mapNotNull null
            MangaChapter(number, it.attr("href"))
        }
    }

    override suspend fun loadImages(chapterLink: String): List<MangaImage> {
        val doc = client.get(chapterLink).document
        val imgs = doc.select("div.reading-content > div > img")
        return imgs.map {
            MangaImage(url = it.attr("data-src").trim())
        }
    }

}
