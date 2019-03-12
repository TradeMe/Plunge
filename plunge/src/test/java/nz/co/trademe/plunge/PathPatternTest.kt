package nz.co.trademe.plunge

import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain all`
import org.amshove.kluent.`should contain`
import org.junit.Test

/**
 * Test for extension functions on the [PathPattern] data class
 */
class PathPatternTest {

    // REGION isValid

    @Test
    fun `isValid rejects incomplete groups`() {
        val testPattern = PathPattern("/incomplete/{group")
        testPattern.isValid(emptyList()) `should be equal to` false
    }

    @Test
    fun `isValid rejects duplicate groups`() {
        val testPattern = PathPattern("/incomplete/{group}/{group}")
        testPattern.isValid(emptyList()) `should be equal to` false
    }

    @Test
    fun `isValid rejects duplicate groups - including required query parameters`() {
        val testPattern = PathPattern("/incomplete/{group}")
        testPattern.isValid(requiredQueryParameters = listOf("group")) `should be equal to` false
    }

    @Test
    fun `isValid rejects duplicate groups - including one flag`() {
        val testPattern = PathPattern("/incomplete/{d|group}/{group}")
        testPattern.isValid(emptyList()) `should be equal to` false
    }

    @Test
    fun `isValid rejects duplicate groups - all including flags`() {
        val testPattern = PathPattern("/incomplete/{d|group}/{d|group}")
        testPattern.isValid(emptyList()) `should be equal to` false
    }

    @Test
    fun `isValid accepts complete groups - one group per path`() {
        val testPattern = PathPattern("/complete/{group}")
        testPattern.isValid(emptyList()) `should be equal to` true
    }

    @Test
    fun `isValid accepts complete groups - one group per path in middle of string`() {
        val testPattern = PathPattern("/complete/infront-{group}-after")
        testPattern.isValid(emptyList()) `should be equal to` true
    }

    @Test
    fun `isValid accepts complete groups - multiple groups per path in middle of string`() {
        val testPattern = PathPattern("/complete/infront-{group1}-{group2}-after")
        testPattern.isValid(emptyList()) `should be equal to` true
    }

    // ENDREGION

    // REGION extractPathGroups

    @Test
    fun `extractPathGroups extracts single group correctly`() {
        val testPattern = PathPattern("/complete/{group}")
        testPattern.extractPathGroups() `should contain` PathGroup("group")
    }

    @Test
    fun `extractPathGroups extracts multiple groups correctly`() {
        val testPattern = PathPattern("/complete/{group1}/{group2}")
        testPattern.extractPathGroups().map { it.name } `should contain all` setOf("group1", "group2")
    }

    @Test
    fun `extractPathGroups extracts multiple groups per path correctly`() {
        val testPattern = PathPattern("/complete/{group1}-{group2}")
        testPattern.extractPathGroups().map { it.name } `should contain all` setOf("group1", "group2")
    }

    @Test
    fun `extractPathGroups extracts flags correctly`() {
        val testPattern = PathPattern("/complete/{flag|group}")
        testPattern.extractPathGroups().first().flags `should contain all` setOf('f', 'l', 'a', 'g')
    }

    // ENDREGION

    // REGION compileToRegex

    @Test
    fun `compileToRegex compiles standard group correctly`() {
        val testPattern = PathPattern("/complete/{group}")
        testPattern.compileToRegex().pattern `should be equal to` "/complete/(\\w+)"
    }

    @Test
    fun `compileToRegex compiles flagged group correctly`() {
        val testPattern = PathPattern("/complete/{d|group}")
        testPattern.compileToRegex().pattern `should be equal to` "/complete/(\\d+)"
    }

    @Test
    fun `compileToRegex compiles standard group correctly - including non-capturing groups`() {
        val testPattern = PathPattern("/complete/{_}/{group}")
        testPattern.compileToRegex().pattern `should be equal to` "/complete/(?:\\w+)/(\\w+)"
    }

    @Test
    fun `compileToRegex compiles standard group correctly - including flagged non-capturing groups`() {
        val testPattern = PathPattern("/complete/{d|_}/{group}")
        testPattern.compileToRegex().pattern `should be equal to` "/complete/(?:\\d+)/(\\w+)"
    }

    @Test
    fun `compileToRegex compiles standard group correctly in middle of path`() {
        val testPattern = PathPattern("/complete/listing-{group}")
        testPattern.compileToRegex().pattern `should be equal to` "/complete/listing-(\\w+)"
    }

    // ENDREGION
}