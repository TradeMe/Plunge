package nz.co.trademe.plunge.sample

import nz.co.trademe.plunge.DeepLinkHandler
import nz.co.trademe.plunge.test.model.PlungeTestCase
import nz.co.trademe.plunge.test.runner.PlungeTestRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class DeepLinkTests {

    companion object {

        @JvmStatic
        private val pathToTests = System.getProperty("user.dir") + "/src/test/test-cases"

        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters() = PlungeTestRunner.testCases(pathToTests)

    }

    @ParameterizedRobolectricTestRunner.Parameter(0)
    lateinit var plungeTestCase: PlungeTestCase

    @Test
    fun test() = PlungeTestRunner.assertPlungeTest(
        plungeTestCase,
        DeepLinkHandler.withSchemeHandlers(
            ClassicSchemeHandler(PlungeTestRunner::captureMatches),
            NonCoSchemeHandler(PlungeTestRunner::captureMatches)
        )
    )

}