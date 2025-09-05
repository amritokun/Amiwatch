package com.amiwatch.parsers.manga

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import com.amiwatch.FileUrl
import com.amiwatch.client
import com.amiwatch.currContext
import com.amiwatch.others.webview.WebViewBottomDialog
import com.amiwatch.parsers.MangaChapter
import com.amiwatch.parsers.MangaImage
import com.amiwatch.parsers.MangaParser
import com.amiwatch.parsers.ShowResponse
import com.amiwatch.webViewInterface
import com.amiwatch.R

class MangaHub : MangaParser() {

    override val name = "MangaHub"
    override val saveName = "manga_hub"
    override val hostUrl = "https://api.mghubcdn.com/graphql"

    class CFBypass(override val location: FileUrl) : WebViewBottomDialog() {
        val mhubAccess = "mhub_access"

        override var title = "Cloudflare Bypass"
        override val webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                val cookie = cookies.getCookie(url.toString())
                if (cookie?.contains(mhubAccess) == true) {
                    val clearance = cookie.substringAfter("$mhubAccess=").substringBefore(";")
                    privateCallback.invoke(mapOf("access" to clearance))
                }
                super.onPageStarted(view, url, favicon)
            }
        }

        companion object {
            fun newInstance(url: String) = CFBypass(FileUrl(url))
        }
    }


    val at = "x-mhub-access"
    var accessString : String? = null
    private suspend fun getAccess(): String{
        if(accessString!=null) return accessString!!
        val webView = CFBypass.newInstance("https://mangahub.io/")
        val string = webViewInterface(webView)?.get("access") ?: throw Exception(currContext()?.getString(R.string.access_not_available))
        accessString = string
        return string
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val doc = client.get("$hostUrl/search?q=${encode(query)}", mapOf(at to getAccess())).document
        val data = doc.select("#mangalist div.media-left")
        return data.map { manga ->
            val link = manga.select("a").attr("href")
            val name = manga.select("img").attr("alt")
            val cover = manga.select("img").attr("src")
            ShowResponse(name = name, link = link, coverUrl = cover)
        }
    }

    override suspend fun loadChapters(mangaLink: String, extra: Map<String, String>?): List<MangaChapter> {
        val doc = client.get(mangaLink, mapOf(at to getAccess())).document
        val chapterLinks = doc.select("#noanim-content-tab > div a").map { it.attr("href") }
        return chapterLinks.reversed().map {
            MangaChapter(number = it.substringAfter("chapter-"), link = it)
        }
    }

    override suspend fun loadImages(chapterLink: String): List<MangaImage> {
        val doc = client.get(chapterLink, mapOf(at to getAccess())).document
        val p = doc.selectFirst("p")?.text()!!
        val firstPage = p.substringBefore("/").toInt()
        val totalPage = p.substringAfter("/").toInt()
        val chap = chapterLink.substringAfter("chapter-")
        val slug = doc.select("div > img:nth-child(2)").attr("src").substringAfter("imghub/").substringBefore("/")
        return (firstPage..totalPage).map {
            MangaImage(url = "https://img.mghubcdn.com/file/imghub/$slug/$chap/$it.jpg")
        }
    }

}
