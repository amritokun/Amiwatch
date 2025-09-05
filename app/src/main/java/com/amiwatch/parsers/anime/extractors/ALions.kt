package com.amiwatch.parsers.anime.extractors

import com.amiwatch.client
import com.amiwatch.others.JsUnpacker
import com.amiwatch.parsers.Video
import com.amiwatch.parsers.VideoContainer
import com.amiwatch.parsers.VideoExtractor
import com.amiwatch.parsers.VideoServer
import com.amiwatch.parsers.VideoType

class ALions(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val player = client.get(server.embed.url).document.html()
        val script = Regex("<script type=\"text/javascript\">(eval.+)\n</script>").find(player)!!.groups[1]!!.value // I could do this with nice-http html parser, but that's too much effort and regex only needs 3ms

        val url = Regex("file:\"([^\"]+)\"\\}").find(JsUnpacker(script).unpack()!!)!!.groups[1]!!.value
        return VideoContainer(listOf(Video(null, VideoType.M3U8, url)))}
}
