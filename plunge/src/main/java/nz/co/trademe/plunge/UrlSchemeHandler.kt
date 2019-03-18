package nz.co.trademe.plunge

/**
 * Base class for Url handlers. Implementations must expose which hosts they handle through
 * the [hostMatches] function, and then also provide urlMatcher implementations through the [matchers]
 * value. Note, this value should be implemented using by [patterns].
 */
abstract class UrlSchemeHandler {

    /**
     * Called to assess whether this UrlSchemeHandler supports the given host
     */
    abstract fun hostMatches(host: String): Boolean

    /**
     * List containing all matchers defined by this scheme handler
     */
    abstract val matchers: List<UrlMatcher>

    /**
     * Function used for defining matchers via a DSL
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun patterns(block: MatcherRegistry.() -> Unit): Lazy<List<UrlMatcher>> = lazy {
        MatcherRegistry(arrayListOf()).apply(block).matchers
    }

    /**
     * Protected class for allowing Type-safe DSL behaviour when construction a pattern list.
     */
    protected data class MatcherRegistry(
            var matchers: MutableList<UrlMatcher>
    ) {

        /**
         * Constructs a [UrlMatcher] based on provided [urlPattern] and [requiredQueryParams].
         * If a match is found when processing an incoming URI, the [acceptedPatternHandler] will be
         * invoked with a map containing the values of keys defined in the [urlPattern], as well as
         * the key-value pairs of query string parameters.
         */
        fun pattern(urlPattern: String, requiredQueryParams: List<String> = emptyList(), acceptedPatternHandler: (UrlMatchResult) -> Unit) =
                urlMatcher(PathPattern(urlPattern), requiredQueryParams, acceptedPatternHandler).also { matchers.add(it) }
    }
}