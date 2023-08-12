package io.lsdconsulting.interceptors.http.naming

private const val FIRST_PART_OF_PATH = "^/?(.*?)([/?].*|$)"
private const val PLANT_UML_CRYPTONITE = "[-]" //Characters that blow up PlantUml need to be replaced

class RegexResolvingNameMapper : DestinationNameMappings {
    override fun mapForPath(path: String): String = path
        .replaceFirst(FIRST_PART_OF_PATH.toRegex(), "$1")
        .replace(PLANT_UML_CRYPTONITE.toRegex(), "_") // TODO Shouldn't this reuse `sanitise()` ?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return javaClass == other?.javaClass
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
