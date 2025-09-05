package com.amiwatch.parsers

import com.amiwatch.Lazier
import com.amiwatch.lazyList
import com.amiwatch.parsers.manga.*

object MangaSources : MangaReadSources() {
    override val list: List<Lazier<BaseParser>> = lazyList(
        "MangaKakalot" to ::MangaKakalot,
        "MangaBuddy" to ::MangaBuddy,
        "MangaPill" to ::MangaPill,
        "MangaDex" to ::MangaDex,
        "MangaReaderTo" to ::MangaReaderTo,
        "AllAnime" to ::AllAnime,
        "Toonily" to ::Toonily,
//        "MangaHub" to ::MangaHub,
        "MangaKatana" to ::MangaKatana,
        "MangaKomi" to ::MangaKomi,
        "Manga4Life" to ::Manga4Life,
        "MangaRead" to ::MangaRead,
        "ComickFun" to ::ComickFun,
        "ColoredManga" to ::ColoredManga,
    )
}

object HMangaSources : MangaReadSources() {
    val aList: List<Lazier<BaseParser>> = lazyList(
        "NineHentai" to ::NineHentai,
        "Manhwa18" to ::Manhwa18,
        "NHentai" to ::NHentai,
    )
    override val list = listOf(aList,MangaSources.list).flatten()
}
