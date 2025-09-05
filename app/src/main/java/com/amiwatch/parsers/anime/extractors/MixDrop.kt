package com.amiwatch.parsers.anime.extractors

import com.amiwatch.FileUrl
import com.amiwatch.client
import com.amiwatch.getSize
import com.amiwatch.others.JsUnpacker
import com.amiwatch.parsers.*

//https://github.com/recloudstream/cloudstream/blob/master/app/src/main/java/com/lagradost/cloudstream3/extractors/MixDrop.kt

private val packedRegex = Regex("""eval\(function\(p,a,c,k,e,.*\)\)""")
private fun getPacked(string: String): String? {
    return packedRegex.find(string)?.value
}

private fun getAndUnpack(string: String): String {
    val packedText = getPacked(string)
    return JsUnpacker(packedText).unpack() ?: string
}

private fun httpsify(url: String?): String? {
    return if (url?.startsWith("//") == true) "https:$url" else url
}

class MixDrop(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val page = client.get(server.embed.url)
        val unpacked = getAndUnpack(page.text)
        val link = httpsify(Regex("""wurl.*?=.*?"(.*?)";""").find(unpacked)?.groupValues?.last())

        link?.let {
            return VideoContainer(
                listOf(
                    Video(
                        null,
                        VideoType.CONTAINER,
                        FileUrl(link, mapOf("Referer" to server.embed.url)),
                        getSize(link)
                    )
                )
            )
        }
        return VideoContainer(listOf())

    }

}
