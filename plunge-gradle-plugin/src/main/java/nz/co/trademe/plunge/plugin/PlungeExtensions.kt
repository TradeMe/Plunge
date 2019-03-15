package nz.co.trademe.plunge.plugin

import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.io.File

/**
 * Extensions class used for defining additional input for the plunge plugin
 */
open class PlungeExtensions(project: Project) {

    private val testDirectory: Property<File> = project.objects.property(File::class.java)

    fun getTestDirectory(): Property<File> = testDirectory

}