package ani.saikou.others

import ani.saikou.client
import ani.saikou.media.Media
import ani.saikou.toastString
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

object Mal {
    private val headers = mapOf(
        "accept" to " text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
        "accept-encoding" to " gzip, deflate, br",
        "accept-language" to " en-GB,en-US;q=0.9,en;q=0.8,hi;q=0.7"
    )

    suspend fun loadMedia(media: Media) {
        try {
            withTimeout(6000) {
                if (media.anime != null) {
                    val res = client.get("https://myanimelist.net/anime/${media.idMAL}", headers).document
                    val a = res.select(".title-english").text()
                    media.nameMAL = if (a != "") a else res.select(".title-name").text()
                    media.typeMAL =
                        if (res.select("div.spaceit_pad > a").isNotEmpty()) res.select("div.spaceit_pad > a")[0].text() else null
                    media.anime.op = arrayListOf()
                    res.select(".opnening > table > tbody > tr").forEach {
                        val text = it.text()
                        if (!text.contains("Help improve our database"))
                            media.anime.op.add(it.text())
                    }
                    media.anime.ed = arrayListOf()
                    res.select(".ending > table > tbody > tr").forEach {
                        val text = it.text()
                        if (!text.contains("Help improve our database"))
                            media.anime.ed.add(it.text())
                    }
                } else {
                    val res = client.get("https://myanimelist.net/manga/${media.idMAL}", headers).document
                    val b = res.select(".title-english").text()
                    val a = res.select(".h1-title").text().removeSuffix(b)
                    media.nameMAL = a
                    media.typeMAL =
                        if (res.select("div.spaceit_pad > a").isNotEmpty()) res.select("div.spaceit_pad > a")[0].text() else null
                }
            }
        } catch (e: Exception) {
            if (e is TimeoutCancellationException) toastString("Failed to load data from MAL")
        }
    }
}