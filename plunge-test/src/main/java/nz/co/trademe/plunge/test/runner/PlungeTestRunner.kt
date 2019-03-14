package nz.co.trademe.plunge.test.runner

import android.net.Uri
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import kotlinx.serialization.json.Json
import nz.co.trademe.plunge.DeepLinkHandler
import nz.co.trademe.plunge.test.model.PlungeTestCase
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.io.File

object PlungeTestRunner {

    @Mock
    lateinit var mockMatchHandler: MockMatchHandler

    @JvmStatic
    fun testCases(pathToTests: String): Collection<Array<PlungeTestCase>> =
        File(pathToTests).listFiles { f ->
            f.extension == "json"
        }.map {
            arrayOf(Json.parse(PlungeTestCase.serializer(), it.readText()))
        }

    @JvmStatic
    fun assertPlungeTest(
        case: PlungeTestCase,
        handler: DeepLinkHandler
    ) {
        MockitoAnnotations.initMocks(this)
        when (case.handled) {
            true -> assertLinkHandled(case, handler)
            false -> assertLinkNotHandled(case, handler)
        }
    }

    @JvmStatic
    fun captureMatches(matches: Map<String, String>) = mockMatchHandler.match(matches)


    private fun assertLinkHandled(
        case: PlungeTestCase,
        handler: DeepLinkHandler
    ) {
        val uri = Uri.parse(case.url)
        assertTrue("Handler did not handle link ${case.url}", handler.processUri(uri))

        val paramsMap = case.params.map { it.name to it.value }.toMap()
        verify(mockMatchHandler).match(paramsMap) // TODO verify that the correct map is passed in

//        val uri = Uri.parse(case.url)
//        val matches = handler.matchers.filter { it.performMatch(uri) != null }.size
//        assertEquals("Expecting exactly 1 match", 1, matches)
//
//        val params = handler.matchers.first { it.performMatch(uri) != null }.performMatch(uri) ?: emptyMap()
//        assertEquals(
//            "Wrong number of parameters were extracted",
//            case.params.size,
//            params.size
//        )
//
//        case.params.forEach {
//            assertEquals("Parameter '${it.name}' was extracted incorrectly", it.value, params[it.name])
//        }




        // TODO improve the assertions so that they are more clear when tests fail for various reasons
    }

    private fun assertLinkNotHandled(
        case: PlungeTestCase,
        handler: DeepLinkHandler
    ) {

        assertFalse(handler.processUri(Uri.parse(case.url)))
        verify(mockMatchHandler, never()).match(any())

//        val matches = handler.matchers.filter { it.performMatch(Uri.parse(case.url)) != null }.size
//        assertEquals("Expecting no matches", 0, matches)
    }

}