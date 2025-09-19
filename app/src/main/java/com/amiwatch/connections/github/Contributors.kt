package com.amiwatch.connections.github

import com.amiwatch.Mapper
import com.amiwatch.R
import com.amiwatch.client
import com.amiwatch.getAppString
import com.amiwatch.settings.Developer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement

class Contributors {

    fun getContributors(): Array<Developer> {
        var developers = arrayOf<Developer>()
        runBlocking(Dispatchers.IO) {
            val repo = getAppString(R.string.repo)
            val res = client.get("https://api.github.com/repos/$repo/contributors")
                .parsed<JsonArray>().map {
                    Mapper.json.decodeFromJsonElement<GithubResponse>(it)
                }
            res.forEach {
                if (it.login == "SunglassJerry") return@forEach
                val role = when (it.login) {
                    "amritokun" -> "Owner & Maintainer"
                    "SyntaxSpin" -> "Contributor & Helper"
                    else -> "Contributor"
                }
                developers = developers.plus(
                    Developer(
                        it.login,
                        it.avatarUrl,
                        role,
                        it.htmlUrl
                    )
                )
            }
            developers = developers.plus(
                arrayOf(
                    Developer(
                        "laidix",
                        "https://avatars.githubusercontent.com/u/150521365?v=4",
                        "Readme Banner Designer",
                        "https://github.com/laidix?tab=repositories"
                    ),
                )
            )
        }
        return developers
    }


    @Serializable
    data class GithubResponse(
        @SerialName("login")
        val login: String,
        @SerialName("avatar_url")
        val avatarUrl: String,
        @SerialName("html_url")
        val htmlUrl: String
    )
}
