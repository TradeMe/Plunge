package nz.co.trademe.plunge.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import nz.co.trademe.plunge.plugin.tasks.TestDeepLinks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import kotlin.reflect.KClass

/**
 * Main entry point for the Plunge Plugin. It applies to an application project
 * and adds tasks which allows easy use of optional testing frameworks.
 */
class PlungePlugin: Plugin<Project> {

    override fun apply(project: Project) {
        // Register extension
        val extensions = project.extensions.create("plunge", PlungeExtensions::class.java, project)

        // Register tasks after evaluation
        project.afterEvaluate { evaluated ->
            evaluated.plugins.all {
                when (it) {
                    is AppPlugin -> {
                        // Apply tasks to project when the project contains the Android [AppPlugin]
                        val extension = project.extensions[AppExtension::class]

                        project.applyTasks(
                            appVariants = extension.applicationVariants.toList(),
                            plungeExtensions = extensions)
                    }
                }
            }
        }

    }

    private fun Project.applyTasks(appVariants: List<ApplicationVariant>, plungeExtensions: PlungeExtensions) {
        appVariants.forEach { variant ->
            tasks.create("plungeTest${variant.name.capitalize()}", TestDeepLinks::class.java) {
                it.packageName = variant.applicationId
                it.testCaseDirectory.set(plungeExtensions.getTestDirectory())

                // Depend on the install task if it exists
                tasks.find { task -> task.name == "install${variant.name.capitalize()}" }?.let { installTask ->
                    it.dependsOn.add(installTask.name)
                }
            }
        }
    }

    private operator fun <T : Any> ExtensionContainer.get(type: KClass<T>): T {
        return getByType(type.java)
    }
}