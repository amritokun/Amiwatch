package ani.saikou.manga

import ani.saikou.parsers.MangaChapter
import ani.saikou.parsers.MangaImage
import java.io.Serializable
import kotlin.math.ceil

data class MangaChapter(
    val number: String,
    var link: String,
    var title: String? = null,
    var description: String? = null,
    var images: List<MangaImage>? = null
) : Serializable {
    constructor(chapter: MangaChapter) : this(chapter.number, chapter.link, chapter.title, chapter.description)

    private var dualPage: List<Pair<MangaImage?, MangaImage?>>? = null
    fun dualPages(): List<Pair<MangaImage?, MangaImage?>> {
        dualPage = dualPage ?: (0..ceil((images!!.size.toFloat() - 1f) / 2).toInt()).map {
            val i = it * 2
            (images?.getOrNull(i) to images?.getOrNull(i + 1))
        }
        return dualPage!!
    }
}
