package com.amiwatch.parsers

import com.amiwatch.Lazier
import com.amiwatch.parsers.novel.DynamicNovelParser
import com.amiwatch.parsers.novel.NovelExtension
import com.amiwatch.settings.saving.PrefManager
import com.amiwatch.settings.saving.PrefName
import com.amiwatch.util.Logger
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

object NovelSources : NovelReadSources() {
    override var list: List<Lazier<BaseParser>> = emptyList()
    var pinnedNovelSources: List<String> = emptyList()

    suspend fun init(fromExtensions: StateFlow<List<NovelExtension.Installed>>) {
        pinnedNovelSources =
            PrefManager.getNullableVal<List<String>>(PrefName.NovelSourcesOrder, null)
                ?: emptyList()

        // Initialize with the first value from StateFlow
        val initialExtensions = fromExtensions.first()
        list = createParsersFromExtensions(initialExtensions) + Lazier(
            { OfflineNovelParser() },
            "Downloaded"
        )

        // Update as StateFlow emits new values
        fromExtensions.collect { extensions ->
            list = sortPinnedNovelSources(
                createParsersFromExtensions(extensions),
                pinnedNovelSources
            ) + Lazier(
                { OfflineNovelParser() },
                "Downloaded"
            )
        }
    }

    fun performReorderNovelSources() {
        //remove the downloaded source from the list to avoid duplicates
        list = list.filter { it.name != "Downloaded" }
        list = sortPinnedNovelSources(list, pinnedNovelSources) + Lazier(
            { OfflineNovelParser() },
            "Downloaded"
        )
    }

    private fun createParsersFromExtensions(extensions: List<NovelExtension.Installed>): List<Lazier<BaseParser>> {
        Logger.log("createParsersFromExtensions")
        Logger.log(extensions.toString())
        return extensions.map { extension ->
            val name = extension.name
            Lazier({ DynamicNovelParser(extension) }, name)
        }
    }

    private fun sortPinnedNovelSources(
        parsers: List<Lazier<BaseParser>>,
        pinnedSources: List<String>
    ): List<Lazier<BaseParser>> {
        val pinnedSourcesMap = parsers.filter { pinnedSources.contains(it.name) }
            .associateBy { it.name }
        val orderedPinnedSources = pinnedSources.mapNotNull { name ->
            pinnedSourcesMap[name]
        }
        val unpinnedSources = parsers.filterNot { pinnedSources.contains(it.name) }
        return orderedPinnedSources + unpinnedSources
    }
}
