package com.amiwatch.parsers.anime.extractors

import com.amiwatch.client
import com.amiwatch.findBetween
import com.amiwatch.parsers.Video
import com.amiwatch.parsers.VideoContainer
import com.amiwatch.parsers.VideoExtractor
import com.amiwatch.parsers.VideoServer
import com.amiwatch.parsers.VideoType

class VidStreaming(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        if(server.embed.url.contains("srcd")) {
            val link = client.get(server.embed.url).text.findBetween("\"file\": '", "',")!!
            return VideoContainer(listOf(Video(null, VideoType.M3U8, link, null)))
        }
        val url = client.get(server.embed.url).document.select("iframe").attr("src")
        if(url.contains("filemoon")) {
            return FileMoon(VideoServer("FileMoon", url)).extract()
        }
        return GogoCDN(VideoServer("GogoCDN", url)).extract()
    }
}
