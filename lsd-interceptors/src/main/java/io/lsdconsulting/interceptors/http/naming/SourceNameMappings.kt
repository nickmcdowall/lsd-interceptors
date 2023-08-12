package io.lsdconsulting.interceptors.http.naming

fun interface SourceNameMappings {
    fun mapForPath(path: String): String
}
