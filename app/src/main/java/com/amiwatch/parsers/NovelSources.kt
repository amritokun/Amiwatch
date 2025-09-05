package com.amiwatch.parsers

import com.amiwatch.Lazier
import com.amiwatch.lazyList
import com.amiwatch.parsers.novel.AnnaArchive

object NovelSources : NovelReadSources() {
    override val list: List<Lazier<BaseParser>> = lazyList(
        "Anna's Archive" to ::AnnaArchive,
    )
}
