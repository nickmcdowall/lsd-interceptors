package io.lsdconsulting.interceptors.http.naming

import io.lsdconsulting.interceptors.common.AppName.Factory.create
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

internal class AppNameTest {

    @Test
    fun removesSpecialCharacters() {
        val alwaysAppName = AlwaysAppName(create("App/Service (Dev)"))
        assertThat(alwaysAppName.mapForPath("/")).isEqualTo("AppServiceDev")
    }
}