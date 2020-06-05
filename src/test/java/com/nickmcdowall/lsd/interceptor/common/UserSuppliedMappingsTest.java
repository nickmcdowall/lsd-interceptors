package com.nickmcdowall.lsd.interceptor.common;

import org.junit.jupiter.api.Test;

import static com.nickmcdowall.lsd.interceptor.common.UserSuppliedMappings.userSuppliedMappings;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

class UserSuppliedMappingsTest {

    @Test
    void usesPrefixToMatchNameToPath() {
        PathToNameMapper names = userSuppliedMappings(of("/name", "NamingService"));

        assertThat(names.mapForPath("/name/one")).isEqualTo("NamingService");
    }

    @Test
    void usesDefaultKeyIfNoMatch() {
        PathToNameMapper names = userSuppliedMappings(of("default", "SomeService"));

        assertThat(names.mapForPath("/name/one")).isEqualTo("SomeService");
    }

    @Test
    void fallsBackToFirtPartOfPathIfNoMatchAndNoDefault() {
        PathToNameMapper names = userSuppliedMappings(of());

        assertThat(names.mapForPath("/service-name/something")).isEqualTo("service_name");
    }

    @Test
    void picksMostSpecificMatch() {
        PathToNameMapper mappings = userSuppliedMappings(of(
                "/na", "FirstNamingService",
                "/name/one", "MostSpecificNamingService",
                "/name/one/other", "DifferentNamingService",
                "/name", "MoreSpecificNamingService"
        ));

        assertThat(mappings.mapForPath("/name/one")).isEqualTo("MostSpecificNamingService");
    }
}