package io.lsdconsulting.interceptors.http.naming

fun interface DestinationNameMappings {
    fun mapForPath(path: String): String
}
