package io.lsdconsulting.interceptors.http.naming

interface SourceNameMappings {
    fun mapForPath(path: String): String
}
