package io.lsdconsulting.interceptors.http.naming;

import io.lsdconsulting.interceptors.common.AppName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AppNameTest {

    @Test
    void removesSpecialCharacters() {
        var alwaysAppName = new AlwaysAppName(AppName.Factory.create("App/Service (Dev)"));

        assertThat(alwaysAppName.mapForPath("/")).isEqualTo("AppServiceDev");
    }
}