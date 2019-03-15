package nz.co.trademe.plunge.sample

import nz.co.trademe.plunge.UrlSchemeHandler

class NonCoSchemeHandler(val router: MainRouter) : UrlSchemeHandler() {

    override fun hostMatches(host: String): Boolean = host.contains("test.nz")

    override val matchers by patterns {
        pattern("/browse/something") { router.onBrowseMatch() }
        pattern("/something/{_}/view/{d|id}") { router.onViewMatch(it["id"]) }
    }
}