package io.lsdconsulting.interceptors.http.naming;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AppNameTest {

    @Test
    void removesSpecialCharacters() {
        AppName appName = new AppName("App/Service (Dev)");

        assertThat(appName.mapForPath("/")).isEqualTo("App_Service_Dev_");
    }
}