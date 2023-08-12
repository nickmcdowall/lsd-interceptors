package io.lsdconsulting.interceptors.http.naming

interface DestinationNameMappings {
    fun mapForPath(path: String): String
}
