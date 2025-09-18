package com.amiwatch.settings.saving.internal

import kotlin.reflect.KClass


data class Pref(
    val prefLocation: Location,
    val type: KClass<*>,
    val default: Any
)

enum class Location(val location: String, val exportable: Boolean) {
    General("com.amiwatch.general", true),
    UI("com.amiwatch.ui", true),
    Player("com.amiwatch.player", true),
    Reader("com.amiwatch.reader", true),
    NovelReader("com.amiwatch.novelReader", true),
    Irrelevant("com.amiwatch.irrelevant", false),
    AnimeDownloads("animeDownloads", false),  //different for legacy reasons
    Protected("com.amiwatch.protected", true),
}
