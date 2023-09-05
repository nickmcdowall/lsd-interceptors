package io.lsdconsulting.interceptors.http.naming

private const val FIRST_PART_OF_PATH = "^/?(.*?)([/?].*|$)"

class RegexResolvingNameMapper : DestinationNameMappings {
    override fun mapForPath(path: String): String = path
        .replaceFirst(FIRST_PART_OF_PATH.toRegex(), "$1")

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return javaClass == other?.javaClass
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
