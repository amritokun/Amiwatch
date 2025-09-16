package ani.saikou.parsers.anime.extractors

import android.util.Base64
import ani.saikou.FileUrl
import ani.saikou.Mapper
import ani.saikou.client
import ani.saikou.parsers.*
import ani.saikou.parsers.anime.NineAnime.Companion.cipher
import ani.saikou.parsers.anime.NineAnime.Companion.encrypt
import kotlinx.serialization.Serializable

class VizCloud(override val server: VideoServer) : VideoExtractor() {

    @Serializable
    private data class Sources(val file: String)

    @Serializable
    private data class Media(val sources: List<Sources>)

    @Serializable
    private data class Data(val media: Media)

    @Serializable
    private data class Response(val data: Data)

    private val regex = Regex("(.+?/)e(?:mbed)?/([a-zA-Z0-9]+)")

    override suspend fun extract(): VideoContainer {
        val embed = server.embed
        val group = regex.find(embed.url)?.groupValues!!

        val host = group[1]

        val id = encrypt(cipher(getKey().cipherKey, group[2]), getKey().encryptKey)

        val encrypted = encrypt(
            dashify(encrypt(id, getKey().pre)), getKey().post
        )
        val link = "${host}/${getKey().mainKey}/$encrypted"
        val response = client.get(link, embed.headers)

        if (!response.text.startsWith("{")) throw Exception("Seems like 9Anime kiddies changed stuff again, Go touch some grass for bout an hour Or use a different Server")
        return VideoContainer(response.parsed<Response>().data.media.sources.map {
            val file = FileUrl(it.file, mapOf("referer" to host))
            Video(null, VideoType.M3U8, file)
        })
    }

    private suspend fun encrypt(text: String, steps: List<Char>): String {
        var output: String = text
        for (step in steps) {
            output = when (step) {
                'o'  -> encrypt(output, getKey().encryptKey).replace("/", "_")
                's'  -> s(output)
                'a'  -> output.reversed()
                else -> output
            }
        }
        return output
    }

    private fun s(g: String): String {
        return g.replace("[a-zA-Z]".toRegex()) {
            val a = if (it.value.first().code <= 90) 90 else 122
            val b = it.value.first().code + 13
            (if (a >= b) b else b - 26).toChar().toString()
        }
    }

    companion object {
        private var lastChecked = 0L
        private const val jsonLink = "https://raw.githubusercontent.com/AnimeJeff/Overflow/main/syek"
        private var cipherKey: CipherKey? = null
        suspend fun getKey(): CipherKey {
            cipherKey = if (cipherKey != null && (lastChecked - System.currentTimeMillis()) < 1000 * 60 * 30) cipherKey!!
            else {
                lastChecked = System.currentTimeMillis()
                Mapper.parse<CipherKey>(
                    Base64.decode(
                        Base64.decode(
                            Base64.decode(client.get(jsonLink).text, Base64.NO_WRAP),
                            Base64.NO_WRAP
                        ), Base64.NO_WRAP
                    ).decodeToString()
                )
            }
            return cipherKey!!
        }


        suspend fun dashify(input: String): String {
            val key = getKey()
            val mapped = input.mapIndexedNotNull { i, c ->
                val operation = key.operations[i % key.operations.size]!!.split(" ")
                val operand = operation[1].toInt()
                when (operation.first()) {
                    "*"  -> c.code * operand
                    "+"  -> c.code + operand
                    "-"  -> c.code - operand
                    "<<" -> c.code shl operand
                    "^"  -> c.code xor operand
                    else -> throw Exception("Unknown operator $operation needed to be implemented")
                }
            }.joinToString("-")
            return mapped
        }

        @Serializable
        data class CipherKey(
            val cipherKey: String,
            val mainKey: String,
            val encryptKey: String,
            val pre: List<Char>,
            val post: List<Char>,
            val operations: Map<Int, String>
        )
    }
}
//Ya boi AnimeJeff was here
