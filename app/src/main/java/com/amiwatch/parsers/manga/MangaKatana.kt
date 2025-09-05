package com.amiwatch.parsers.manga

import com.amiwatch.client
import com.amiwatch.parsers.MangaChapter
import com.amiwatch.parsers.MangaImage
import com.amiwatch.parsers.MangaParser
import com.amiwatch.parsers.ShowResponse

class MangaKatana : MangaParser() {

    override val name = "MangaKatana"
    override val saveName = "manga_katana"
    override val hostUrl = "https://mangakatana.com"

    override suspend fun search(query: String): List<ShowResponse> {
        val doc = client.get("$hostUrl/?search=${encode(query)}").document
        val mediaData = doc.select("#book_list div.media div.wrap_img a")
        val titleData = doc.select("#book_list div.text h3 a")
        val data = titleData.zip(mediaData)
        return data.map {
            val name = it.first.text()
            val link = it.second.attr("href")
            val cover = it.second.select("img").attr("src")
            ShowResponse(name= name, link = link, coverUrl = cover)
        }
    }

    override suspend fun loadChapters(mangaLink: String, extra: Map<String, String>?): List<MangaChapter> {
        val doc = client.get(mangaLink).document
        val data = doc.select("tbody tr a")
        val chapNumRe = Regex("([0-9]+(?:\\.[0-9]+)?)")
        return data.reversed().map {
            MangaChapter(number = chapNumRe.find(it.text())?.groupValues?.get(1).toString(), link = it.attr("href"))
        }
    }

    override suspend fun loadImages(chapterLink: String): List<MangaImage> {
        val match = client.get(chapterLink).text.substringBefore(",];f").substringAfterLast("=[","")
        return match.split(",").map {
            MangaImage(it.replace("\"", "").replace("'", ""))
        }
    }

}
