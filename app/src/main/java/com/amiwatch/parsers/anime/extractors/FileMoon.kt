package com.amiwatch.parsers.anime.extractors

import com.amiwatch.FileUrl
import com.amiwatch.client
import com.amiwatch.getSize
import com.amiwatch.others.JsUnpacker
import com.amiwatch.parsers.*

private val packedRegex = Regex("""eval\(function\(p,a,c,k,e,.*\)\)""")
private fun getPacked(string: String): String? {
    return packedRegex.find(string)?.value
}

private fun getAndUnpack(string: String): String {
    val packedText = getPacked(string)
    return JsUnpacker(packedText).unpack() ?: string
}
class FileMoon(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val page = client.get(server.embed.url)
        val unpacked = getAndUnpack(page.text)
        val link = Regex("file:\"(.+?)\"").find(unpacked)?.groupValues?.last()

        link?.let {
            return VideoContainer(
                listOf(
                    Video(
                        null,
                        VideoType.M3U8,
                        FileUrl(link, mapOf("Referer" to server.embed.url)),
                        getSize(link)
                    )
                )
            )
        }
        return VideoContainer(listOf())

    }

}
