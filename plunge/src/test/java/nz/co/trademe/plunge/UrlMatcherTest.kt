package nz.co.trademe.plunge

import android.net.Uri
import org.amshove.kluent.`should contain all`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should equal`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UrlMatcherTest {

    // REGION urlMatcher

    @Test
    fun `urlMatcher returns a valid matcher for valid pattern`() {
        // This will throw if not valid
        urlMatcher(PathPattern("/complete/{group}"), emptyList()) {}
    }

    @Test
    fun `urlMatcher throws for an invalid pattern`() {
        { urlMatcher(PathPattern("/complete/{group"), emptyList()) {}} `should throw` IllegalArgumentException::class
    }

    // ENDREGION

    // REGION UrlMatcher.performMatch

    @Test
    fun `urlMatcher matches correctly on pattern`() {
        val matcher = urlMatcher(PathPattern("/complete/{group}"), emptyList()) {}
        val input = Uri.parse("https://www.test.com/complete/test")

        matcher.performMatch(input)?.params.orEmpty() `should contain` ("group" to "test")
    }

    @Test
    fun `urlMatcher matches correctly on digit pattern`() {
        val matcher = urlMatcher(PathPattern("/complete/{d|group}"), emptyList()) {}
        val input = Uri.parse("https://www.test.com/complete/1234")

        matcher.performMatch(input)?.params.orEmpty() `should contain` ("group" to "1234")
    }

    @Test
    fun `urlMatcher matches correctly on excluding pattern`() {
        val matcher = urlMatcher(PathPattern("/complete/{_}/listing"), emptyList()) {}
        val input = Uri.parse("https://www.test.com/complete/anything/listing")

        matcher.performMatch(input)?.params.orEmpty() `should equal` emptyMap()
    }

    @Test
    fun `urlMatcher matches correctly on multiple excluding pattern`() {
        val matcher = urlMatcher(PathPattern("/{_}/{_}/{_}/{_}/listing/{d|listingId}"), emptyList()) {}

        val input = Uri.parse("https://www.test.com/complete/anything/complete/anything/listing/123234345")

        matcher.performMatch(input)?.params.orEmpty() `should equal` mapOf("listingId" to "123234345")
    }

    @Test
    fun `urlMatcher doesn't match on digit pattern when word characters present`() {
        val matcher = urlMatcher(PathPattern("/complete/{d|group}"), emptyList()) {}
        val input = Uri.parse("https://www.test.com/complete/12fd34")

        matcher.performMatch(input)?.params.orEmpty() `should equal` emptyMap()
    }

    @Test
    fun `urlMatcher matches on multiple groups`() {
        val matcher = urlMatcher(PathPattern("/complete/{group1}/{group2}"), emptyList()) {}
        val input = Uri.parse("https://www.test.com/complete/1234/test")

        matcher.performMatch(input)?.params.orEmpty() `should contain all` mapOf("group1" to "1234", "group2" to "test")
    }

    @Test
    fun `urlMatcher matches on multiple groups with flags`() {
        val matcher = urlMatcher(PathPattern("/complete/{d|group1}/{group2}"), emptyList()) {}
        val input = Uri.parse("https://www.test.com/complete/1234/test")

        matcher.performMatch(input)?.params.orEmpty() `should contain all` mapOf("group1" to "1234", "group2" to "test")
    }

    @Test
    fun `urlMatcher matches on multiple groups in same path`() {
        val matcher = urlMatcher(PathPattern("/complete/{group1}-{group2}.htm"), emptyList()) {}
        val input = Uri.parse("https://www.test.com/complete/1234-test.htm")

        matcher.performMatch(input)?.params.orEmpty() `should contain all` mapOf("group1" to "1234", "group2" to "test")
    }

    @Test
    fun `urlMatcher matches on multiple groups in same path with flags`() {
        val matcher = urlMatcher(PathPattern("/complete/{d|group1}-{group2}.htm"), emptyList()) {}
        val input = Uri.parse("https://www.test.com/complete/1234-test.htm")

        matcher.performMatch(input)?.params.orEmpty() `should contain all` mapOf("group1" to "1234", "group2" to "test")
    }

    @Test
    fun `urlMatcher matches and returns query string params`() {
        val matcher = urlMatcher(PathPattern("/complete"), emptyList()) {}
        val input = Uri.parse("https://www.test.com/complete?param=test")

        matcher.performMatch(input)?.params.orEmpty() `should contain` ("param" to "test")
    }

    @Test
    fun `urlMatcher matches and overrides query string params when conflicting with group`() {
        val matcher = urlMatcher(PathPattern("/complete/{param}"), emptyList()) {}
        val input = Uri.parse("https://www.test.com/complete/1234?param=test")

        matcher.performMatch(input)?.params.orEmpty() `should contain` ("param" to "1234")
    }

    // ENDREGION

    // REGION UrlMatcher.onMatch

    @Test
    fun `urlMatcher should call handler on match`() {
        val matcher = urlMatcher(PathPattern("/complete/{param}"), emptyList()) { throw Exception(it.params["param"]) }
        val input = Uri.parse("https://www.test.com/complete/1234")

        fun run() { matcher.onMatch(matcher.performMatch(input)!!) }

        ::run `should throw` Exception::class `with message` "1234"
    }

    // ENDREGION
}