package nz.co.trademe.plunge.sample

import nz.co.trademe.plunge.UrlSchemeHandler

class ClassicSchemeHandler(val router: MainRouter) : UrlSchemeHandler() {
    override fun hostMatches(host: String): Boolean = host.contains("test.co.nz")

    override val matchers by patterns {
        pattern("/login", requiredQueryParams = listOf("token")) { router.onLoginMatch() }
        pattern("/{_}/{d|id}-id.htm") { router.onIdMatch(it["id"]) }
    }
}