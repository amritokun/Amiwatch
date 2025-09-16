package ani.saikou.parsers.anime.extractors

import ani.saikou.client
import ani.saikou.findBetween
import ani.saikou.parsers.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class StreamSB(override val server: VideoServer) : VideoExtractor() {
    override suspend fun extract(): VideoContainer {
        val videos = mutableListOf<Video>()
        val id = server.embed.url.let { it.findBetween("/e/", ".html") ?: it.split("/e/")[1] }
        val jsonLink =
            "https://streamsss.net/sources48/${bytesToHex("||$id||||streamsb".toByteArray())}/"
        val json = client.get(jsonLink, mapOf("watchsb" to "sbstream")).parsed<Response>()
        if (json.statusCode == 200) {
            videos.add(Video(null, VideoType.M3U8, json.streamData!!.file))
        }
        return VideoContainer(videos)
    }

    private val hexArray = "0123456789ABCDEF".toCharArray()

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF

            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    @Serializable
    private data class Response(
        @SerialName("stream_data")
        val streamData: StreamData? = null,
        @SerialName("status_code")
        val statusCode: Int? = null
    )

    @Serializable
    private data class StreamData(
        @SerialName("file") val file: String
    )
}