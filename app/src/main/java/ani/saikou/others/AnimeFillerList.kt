package ani.saikou.others

import ani.saikou.anime.Episode
import ani.saikou.client
import ani.saikou.tryWithSuspend
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object AnimeFillerList {
    suspend fun getFillers(malId: Int): Map<String, Episode>? {
        return tryWithSuspend {
            val json = client.get("https://raw.githubusercontent.com/saikou-app/mal-id-filler-list/main/fillers/$malId.json")
            return@tryWithSuspend if (json.text != "404: Not Found") json.parsed<AnimeFillerListValue>().episodes?.associate {
                val num = it.number.toString()
                num to Episode(
                    num,
                    it.title,
                    filler = it.fillerBool == true
                )
            } else null
        }
    }

    @Serializable
    data class AnimeFillerListValue (
        @SerialName("MAL-id")
        val malID: Int? = null,

        @SerialName("Anilist-id")
        val anilistID: Int? = null,

        val episodes: List<AFLEpisode>? = null
    )

    @Serializable
    data class AFLEpisode (
        val number: Int? = null,
        val title: String? = null,
        val desc: String? = null,
        val filler: String? = null,
        @SerialName("filler-bool") val fillerBool : Boolean?=null,
        val airDate: String? = null
    )
}


