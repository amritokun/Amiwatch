package com.amiwatch.addons.torrent

import com.amiwatch.addons.LoadResult

open class TorrentLoadResult : LoadResult() {
    class Success(val extension: TorrentAddon.Installed) : TorrentLoadResult()
}
