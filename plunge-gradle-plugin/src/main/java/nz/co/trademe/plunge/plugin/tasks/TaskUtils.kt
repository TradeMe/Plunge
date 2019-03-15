package nz.co.trademe.plunge.plugin.tasks

import org.gradle.api.GradleException
import java.util.concurrent.TimeUnit

@Throws(CommandFailedException::class)
suspend fun runCommand(command: String): String {
    try {
        val process = Runtime.getRuntime().exec(command)

        process.waitFor(10, TimeUnit.SECONDS)
        process.inputStream.reader(Charsets.UTF_8).use {
            return it.readText()
        }

    } catch (e: Exception) {
        throw CommandFailedException
    }
}

@Throws(GradleException::class)
fun fail(reason: String): Nothing =
    throw GradleException(reason)

fun log(message: String) =
    println("[PLUNGE] $message")

object CommandFailedException : RuntimeException()