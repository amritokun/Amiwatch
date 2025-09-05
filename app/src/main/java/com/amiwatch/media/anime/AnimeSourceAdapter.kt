package com.amiwatch.media.anime

import com.amiwatch.media.MediaDetailsViewModel
import com.amiwatch.media.SourceAdapter
import com.amiwatch.media.SourceSearchDialogFragment
import com.amiwatch.parsers.ShowResponse
import kotlinx.coroutines.CoroutineScope

class AnimeSourceAdapter(
    sources: List<ShowResponse>,
    val model: MediaDetailsViewModel,
    val i: Int,
    val id: Int,
    fragment: SourceSearchDialogFragment,
    scope: CoroutineScope
) : SourceAdapter(sources, fragment, scope) {

    override suspend fun onItemClick(source: ShowResponse) {
        model.overrideEpisodes(i, source, id)
    }
}
