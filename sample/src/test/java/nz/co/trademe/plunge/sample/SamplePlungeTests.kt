package nz.co.trademe.plunge.sample

import nz.co.trademe.plunge.DeepLinkHandler
import nz.co.trademe.plunge.parsing.models.PlungeTestCase
import nz.co.trademe.plunge.test.PlungeTestRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * This is a sample test file. In order to create unit tests with Plunge,
 * create a file similar to this one in your test source set.
 */
@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SamplePlungeTests {

    companion object {

        /**
         * Specify the path to your test cases. In this case, we put it in
         * the test source set (i.e. /sample/src/test/test-cases/).
         */
        @JvmStatic
        private val pathToTests = "/src/test/test-cases"

        /**
         * Declare a static function which returns a collection of Object arrays.
         * Annotate it with the below Parameters annotation, and return
         * [PlungeTestRunner.testCases] with the path to your tests as its sole
         * argument.
         */
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun parameters() = PlungeTestRunner.testCases(pathToTests)

    }

    // This is the handler under test
    val yourHandler =
        DeepLinkHandler.withSchemeHandlers(
            ClassicSchemeHandler(Mockito.mock(MainRouter::class.java)),
            NonCoSchemeHandler(Mockito.mock(MainRouter::class.java))
        )

    /**
     * Declare a field of type [PlungeTestCase] and annotate it with the respective
     * Parameter annotation. This will be used as a reference to the test case
     * contained in each JSON file.
     */
    @ParameterizedRobolectricTestRunner.Parameter(0)
    lateinit var testCase: PlungeTestCase


    /**
     * Finally, declare a single @[Test]-annotated function which calls
     * [PlungeTestRunner.assertPlungeTest] with the test case and your
     * [DeepLinkHandler] implementation.
     */
    @Test
    fun runTest() =
        PlungeTestRunner.assertPlungeTest(
            testCase,
            yourHandler
        )

}