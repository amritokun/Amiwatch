package ani.saikou.parsers.anime

import ani.saikou.FileUrl
import ani.saikou.client
import ani.saikou.parsers.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ConsumeBili : AnimeParser() {
    override val name = "Consume Bili"
    override val saveName = "consume-bili"
    override val isDubAvailableSeparately = false
    override val hostUrl = "https://api.consumet.org/anime/bilibili"

    override suspend fun loadEpisodes(animeLink: String, extra: Map<String, String>?): List<Episode> {
        val response = client.get("$hostUrl/info?id=$animeLink").parsed<InfoResponse>()

        return response.episodes.map {
            Episode(
                number = it.number.toString(),
                link = it.id,
                title = it.title,
                thumbnail = it.image,
            )
        }
    }

    override suspend fun loadVideoServers(episodeLink: String, extra: Any?): List<VideoServer> {
        val response = client.get("$hostUrl/watch?episodeId=$episodeLink").parsed<WatchResponse>()

        val sources = response.sources.map { source ->
            Video(
                null,
                VideoType.DASH,
                FileUrl(
                    url = source.url,
                )
            )
        }

        val subtitles = response.subtitles.map {
            Subtitle(
                language = it.lang,
                url = it.url,
                type = SubtitleType.VTT
            )
        }

        return listOf(
            VideoServer(
                name = "Bilibili",
                embed = FileUrl(url = ""),
                extraData = mapOf("sources" to sources, "subtitles" to subtitles)
            )
        )
    }

    override suspend fun getVideoExtractor(server: VideoServer): VideoExtractor {
        return BilibiliExtractor(server)
    }

    class BilibiliExtractor(override val server: VideoServer) : VideoExtractor() {
        override suspend fun extract(): VideoContainer {
            val extra = server.extraData as Map<*, *>

            val subtitles = extra["subtitles"] as List<Subtitle>
            val sources = extra["sources"] as List<Video>

            return VideoContainer(
                videos = sources,
                subtitles = subtitles
            )
        }
    }

    override suspend fun search(query: String): List<ShowResponse> {
        val encoded = encode(query)
        val response = client.get("$hostUrl/$encoded").parsed<SearchResponse>()
        return response.results.map {
            ShowResponse(
                name = it.title,
                link = it.id.toString(),
                coverUrl = FileUrl(it.image)
            )
        }
    }

    @Serializable
    data class SearchResponse(
        @SerialName("totalResults") val totalResults: Int?,
        @SerialName("results") val results: ArrayList<SearchResult>
    )

    @Serializable
    data class SearchResult(
        @SerialName("id") val id: Int,
        @SerialName("title") val title: String,
        @SerialName("image") val image: String,
        @SerialName("genres") val genres: ArrayList<String>,
    )

    @Serializable
    data class InfoResponse(
        @SerialName("id") val id: String,
        @SerialName("title") val title: String,
        @SerialName("description") val description: String,
        @SerialName("episodes") val episodes: ArrayList<ConsumeBiliEpisode>,
        @SerialName("totalEpisodes") val totalEpisodes: Int?,
    )

    @Serializable
    data class WatchResponse(
        @SerialName("sources") val sources: ArrayList<ConsumeBiliSource>,
        @SerialName("subtitles") val subtitles: ArrayList<ConsumeBiliSubtitle>
    )

    @Serializable
    data class ConsumeBiliEpisode(
        @SerialName("id") val id: String,
        @SerialName("number") val number: Int,
        @SerialName("title") val title: String,
        @SerialName("image") val image: String,
    )

    @Serializable
    data class ConsumeBiliSource(
        @SerialName("url") val url: String,
        @SerialName("isM3U8") val isM3U8: Boolean,
        @SerialName("isDASH") val isDASH: Boolean
    )

    @Serializable
    data class ConsumeBiliSubtitle(
        @SerialName("lang") val lang: String,
        @SerialName("url") val url: String,
    )
}