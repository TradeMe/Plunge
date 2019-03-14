package nz.co.trademe.plunge.test.model

import kotlinx.serialization.Serializable

@Serializable
data class PlungeTestCaseParam(
    val name: String,
    val value: String
) {
    override fun toString(): String = "\"$name\" -> \"$value\""
}