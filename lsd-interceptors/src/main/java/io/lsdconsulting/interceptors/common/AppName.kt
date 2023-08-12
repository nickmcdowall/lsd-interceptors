package io.lsdconsulting.interceptors.common

import com.lsd.core.domain.ComponentName

data class AppName private constructor(val value: String) {

    companion object Factory {
        fun create(value: String) : AppName {
            return AppName(ComponentName(value).normalisedName)
        }
    }
}
