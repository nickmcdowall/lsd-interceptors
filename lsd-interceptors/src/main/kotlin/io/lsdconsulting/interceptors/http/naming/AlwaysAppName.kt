package io.lsdconsulting.interceptors.http.naming

import io.lsdconsulting.interceptors.common.AppName

data class AlwaysAppName(
    var appName: AppName
) : SourceNameMappings {

    override fun mapForPath(path: String): String {
        return appName.value
    }
}
