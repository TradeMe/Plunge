package nz.co.trademe.plunge.test.model

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable

@Serializable
data class PlungeTestCase(
    val url: String,
    val description: String,
    @Optional val handled: Boolean = true,
    @Optional val params: List<PlungeTestCaseParam> = emptyList()
) {
    override fun toString(): String = "$description (Handled: $handled)"
}
