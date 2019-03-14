package nz.co.trademe.plunge.plugin.tasks

import kotlinx.coroutines.runBlocking
import nz.co.trademe.plunge.parsing.PlungeParser
import nz.co.trademe.plunge.parsing.models.PlungeTestCase
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

private const val GET_STATE_COMMAND = "adb get-state"
private const val TEST_URL_COMMAND_PREFIX =
    "adb shell cmd package query-activities --components -a android.intent.action.VIEW -d "

/**
 * Gradle task for testing deep links by utilising adb features
 */
open class TestDeepLinks : DefaultTask() {

    @get:Input
    val testCaseDirectory: Property<File> = project.objects.property(File::class.java)

    @get:Input
    var packageName: String? = null

    @TaskAction
    fun runTests() {
        val directory = testCaseDirectory.get()

        println()
        log("Running tests for $packageName with test cases from [${directory.absolutePath}]")
        log("")

        runBlocking {
            // Check only one device is connected
            try {
                runCommand(GET_STATE_COMMAND)
            } catch (e: CommandFailedException) {
                fail("Too many devices attached. Ensure only one is connected.")
            }

            // Check all test cases
            val testCases = PlungeParser.readTestCases(directory)
            val failedTests = testCases
                .filterNot { runTestCase(it, packageName!!) }

            log("")

            if (failedTests.isEmpty()) {
                log("${testCases.size} tests run, all passed \uD83C\uDF89")
            } else {
                log("${testCases.size} tests run, the following ${failedTests.size} failed \uD83D\uDCA5")

                failedTests.forEach {
                    log(it.url)
                }

                throw GradleException("Some Plunge tests failed, check log for results")
            }
        }

    }

    private suspend fun runTestCase(case: PlungeTestCase, packageName: String): Boolean {
        val urlHandled = runCommand(TEST_URL_COMMAND_PREFIX + case.url).contains(packageName)

        // Report results
        val testPassed = urlHandled == case.handled

        if (testPassed) {
            log("Passed: $case")
        } else {
            log("Failed: $case")
        }

        return testPassed
    }
}