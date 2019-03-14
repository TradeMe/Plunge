package nz.co.trademe.plunge.sample

import nz.co.trademe.plunge.UrlSchemeHandler

/**
 * Example Classic Scheme handler
 */
class ClassicSchemeHandler(val onMatchFound: ((Map<String, String>) -> Unit) = {}) : UrlSchemeHandler() {
    override fun hostMatches(host: String): Boolean = host.contains("test.co.nz")

    override val matchers by patterns {
        pattern("/login", requiredQueryParams = listOf("token")) { onMatchFound(it) }
        pattern("/{_}/{d|id}-id.htm") { onMatchFound(it) }
    }
}