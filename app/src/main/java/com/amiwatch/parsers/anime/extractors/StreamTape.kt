package com.amiwatch.parsers.anime.extractors

import com.amiwatch.FileUrl
import com.amiwatch.client
import com.amiwatch.getSize
import com.amiwatch.parsers.*

class StreamTape(override val server: VideoServer) : VideoExtractor() {
    private val linkRegex = Regex("""'robotlink'\)\.innerHTML = '(.+?)'\+ \('(.+?)'\)""")

    override suspend fun extract(): VideoContainer {
        val reg = linkRegex.find(client.get(server.embed.url.replace("tape.com","adblocker.xyz")).text)?:return VideoContainer(listOf())
        val extractedUrl = FileUrl("https:${reg.groups[1]!!.value + reg.groups[2]!!.value.substring(3)}")
        return VideoContainer(listOf(Video(null, VideoType.CONTAINER, extractedUrl, getSize(extractedUrl))))
    }
}
