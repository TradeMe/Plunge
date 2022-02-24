package nz.co.trademe.plunge.parsing

import kotlinx.serialization.json.Json
import nz.co.trademe.plunge.parsing.models.PlungeTestCase
import java.io.File

private const val JSON_EXTENSION = "json"

/**
 * Common entry point for parsing plunge test cases. Used by both the plugin, and the unit test module.
 */
object PlungeParser {

    fun readTestCases(directory: File): List<PlungeTestCase> =
        directory
            .listFiles { f -> f?.extension == JSON_EXTENSION }
            ?.map { Json.decodeFromString(PlungeTestCase.serializer(), it.readText()) } ?: emptyList()
}