package io.lsdconsulting.interceptors.common

fun singleValueMap(headers: Map<String, Collection<String>>): Map<String, String> {
    return headers.mapValues { it.value.firstOrNull() ?: "" }
}

enum class HeaderKeys(private val headerName: String) {
    SOURCE_NAME("Source-Name"),
    TARGET_NAME("Target-Name");

    fun key() = headerName
}
