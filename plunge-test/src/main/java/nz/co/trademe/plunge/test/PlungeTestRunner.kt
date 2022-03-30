package nz.co.trademe.plunge.test

import android.net.Uri
import nz.co.trademe.plunge.DeepLinkHandler
import nz.co.trademe.plunge.parsing.PlungeParser
import nz.co.trademe.plunge.parsing.models.PlungeTestCase
import org.junit.Assert.*
import java.io.File

object PlungeTestRunner {

    @JvmStatic
    fun testCases(pathToTests: String): Collection<Array<PlungeTestCase>> =
        PlungeParser.readTestCases(File(pathToTests)).map { arrayOf(it) }

    @JvmStatic
    fun assertPlungeTest(
        case: PlungeTestCase,
        handler: DeepLinkHandler
    ) {
        println("URL: ${case.url}")
        println("Handled?: ${case.handled}")

        when (case.handled) {
            true -> assertLinkHandled(case, handler)
            false -> assertLinkNotHandled(case, handler)
        }
    }

    private fun assertLinkHandled(
        case: PlungeTestCase,
        handler: DeepLinkHandler
    ) {
        val uri = Uri.parse(case.url)
        assertTrue("Handler did not handle link ${case.url}", handler.processUri(uri))

        val schemeHandler = handler.findSchemeHandler(uri)
        assertNotNull("No scheme handler found", schemeHandler)

        val matcher = schemeHandler!!.matchers.first { it.performMatch(uri) != null }
        println("Matcher: $matcher")

        val params = matcher.performMatch(uri)?.params ?: emptyMap()
        val caseParams = case.params ?: emptyList()

        assertEquals(
            "Wrong parameters extracted",
            caseParams.map { it.name to it.value }.toMap(),
            params
        )

        caseParams.forEach {
            assertEquals("Parameter '${it.name}' was extracted incorrectly", it.value, params[it.name])
        }
    }

    private fun assertLinkNotHandled(
        case: PlungeTestCase,
        handler: DeepLinkHandler
    ) {
        val uri = Uri.parse(case.url)

        assertFalse(handler.processUri(uri))

        val schemeHandler = handler.findSchemeHandler(uri)
        if (schemeHandler != null) {
            val matcher = schemeHandler.matchers.firstOrNull { it.performMatch(uri) != null }
            assertNull("Found a matcher to handle an unhandled link: $matcher", matcher)
        }

    }

}