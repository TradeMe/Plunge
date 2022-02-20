package nz.co.trademe.plunge

import android.net.Uri

/**
 * Class used for extending the results provided to the match handling functions
 */
data class UrlMatchResult(
    val url: Uri,
    val params: Map<String?, String?>
)