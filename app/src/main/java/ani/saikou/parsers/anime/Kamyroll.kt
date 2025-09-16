package ani.saikou.parsers.anime

import ani.saikou.*
import ani.saikou.media.Media
import ani.saikou.parsers.*
import ani.saikou.parsers.anime.extractors.PStream
import ani.saikou.settings.PlayerSettings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.floor

class Kamyroll : AnimeParser() {

    override val name: String = "Kamyroll"
    override val saveName: String = "kamy_roll"
    override val hostUrl: String = apiUrl
    override val isDubAvailableSeparately: Boolean = false

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?): List<Episode> {
        return if (extra?.get("type") == "series") {
            val idHeader = "id" to animeLink
            val filter = extra["filter"]
            val eps = client.get(
                "$hostUrl/content/v1/seasons",
                getHeaders(),
                params = if (filter == null) mapOf(channelHeader, localeHeader, idHeader)
                else mapOf(channelHeader, localeHeader, idHeader, "filter" to filter),
                timeout = 100
            ).parsed<EpisodesResponse>()

            data class Temp(
                val type: String,
                val thumb: String?,
                val title: String?,
                val description: String?,
                val series: MutableMap<String, String> = mutableMapOf()
            )

            val epMap = mutableMapOf<Float, Temp>()
            val dataList = (eps.items ?: return listOf()).mapNotNull { item ->
                val tit = item.title ?: return@mapNotNull null
                (item.episodes ?: return@mapNotNull null).map {
                    Pair(
                        it.sequenceNumber,
                        Temp(
                            it.type,
                            it.images?.thumbnail?.getOrNull(6)?.source,
                            it.title,
                            it.description,
                            mutableMapOf(tit to it.id)
                        )
                    )
                }
            }.flatten()
            dataList.forEach {
                val key = it.first ?: return@forEach
                epMap[key] = epMap[key] ?: it.second
                epMap[key]?.series?.putAll(it.second.series)
            }
            epMap.map {
                if (it.value.thumb != null)
                    Episode(
                        it.key.toString().replace(".0", ""),
                        it.value.type,
                        it.value.title,
                        it.value.thumb!!,
                        if (epMap.size < 700) it.value.description else null,
                        false,
                        it.value.series.toMap()
                    )
                else
                    Episode(it.key.toString(), it.value.type, extra = it.value.series.toMap())
            }
        } else {
            val eps = client.get(
                "$hostUrl/content/v1/movies",
                getHeaders(),
                params = mapOf(
                    channelHeader,
                    localeHeader,
                    "id" to animeLink
                ),
                timeout = 100
            ).parsed<MovieResponse>()
            val ep = eps.items?.sortedByDescending { it.duration }?.get(0)?.let {
                val thumb = it.images?.thumbnail?.getOrNull(5)?.source
                if (thumb != null) Episode("1", it.id, thumbnail = thumb)
                else Episode("1", it.id)
            } ?: return listOf()
            listOf(ep)
        }
    }

    override suspend fun loadVideoServers(episodeLink: String, extra: Any?): List<VideoServer> {
        return if (extra is Map<*, *>) {
            extra.map {
                VideoServer(it.key.toString(), it.value.toString())
            }
        } else {
            listOf(VideoServer(channel, episodeLink))
        }
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor = KamyrollExtractor(server)

    class KamyrollExtractor(override val server: VideoServer) : VideoExtractor() {
        override suspend fun extract(): VideoContainer {
            val eps = client.get(
                "$apiUrl/videos/v1/streams",
                getHeaders(),
                params = mapOf(
                    channelHeader,
                    "id" to server.embed.url,
                    "type" to "adaptive_hls",
                    "format" to when(settings.kamySubType){
                        0 -> "ass"
                        1 -> "vtt"
                        2 -> "srt"
                        else -> "vtt"
                    },
                ),
                timeout = 60
            ).parsed<StreamsResponse>()

            val video = eps.streams?.mapNotNull {
                it.url ?: return@mapNotNull null
                if (it.url.contains("pstream.net"))
                    return PStream(VideoServer("PStream", it.url)).extract()
                Video(
                    null,
                    VideoType.M3U8,
                    FileUrl(
                        it.url,
                        mapOf("accept" to "*/*", "accept-encoding" to "gzip")
                    ),
                    null,
                    when (it.hardsubLocale){
                        "ja-JP" -> "[ja-JP] Japanese"
                        "en-US" -> "[en-US] English"
                        "de-DE" -> "[de-DE] German"
                        "es-ES" -> "[es-ES] Spanish"
                        "es-419" -> "[es-419] Spanish"
                        "fr-FR" -> "[fr-FR] French"
                        "it-IT" -> "[it-IT] Italian"
                        "pt-BR" -> "[pt-BR] Portuguese (Brazil)"
                        "pt-PT" -> "[pt-PT] Portuguese (Portugal)"
                        "ru-RU" -> "[ru-RU] Russian"
                        "zh-CN" -> "[zh-CN] Chinese (Simplified)"
                        "tr-TR" -> "[tr-TR] Turkish"
                        "ar-ME" -> "[ar-ME] Arabic"
                        "ar-SA" -> "[ar-SA] Arabic (Saudi Arabia)"
                        "uk-UK" -> "[uk-UK] Ukrainian"
                        "he-IL" -> "[he-IL] Hebrew"
                        "pl-PL" -> "[pl-PL] Polish"
                        "ro-RO" -> "[ro-RO] Romanian"
                        "sv-SE" -> "[sv-SE] Swedish"
                        ""      -> ""
                        else -> "[${it.hardsubLocale}] "
                    } + if(it.hardsubLocale != "") " Hard-Subbed" else "Soft/No Subs",
                )
            }

            val subtitle = eps.subtitles?.mapNotNull {
                Subtitle(
                    it.locale ?: return@mapNotNull null,
                    it.url ?: return@mapNotNull null,
                    when(settings.kamySubType){
                        0 -> SubtitleType.ASS
                        1 -> SubtitleType.VTT
                        2 -> SubtitleType.SRT
                        else -> SubtitleType.VTT
                    }
                )
            }
            return VideoContainer(video ?: listOf(), subtitle ?: listOf())
        }

        @Serializable
        private data class StreamsResponse(
            @SerialName("subtitles") val subtitles: List<Subtitle>? = null,
            @SerialName("streams") var streams: List<Stream>? = null
        ) {
            @Serializable
            data class Stream(
                @SerialName("hardsub_locale") val hardsubLocale: String? = null,
                @SerialName("url") val url: String? = null
            )

            @Serializable
            data class Subtitle(
                @SerialName("locale") val locale: String? = null,
                @SerialName("url") val url: String? = null,
                @SerialName("format") val format: String? = null
            )
        }
    }

    override suspend fun autoSearch(mediaObj: Media): ShowResponse? {
        if (channel == "crunchyroll") {
            var response = loadSavedShowResponse(mediaObj.id)
            if (response != null) {
                saveShowResponse(mediaObj.id, response, true)
            } else {
                response = if (mediaObj.crunchySlug != null || mediaObj.vrvId != null) ShowResponse(
                    "Automatically",
                    mediaObj.vrvId ?: mediaObj.crunchySlug!!,
                    "",
                    extra = mapOf(
                        "type" to if (mediaObj.format != "MOVIE") "series" else "",
                        "filter" to (mediaObj.alName())
                    )
                ) else null
                if (response == null) {
                    setUserText("Searching : ${mediaObj.mainName()}")
                    response = search("$" + mediaObj.mainName()).let { if (it.isNotEmpty()) it[0] else null }
                }
                if (response == null) {
                    setUserText("Searching : ${mediaObj.nameRomaji}")
                    response = search("$" + mediaObj.nameRomaji).let { if (it.isNotEmpty()) it[0] else null }
                }
                saveShowResponse(mediaObj.id, response)
            }
            return response
        } else return super.autoSearch(mediaObj)
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val res = client.get(
            "$hostUrl/content/v1/search",
            getHeaders(),
            params = mapOf(
                channelHeader,
                "limit" to "25",
                "query" to encode(query)
            )
        ).parsed<SearchResponse>()
        return (res.items ?: listOf()).map { item ->
            val filter = if (query.startsWith("$")) query.substringAfter("$") else null
            item.items.map {
                val type = "type" to it.type
                ShowResponse(
                    name = it.title,
                    link = it.id,
                    coverUrl = it.images?.posterTall?.getOrNull(floor((it.images.posterTall.size / 2).toDouble()).toInt())?.source ?: "",
                    extra = if (filter == null) mapOf(type) else mapOf(type, "filter" to filter)
                )
            }
        }.flatten().sortedBy { levenshtein(it.name, query) }
    }

    companion object {
        private const val player = "player_settings"
        val settings = loadData<PlayerSettings>(player, toast = false) ?: PlayerSettings().apply { saveData(player, this) }
        private val subLocale = when (settings.locale) {
            0, 2 -> "en-US"
            1    -> "ja-JP"
            3    -> "de-DE"
            4    -> "es-419"
            5    -> "es-ES"
            6    -> "fr-FR"
            7    -> "it-IT"
            8    -> "ar-SA"
            9    -> "ar-ME"
            10   -> "pt-BR"
            11   -> "pt-PT"
            12   -> "ru-RU"
            13   -> "zh-CN"
            14   -> "tr-TR"
            15   -> "ar-SA"
            16   -> "uk-UK"
            17   -> "he-IL"
            18   -> "pl-PL"
            19   -> "ro-RO"
            20   -> "sv-SE"
            else -> "en-US"
        }
        private val locale = when (settings.subtitles) {
            true  -> subLocale
            false -> ""
        }
        private const val apiUrl = "https://api.kamyroll.tech"
        private const val channel = "crunchyroll"

        private var headers: Map<String, String>? = null
        private val channelHeader = "channel_id" to channel
        private val localeHeader = "locale" to locale

        private suspend fun newToken(): Map<String, String>{
            headers = headers ?: let {
                val res = client.post(
                    "$apiUrl/auth/v1/token",
                    data = mapOf(
                        "device_id" to "com.service.data",
                        "device_type" to "ani.saikou",
                        "access_token" to "HMbQeThWmZq4t7w",
                    )
                ).parsed<AccessToken>()
                mapOf("authorization" to "${res.tokenType} ${res.accessToken}")
            }
            val timestamp = System.currentTimeMillis()
            saveData("kamyrollTokenCreationDate", timestamp)
            saveData("kamyrollToken", headers)
            return headers as Map<String, String>
        }

        suspend fun getHeaders(): Map<String, String> {
            val timestamp = System.currentTimeMillis()
            val lastTime = loadData<Long>("kamyrollTokenCreationDate", currActivity(), false)

            if(lastTime == null || (timestamp - lastTime) >= 604800000){
                       newToken()
                }
            else{
                val headers: Map<String, String>? = loadData<Map<String, String>>("kamyrollToken", currActivity(), false)
                return headers!!
            }
            return headers!!
        }

        @Serializable
        private data class AccessToken(
            @SerialName("access_token")
            val accessToken: String,
            @SerialName("token_type")
            val tokenType: String,
        )
    }

    @Serializable
    private data class MovieResponse(
        @SerialName("items") val items: List<KamyEpisode>? = null,
    )

    @Serializable
    private data class EpisodesResponse(
        @SerialName("total") val total: Long? = null,
        @SerialName("items") val items: List<Item>? = null
    ) {
        @Serializable
        data class Item(
            @SerialName("title") val title: String? = null,

            @SerialName("season_number")
            val seasonNumber: Long? = null,

            @SerialName("episode_count")
            val episodeCount: Long? = null,

            @SerialName("episodes")
            val episodes: List<KamyEpisode>? = null
        )
    }

    @Serializable
    data class KamyEpisode(
        @SerialName("id") val id: String,
        @SerialName("type") val type: String,

        @SerialName("season_number")
        val seasonNumber: Float? = null,

        @SerialName("episode") val episode: String? = null,

        @SerialName("sequence_number")
        val sequenceNumber: Float? = null,

        @SerialName("title")
        val title: String? = null,

        @SerialName("description")
        val description: String? = null,

        @SerialName("is_subbed")
        val isSubbed: Boolean? = null,

        @SerialName("is_dubbed")
        val isDubbed: Boolean? = null,

        @SerialName("images") val images: Images? = null,

        @SerialName("duration_ms")
        val duration: Long? = null,
    ) {
        @Serializable
        data class Images(
            @SerialName("thumbnail") val thumbnail: List<Thumbnail>? = null
        )

        @Serializable
        data class Thumbnail(
            @SerialName("width") val width: Long? = null,
            @SerialName("height") val height: Long? = null,
            @SerialName("source") val source: String? = null
        )
    }

    @Serializable
    private data class SearchResponse(
        @SerialName("total") val total: Long? = null,
        @SerialName("items") val items: List<ResponseItem>? = null
    ) {
        @Serializable
        data class ResponseItem(@SerialName("items") val items: List<ItemItem>)

        @Serializable
        data class ItemItem(
            @SerialName("id") val id: String,
            @SerialName("media_type") val type: String,
            @SerialName("title") val title: String,
            @SerialName("images") val images: Images? = null,
        )

        @Serializable
        data class Images(
            @SerialName("poster_tall") val posterTall: List<PosterTall>
        )

        @Serializable
        data class PosterTall(
            @SerialName("source") val source: String,
        )
    }
}
