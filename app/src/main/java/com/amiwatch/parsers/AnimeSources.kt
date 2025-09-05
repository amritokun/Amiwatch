package com.amiwatch.parsers

import com.amiwatch.Lazier
import com.amiwatch.lazyList
import com.amiwatch.parsers.anime.AllAnime
import com.amiwatch.parsers.anime.AnimeDao
import com.amiwatch.parsers.anime.AnimePahe
import com.amiwatch.parsers.anime.Gogo
import com.amiwatch.parsers.anime.Haho
import com.amiwatch.parsers.anime.HentaiFF
import com.amiwatch.parsers.anime.HentaiMama
import com.amiwatch.parsers.anime.HentaiStream
import com.amiwatch.parsers.anime.Marin
import com.amiwatch.parsers.anime.AniWave
import com.amiwatch.parsers.anime.Kaido

object AnimeSources : WatchSources() {
    override val list: List<Lazier<BaseParser>> = lazyList(
        "AllAnime" to ::AllAnime,
        "Gogo" to ::Gogo,
        "Kaido" to ::Kaido,
        "Marin" to ::Marin,
        "AnimePahe" to ::AnimePahe,
        "AniWave" to ::AniWave,
        "AnimeDao" to ::AnimeDao,
    )
}

object HAnimeSources : WatchSources() {
    private val aList: List<Lazier<BaseParser>>  = lazyList(
        "HentaiMama" to ::HentaiMama,
        "Haho" to ::Haho,
        "HentaiStream" to ::HentaiStream,
        "HentaiFF" to ::HentaiFF,
    )

    override val list = listOf(aList,AnimeSources.list).flatten()
}
