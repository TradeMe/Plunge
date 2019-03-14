package nz.co.trademe.plunge.sample

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import nz.co.trademe.plunge.DeepLinkHandler

class MainActivity : AppCompatActivity(), MainRouter {

    private val linkHandler = DeepLinkHandler.withSchemeHandlers(
        NonCoSchemeHandler(this),
        ClassicSchemeHandler(this)
    )

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

    private fun onNoMatchFound() {
        resultsTextView.text = getString(R.string.no_matches_found)
    }

    override fun onBrowseMatch() {
        resultsTextView.text = getString(R.string.route_to_browse)
    }

    override fun onViewMatch(id: String?) {
        resultsTextView.text = getString(R.string.route_to_view)
    }

    override fun onLoginMatch() {
        resultsTextView.text = getString(R.string.route_to_login)
    }

    override fun onIdMatch(id: String?) {
        resultsTextView.text = getString(R.string.route_to_id)
    }

}

interface MainRouter {

    fun onBrowseMatch()
    fun onViewMatch(id: String?)
    fun onLoginMatch()
    fun onIdMatch(id: String?)

}