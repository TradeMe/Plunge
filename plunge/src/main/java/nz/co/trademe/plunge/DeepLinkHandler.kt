package nz.co.trademe.plunge

import android.net.Uri

/**
 * Main class handling deep link interactions.
 */
class DeepLinkHandler private constructor(
    private val schemeHandlers: Array<out UrlSchemeHandler>
) {

    /**
     * Find the scheme handler that matches the host name
     */
    fun findSchemeHandler(uri: Uri): UrlSchemeHandler? =
        schemeHandlers.find { uri.host?.let(it::hostMatches) ?: false }

    /**
     * Function for processing a given uri. If a match is found in any of the appropriate matchers,
     * this will return true and invoke the accepted link handler on that urlMatcher (defined in the [UrlSchemeHandler]).
     * If no match is found, false is returned.
     */
    fun processUri(uri: Uri): Boolean {
        schemeHandlers
            // Find the scheme handler that matches the host name
            .find { uri.host?.let(it::hostMatches) ?: false }
            // Check if any of their matchers match
            ?.matchers
            // Invoke the matchers, returning true if a match is found
            ?.forEach {
                val result = it.performMatch(uri)

                if (result != null) {
                    it.onMatch(result)
                    return true
                }
            }

        // Nothing matched, return false.
        return false
    }

    companion object {

        /**
         * Function for instantiating a [DeepLinkHandler]
         */
        fun withSchemeHandlers(
            schemeHandler: UrlSchemeHandler,
            vararg schemeHandlers: UrlSchemeHandler
        ): DeepLinkHandler =
            DeepLinkHandler(arrayOf(schemeHandler, *schemeHandlers))
    }
}