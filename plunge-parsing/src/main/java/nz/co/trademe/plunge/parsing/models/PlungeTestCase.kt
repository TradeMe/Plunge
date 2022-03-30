package nz.co.trademe.plunge.parsing.models

import kotlinx.serialization.Serializable

@Serializable
data class PlungeTestCase(
    val url: String,
    val description: String,
    val handled: Boolean? = true,
    val params: List<PlungeTestCaseParam>? = emptyList()
) {
    override fun toString(): String = "$description (Handled: $handled)"
}