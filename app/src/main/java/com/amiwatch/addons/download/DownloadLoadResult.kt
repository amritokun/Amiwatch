package com.amiwatch.addons.download

import com.amiwatch.addons.LoadResult

open class DownloadLoadResult : LoadResult() {
    class Success(val extension: DownloadAddon.Installed) : DownloadLoadResult()
}
