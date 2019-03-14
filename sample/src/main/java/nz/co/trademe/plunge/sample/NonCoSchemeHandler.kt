package nz.co.trademe.plunge.sample

import nz.co.trademe.plunge.UrlSchemeHandler

/**
 * Example FrEnd Scheme handler
 */
class NonCoSchemeHandler(val onMatchFound: (Map<String, String>) -> Unit = {}) : UrlSchemeHandler() {


    override fun hostMatches(host: String): Boolean = host.contains("test.nz")

    override val matchers by patterns {
        pattern("/browse/something") { onMatchFound(it) }
        pattern("/something/{_}/view/{d|id}") { onMatchFound(it) }
    }
}