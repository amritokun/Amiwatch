package com.amiwatch.media.manga

import com.amiwatch.media.Author
import java.io.Serializable

data class Manga(
    var totalChapters: Int? = null,
    var selectedChapter: MangaChapter? = null,
    var chapters: MutableMap<String, MangaChapter>? = null,
    var slug: String? = null,
    var author: Author? = null,
) : Serializable
