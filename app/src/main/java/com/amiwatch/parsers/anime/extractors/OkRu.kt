package com.amiwatch.parsers.anime.extractors

import com.amiwatch.client
import com.amiwatch.parsers.Video
import com.amiwatch.parsers.VideoContainer
import com.amiwatch.parsers.VideoExtractor
import com.amiwatch.parsers.VideoServer
import com.amiwatch.parsers.VideoType

class OkRu(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val player = client.get(server.embed.url).document.html()
        val mediaUrl = Regex("https://vd\\d+\\.mycdn\\.me/e[^\\\\]+").find(player)

        return VideoContainer(
            listOf(
                Video(null, VideoType.M3U8, mediaUrl!!.value),
                Video(null, VideoType.DASH, mediaUrl.next()!!.value)
            )
        )
    }
}
