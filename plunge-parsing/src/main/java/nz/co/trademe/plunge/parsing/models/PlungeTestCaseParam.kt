package nz.co.trademe.plunge.parsing.models

import kotlinx.serialization.Serializable

@Serializable
data class PlungeTestCaseParam(
    val name: String,
    val value: String
)