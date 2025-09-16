package ani.saikou.parsers.anime

import ani.saikou.FileUrl
import ani.saikou.asyncMap
import ani.saikou.client
import ani.saikou.parsers.*
import ani.saikou.parsers.anime.extractors.StreamTape
import ani.saikou.parsers.anime.extractors.VideoVard
import ani.saikou.parsers.anime.extractors.VizCloud
import ani.saikou.tryWithSuspend
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import org.jsoup.Jsoup
import java.net.URLDecoder.decode
import java.net.URLEncoder.encode

class NineAnime : AnimeParser() {

    override val name = "9anime"
    override val saveName = "9anime_pl"
    override val hostUrl = "https://9anime.pl"
    override val malSyncBackupName = "9anime"
    override val isDubAvailableSeparately = true

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?): List<Episode> {
        val animeId = client.get(animeLink).document.select("#watch-main").attr("data-id")
        val body = client.get("$hostUrl/ajax/episode/list/$animeId?vrf=${encodeVrf(animeId)}").parsed<Response>().result
        return Jsoup.parse(body).body().select("ul > li > a").mapNotNull {
            val id = it.attr("data-ids").split(",")
                .getOrNull(if (selectDub) 1 else 0) ?: return@mapNotNull null
            val num = it.attr("data-num")
            val title = it.selectFirst("span.d-title")?.text()
            val filler = it.hasClass("filler")
            Episode(num, "$hostUrl/ajax/server/list/$id?vrf=${encodeVrf(id)}", title, isFiller = filler)
        }
    }

    private val embedHeaders = mapOf("referer" to "$hostUrl/")

    override suspend fun loadVideoServers(episodeLink: String, extra: Any?): List<VideoServer> {
        val list = mutableListOf<VideoServer>()
        val body = client.get(episodeLink).parsed<Response>().result
        val document = Jsoup.parse(body)

        var videoVardDownload: VideoServer? = null

        list.addAll(document.select("li").mapNotNull {
            val name = it.text()
            val encodedStreamUrl = getEpisodeLinks(it.attr("data-link-id"))?.result?.url ?: return@mapNotNull null
            val realLink = FileUrl(decodeVrf(encodedStreamUrl), embedHeaders)

            if (name == "VideoVard") videoVardDownload = VideoServer("$name Mp4", realLink)

            VideoServer(name, realLink)
        })

        videoVardDownload?.also { list.add(it) }
        return list
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor? {
        val extractor: VideoExtractor? = when (server.name) {
            "Vidstream"     -> VizCloud(server)
            "MyCloud"       -> VizCloud(server)
            "VideoVard"     -> VideoVard(server)
            "VideoVard Mp4" -> VideoVard(server, true)
            "Streamtape"    -> StreamTape(server)
            else            -> null
        }
        return extractor
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val vrf = encodeVrf(query)
        val searchLink =
            "$hostUrl/filter?language%5B%5D=${if (selectDub) "dubbed" else "subbed"}&keyword=${encode(query)}&vrf=${vrf}&page=1"
        return client.get(searchLink).document.select("#list-items div.ani.poster.tip > a").map {
            val link = hostUrl + it.attr("href")
            val img = it.select("img")
            val title = img.attr("alt")
            val cover = img.attr("src")
            ShowResponse(title, link, cover)
        }
    }

    override suspend fun loadByVideoServers(episodeUrl: String, extra: Any?, callback: (VideoExtractor) -> Unit) {
        tryWithSuspend {
            val servers = loadVideoServers(episodeUrl, extra).map { getVideoExtractor(it) }
            val mutex = Mutex()

            servers.asyncMap {
                tryWithSuspend {
                    it?.apply {
                        if (this is VizCloud) {
                            mutex.withLock {
                                load()
                                callback.invoke(this)
                            }
                        } else {
                            load()
                            callback.invoke(this)
                        }
                    }
                }
            }
        }
    }

    @Serializable
    private data class Links(val result: Url?) {
        @Serializable
        data class Url(val url: String?)
    }

    @Serializable
    data class Response(val result: String)

    private suspend fun getEpisodeLinks(id: String): Links? {
        return tryWithSuspend { client.get("$hostUrl/ajax/server/$id?vrf=${encodeVrf(id)}").parsed() }
    }


    companion object {
//        private var lastChecked = 0L
//        private const val jsonLink = "https://raw.githubusercontent.com/chenkaslowankiya/BruvFlow/main/keys.json"
        private var cipherKey: Key = Key(
            "sUuzSWVbCkqDDrkz",
            "hlPeNwkncH0fq9so",
            "GDCCEGGDCCEGGDCCHEDDFHHEDDFHHEDDIFEEGIIFEEGIIFEEJGFFHJJGFFHJJGFFKHGGIKKHGGIKKHGGLIHHJLLIHHJLLIHHMJIIKMMJIIKMMJIINKJJLNNKJJLNNKJJOLKKMOOLKKMOOLKKPMLLNPPMLLNPPMLLQNMMOQQNMMOQQNMMRONNPRRONNPRRONNSPOOQSSPOOQSSPOOTQPPRTTQPPRTTQPPURQQSUURQQSUURQQVSRRTVVSRRTVVSRRWTSSUWWTSSUWWTSSXUTTVXXUTTVXXUTTYVUUWYYVUUWYYVUUZWVVXZZWVVXZZWVV[XWWY[[XWWY[[XWW\\YXXZ\\\\YXXZ\\\\YXX]ZYY[]]ZYY[]]ZYY^[ZZ\\^^[ZZ\\^^[ZZ_\\[[]__\\[[]__\\[[`]\\\\^``]\\\\^``]\\\\gdcceggdcceggdccheddfhheddfhheddifeegiifeegiifeejgffhjjgffhjjgffkhggikkhggikkhgglihhjllihhjllihhmjiikmmjiikmmjiinkjjlnnkjjlnnkjjolkkmoolkkmoolkkpmllnppmllnppmllqnmmoqqnmmoqqnmmronnprronnprronnspooqsspooqsspootqpprttqpprttqppurqqsuurqqsuurqqvsrrtvvsrrtvvsrrwtssuwwtssuwwtssxuttvxxuttvxxuttyvuuwyyvuuwyyvuuzwvvxzzwvvxzzwvv{xwwy{{xwwy{{xww|yxxz||yxxz||yxx}zyy{}}zyy{}}zyy~{zz|~~{zz|~~{zz|{{}|{{}|{{}||~}||~}||6322466322466322743357743357743385446885446885449655799655799655:7668::7668::766;8779;;8779;;877<988:<<988:<<988=:99;==:99;==:99>;::<>>;::<>>;::?<;;=??<;;=??<;;1.--/11.--/11.--C@??ACC@??ACC@??5211355211355211ebaaceebaaceebaa"
        )

        private fun getKey(): Key {
//            cipherKey = if (cipherKey != null && (lastChecked - System.currentTimeMillis()) < 1000 * 60 * 30) cipherKey!!
//            else {
//                lastChecked = System.currentTimeMillis()
//                client.get(jsonLink).parsed()
//            }
            return cipherKey

        }

        @Serializable
        data class Key(
            val cipher: String,
            val decipher: String,
            val keyTable: String
        )

        private fun encodeVrf(text: String): String =
            encode(encrypt(mapKeys(encrypt(cipher(getKey().cipher, text), baseTable), getKey().keyTable), baseTable))

        private fun mapKeys(encrypted: String, keyMap: String): String {
            val table = keyMap.split("")
            return encrypted.mapIndexedNotNull { i, c ->
                table.getOrNull((baseTable1.indexOf(c) * 16) + 1 + (i % 16))
            }.joinToString("")
        }

        private const val baseTable = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=_"
        private const val baseTable1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+=/_"

        private fun decodeVrf(text: String): String = decode(cipher(getKey().decipher, decrypt(text)))

        fun encrypt(input: String, key: String): String {
            if (input.any { it.code > 255 }) throw Exception("illegal characters!")
            var output = ""
            for (i in input.indices step 3) {
                val a = intArrayOf(-1, -1, -1, -1)
                a[0] = input[i].code shr 2
                a[1] = (3 and input[i].code) shl 4
                if (input.length > i + 1) {
                    a[1] = a[1] or (input[i + 1].code shr 4)
                    a[2] = (15 and input[i + 1].code) shl 2
                }
                if (input.length > i + 2) {
                    a[2] = a[2] or (input[i + 2].code shr 6)
                    a[3] = 63 and input[i + 2].code
                }
                for (n in a) {
                    if (n == -1) output += "="
                    else {
                        if (n in 0..63) output += key[n]
                    }
                }
            }
            return output
        }

        fun cipher(key: String, text: String): String {
            val arr = IntArray(256) { it }

            var u = 0
            var r: Int
            arr.indices.forEach {
                u = (u + arr[it] + key[it % key.length].code) % 256
                r = arr[it]
                arr[it] = arr[u]
                arr[u] = r
            }
            u = 0
            var c = 0

            return text.indices.map { j ->
                c = (c + 1) % 256
                u = (u + arr[c]) % 256
                r = arr[c]
                arr[c] = arr[u]
                arr[u] = r
                (text[j].code xor arr[(arr[c] + arr[u]) % 256]).toChar()
            }.joinToString("")
        }

        @Suppress("SameParameterValue")
        private fun decrypt(input: String): String {
            val key = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            val t = if (input.replace("""[\t\n\f\r]""".toRegex(), "").length % 4 == 0) {
                input.replace("""==?$""".toRegex(), "")
            } else input
            if (t.length % 4 == 1 || t.contains("""[^+/0-9A-Za-z]""".toRegex())) throw Exception("bad input")
            var i: Int
            var r = ""
            var e = 0
            var u = 0
            for (o in t.indices) {
                e = e shl 6
                i = key.indexOf(t[o])
                e = e or i
                u += 6
                if (24 == u) {
                    r += ((16711680 and e) shr 16).toChar()
                    r += ((65280 and e) shr 8).toChar()
                    r += (255 and e).toChar()
                    e = 0
                    u = 0
                }
            }
            return if (12 == u) {
                e = e shr 4
                r + e.toChar()
            } else {
                if (18 == u) {
                    e = e shr 2
                    r += ((65280 and e) shr 8).toChar()
                    r += (255 and e).toChar()
                }
                r
            }
        }

        private fun encode(input: String): String = encode(input, "utf-8").replace("+", "%20")

        private fun decode(input: String): String = decode(input, "utf-8")
    }
}
//Ya boi AnimeJeff was here
