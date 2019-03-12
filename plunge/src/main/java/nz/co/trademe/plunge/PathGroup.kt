package nz.co.trademe.plunge

/**
 * Data class containing a user-defined group. This group can be read as such:
 * {d|name}
 *  ^  ^
 *  |  └ The value of the [name] field
 *  Individual characters before the `|` make up the [flags] set.
 */
internal data class PathGroup(val name: String, val flags: Set<Char> = emptySet())