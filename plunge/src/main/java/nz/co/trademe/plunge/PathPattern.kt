package nz.co.trademe.plunge

/**
 * Simple data class used for defining a PathPattern. Ideally this would be an inline class, but due
 * to that language feature's experimental status we'll stick with a data class for now.
 */
internal data class PathPattern(val pattern: String)

/**
 * Function for validating that a pattern is valid. This function checks two conditions:
 *
 * * All groups defined are complete
 * * No keys are duplicated within the path pattern
 * * No keys are duplicated between defined groups and required query parameters
 */
internal fun PathPattern.isValid(requiredQueryParameters: List<String>): Boolean {
    // Check any capturing group is completed properly
    val incompleteGroups = pattern.split("/")
            .filter { it.contains("{") || it.contains("}") }
            .map { part -> part.split('{').let {
                // Remove the first bit as it's irrelevant
                it.subList(1, it.size)
            }}
            .flatten()
            .filterNot { it.isEmpty() || it.contains("}") }

    if (incompleteGroups.isNotEmpty()) {
        return false
    }

    // Check no group keys and expected query string keys are the same
    val allGroups = extractPathGroups()
            .map { it.name }
            .toList()

    // Filter non-capturing groups as they're fine to be duplicated
    val filteredGroups = allGroups.filterNot { it == "_" }

    if (filteredGroups.toSet().size < filteredGroups.size) {
        return false
    }

    val duplicateKeys = allGroups
            .filter { requiredQueryParameters.contains(it) }

    if (duplicateKeys.isNotEmpty()) {
        return false
    }

    return true
}

/**
 * Function for extracting path groups from a given pattern
 */
internal fun PathPattern.extractPathGroups(): List<PathGroup> =
    pattern.split("/")
            .asSequence()
            // Only validate parts with capturing groups
            .filter { it.contains("{") && it.contains("}") }
            // Pull out each group from the part
            .map { part ->
                part.split("{")
                        .filter { it.isNotEmpty() && it.contains("}") }
                        .map { it.substring(0, it.indexOf("}")) }
            }
            // Flatten to one list
            .flatten()
            .map {
                PathGroup(
                        name = if (it.contains("|")) it.split("|").last() else it,
                        flags = if (it.contains("|")) it.split("|").first().toSet() else emptySet()
                )
            }
            .toList()

/**
 * Function for compiling a given pattern to [Regex], also returning the
 * list of groups found while parsing.
 */
internal fun PathPattern.compileToRegex(): Regex {
    var regexString = ""

    val parts = pattern.split("/")

    parts.forEachIndexed { index, part ->
        // If the part doesn't contain groups, just add it and continue
        if (!part.contains("{") && !part.contains("}")) {
            regexString += part
        } else {
            var group = ""

            // Iterate through the part and generate Regex groups (either capturing or non-capturing)
            var inGroup = false

            part.forEachIndexed { i, c ->
                when {
                    c == '{' && !inGroup -> {
                        // Start group
                        inGroup = true
                        // Find the next '}' and grab the inner text
                        val partInner = part.substring(i + 1, part.indexOf('}', startIndex = i)).split('|')
                        val generatedRegex = when (partInner.size) {
                            // Group is of the format "{name}" - build a standard group
                            1 -> buildStandardNamedGroup(partInner[0])
                            // Group is for the format "{d|name}" where everything before the vertical bar
                            // counts as a flag. Build specific group based off flags.
                            else -> buildNamedGroupWithFlags(partInner[1], partInner[0].toSet())
                        }

                        group += generatedRegex
                    }
                    c == '}' && inGroup -> {
                        // End of the group, continue
                        inGroup = false
                    }
                    !inGroup -> {
                        group += c
                    }
                }
            }

            regexString += group
        }

        if (index != parts.size - 1) {
            regexString += "/"
        }
    }

    // Add an optional, non-capturing trailing slash if the last character isn't a non-optional trailing slash
    if (regexString.last() != '/') {
        regexString += "(?:/)?"
    }

    return regexString.toRegex(RegexOption.IGNORE_CASE)
}

/**
 * Function for building a standard named groups, i.e "(?<name>\\w+)
 */
private fun buildStandardNamedGroup(name: String): String =
        buildNamedGroupWithFlags(name, emptySet())

/**
 * Function for building a named group, taking in to account the flags present on the capturing group
 */
private fun buildNamedGroupWithFlags(name: String, flags: Set<Char>): String =
        when (name) {
            // If the name is `_` we should construct a non-capturing group matching word characters
            "_" -> if (flags.contains('d')) "(?:\\d+)" else "(?:[^/]+)"
            // Construct a capturing group
            else -> if (flags.contains('d')) "(\\d+)" else "([^/]+)"
        }