package com.amiwatch.parsers.anime.extractors

import com.amiwatch.FileUrl
import com.amiwatch.client
import com.amiwatch.getSize
import com.amiwatch.parsers.Video
import com.amiwatch.parsers.VideoContainer
import com.amiwatch.parsers.VideoExtractor
import com.amiwatch.parsers.VideoServer
import com.amiwatch.parsers.VideoType

class Mp4Upload(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val link = client.get(server.embed.url).document
            .select("script").html()
            .substringAfter("src: \"").substringBefore("\"")
        val host = link.substringAfter("https://").substringBefore("/")
        val file = FileUrl(link, mapOf("host" to host))
        return VideoContainer(
            listOf(Video(null, VideoType.CONTAINER, file, getSize(file)))
        )
    }
}
