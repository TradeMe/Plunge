package nz.co.trademe.plunge.sample

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_main.*
import nz.co.trademe.plunge.DeepLinkHandler
import nz.co.trademe.plunge.UrlSchemeHandler

class MainActivity : AppCompatActivity() {

    private val linkHandler = DeepLinkHandler.withSchemeHandlers(NonCoSchemeHandler(), ClassicSchemeHandler())

    private val allPatterns = listOf(
        "test.nz/browse/something",
        "test.nz/something/{_}/view/{d|id}",
        "www.test.co.nz/login?token={token}",
        "www.test.co.nz/{_}/{d|id}-id.htm"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        testLinkButton.setOnClickListener {
            val uri = try {
                Uri.parse(textInputLayout.editText?.text.toString())
            } catch (e: Exception) {
                resultsTextView.text = getString(R.string.invalid_url)
                return@setOnClickListener
            }

           processUri(uri)
        }

        patternsTextView.text = allPatterns.joinToString("\n")

        // Handle URI from deep link
        intent.data?.let(::processUri)
    }

    private fun processUri(uri: Uri) {
        if (!linkHandler.processUri(uri)) {
            onNoMatchFound()
        }
    }

    fun onMatchFound(extractions: Map<String, String> = emptyMap()) {
        resultsTextView.text = "Matched! Extracted: $extractions"
    }

    private fun onNoMatchFound() {
        resultsTextView.text = getString(R.string.no_matches_found)
    }

    /**
     * Example FrEnd Scheme handler
     */
    inner class NonCoSchemeHandler : UrlSchemeHandler() {
        override fun hostMatches(host: String): Boolean = host.contains("test.nz")

        override val matchers by patterns {
            pattern("/browse/something") { onMatchFound(it) }
            pattern("/something/{_}/view/{d|id}") { onMatchFound(it) }
        }
    }

    /**
     * Example Classic Scheme handler
     */
    inner class ClassicSchemeHandler : UrlSchemeHandler() {
        override fun hostMatches(host: String): Boolean = host.contains("test.co.nz")

        override val matchers by patterns {
            pattern("/login", requiredQueryParams = listOf("token")) { onMatchFound(it) }
            pattern("/{_}/{d|id}-id.htm") { onMatchFound(it) }
        }
    }
}