package com.amiwatch.parsers.anime.extractors

import android.net.Uri
import com.amiwatch.FileUrl
import com.amiwatch.client
import com.amiwatch.findBetween
import com.amiwatch.getSize
import com.amiwatch.parsers.Video
import com.amiwatch.parsers.VideoContainer
import com.amiwatch.parsers.VideoExtractor
import com.amiwatch.parsers.VideoServer
import com.amiwatch.parsers.VideoType
import java.util.*

class DoodStream(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val domain = "https://" + Uri.parse(server.embed.url).host!!
        val res = client.get(server.embed.url).text
        val hash = res.findBetween("/pass_md5/", "'")!!
        val token = res.findBetween("token=", "&")!!
        val url = client.get("$domain/pass_md5/$hash", referer = domain).text
        val link = FileUrl("$url?token=$token&expiry=${Date().time}}", mapOf("referer" to domain))
        return VideoContainer(
            listOf(Video(null, VideoType.CONTAINER, link, getSize(link)))
        )
    }
}
