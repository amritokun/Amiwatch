package com.amiwatch.parsers.anime.extractors

import com.amiwatch.asyncMap
import com.amiwatch.client
import com.amiwatch.getSize
import com.amiwatch.parsers.Video
import com.amiwatch.parsers.VideoContainer
import com.amiwatch.parsers.VideoExtractor
import com.amiwatch.parsers.VideoServer
import com.amiwatch.parsers.VideoType
import com.amiwatch.tryWithSuspend
import kotlinx.serialization.Serializable

class FPlayer(override val server: VideoServer) : VideoExtractor() {

    override suspend fun extract(): VideoContainer {
        val url = server.embed.url
        val apiLink = url.replace("/v/", "/api/source/")
        return  tryWithSuspend {
            val json = client.post(apiLink, referer = url).parsed<Json>()
            if (json.success) {
                VideoContainer(json.data?.asyncMap {
                    Video(
                        it.label.replace("p", "").toIntOrNull(),
                        VideoType.CONTAINER,
                        it.file,
                        getSize(it.file)
                    )
                }?: listOf())
            } else null
        } ?: VideoContainer(listOf())
    }

    @Serializable
    private data class Data(
        val file: String,
        val label: String
    )

    @Serializable
    private data class Json(
        val success: Boolean,
        val data: List<Data>?
    )
}
