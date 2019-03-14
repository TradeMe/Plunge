package nz.co.trademe.plunge.sample

import nz.co.trademe.plunge.DeepLinkHandler
import nz.co.trademe.plunge.test.model.PlungeTestCase
import nz.co.trademe.plunge.test.runner.PlungeTestRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(manifest= Config.NONE)
class SamplePlungeTests {

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
            ClassicSchemeHandler(Mockito.mock(MainRouter::class.java)),
            NonCoSchemeHandler(Mockito.mock(MainRouter::class.java))
        )
    )

}