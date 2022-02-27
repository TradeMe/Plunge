package nz.co.trademe.plunge

import android.net.Uri
import android.util.Log

/**
 * Interface defining a [UrlMatcher]
 */
interface UrlMatcher {

    /**
     * Function for performing a match against an input URI, returning
     * the map of matched names
     */
    fun performMatch(uri: Uri): UrlMatchResult?

    /**
     * Function invoked when results are returned from performMatch
     */
    fun onMatch(matchResult: UrlMatchResult)
}


/**
 * Function for building and returning a [UrlMatcher] function
 */
internal fun urlMatcher(pattern: PathPattern, requiredQueryParams: List<String>, acceptedHandler: (UrlMatchResult) -> Unit): UrlMatcher {
    // If the pattern given is not valid, throw immediately.
    if (!pattern.isValid(requiredQueryParams)) {
        throw IllegalArgumentException(
                "Invalid pattern: ${pattern.pattern}. Check that capturing groups are closed properly and no groups are named the same as expected query string parameters"
        )
    }

    // Construct the [UrlMatcher] and return it
    return Matcher(pattern, requiredQueryParams, acceptedHandler)
}


/**
 * A [UrlMatcher] is a function which takes a [Uri] and returns whether or not the URI matches
 * patterns defined within the function itself. A further parameter of [invokeHandler] has been
 * included for testing such that the urlMatcher can be run without having to mock the view invocation.
 */
internal class Matcher(
    private val pattern: PathPattern,
    private val requiredQueryParams: List<String>,
    private val acceptedHandler: (UrlMatchResult) -> Unit
): UrlMatcher {

    override fun performMatch(uri: Uri): UrlMatchResult? {
        val path = uri.path ?: return null

        val patternRegex = pattern.compileToRegex()
        val allGroups = pattern.extractPathGroups()

        // If the regex isn't a match, return false
        if (!patternRegex.matches(path)) return null

        // Check to be sure we have our required query params
        val queryNames = uri.queryParameterNames.map { it.lowercase() }
        val requiredNames = requiredQueryParams.map { it.lowercase() }

        if (!queryNames.containsAll(requiredNames)) return null

        // Extract names groups
        val capturingGroups = allGroups.filterNot { it.name == "_" }
        val groups = patternRegex.matchEntire(path)
                ?.groups
                ?.toList()
                ?.let { it.subList(1, it.size) }
                ?: emptyList()

        // If we expect more groups than we found, return false
        if (capturingGroups.size > groups.size) return null

        // Match up groups to captured groups and hope the ordering is the same
        val urlPartExtractions: Map<String, String> = capturingGroups
                .withIndex()
                .associateWith { (index, _) -> groups[index] }
                .filter { it.value != null }
                .map { it.key.value.name to it.value!!.value }
                .toMap()

        // Extract all query parameters into a map, excluding those which are defined as a group name
        val queryExtractions: Map<String, String> = uri.queryParameterNames
                .associateWith { uri.getQueryParameter(it) }
                .filter { it.value != null }
                .map { it.key to it.value!! }
                .toMap()
                .filterNot { entry ->
                    capturingGroups.any { group -> group.name.equals(entry.key, ignoreCase = true) }.also { conflictingName ->
                        if (conflictingName) {
                            Log.w("UrlMatcher", "Query string of name \"${entry.key}\" conflicts with name of group. Dropping in favour of group name.")
                        }
                    }
                }

        return UrlMatchResult(url = uri, params = urlPartExtractions + queryExtractions)
    }

    override fun onMatch(matchResult: UrlMatchResult) {
        acceptedHandler(matchResult)
    }

    override fun toString(): String = pattern.toString()

}
