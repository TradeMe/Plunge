package nz.co.trademe.plunge

import android.net.Uri
import org.amshove.kluent.`should be equal to`
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DeepLinkHandlerTest {

    @Test
    fun `DeepLinkHandler handled host should match correctly`() {
        val handler = object : UrlSchemeHandler() {
            override fun hostMatches(host: String): Boolean = host == "www.test.com"

            override val matchers by patterns {
                pattern("/listing") { }
            }

        }

        val deepLinker = DeepLinkHandler.withSchemeHandlers(handler)

        deepLinker.processUri(Uri.parse("https://www.test.com/listing")) `should be equal to` true
    }

    @Test
    fun `DeepLinkHandler reject unhandled URL`() {
        val handler = object : UrlSchemeHandler() {
            override fun hostMatches(host: String): Boolean = host == "www.test.com"

            override val matchers by patterns {
                pattern("/listing") { }
            }
        }

        val deepLinker = DeepLinkHandler.withSchemeHandlers(handler)

        deepLinker.processUri(Uri.parse("https://www.test.com/something")) `should be equal to` false
    }
}