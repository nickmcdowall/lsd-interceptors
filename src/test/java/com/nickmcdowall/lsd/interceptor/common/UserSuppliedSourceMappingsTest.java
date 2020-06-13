package com.nickmcdowall.lsd.interceptor.common;

import com.nickmcdowall.lsd.interceptor.naming.SourceNameMappings;
import org.junit.jupiter.api.Test;

import static com.nickmcdowall.lsd.interceptor.naming.UserSuppliedSourceMappings.userSuppliedSourceMappings;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

class UserSuppliedSourceMappingsTest {

    @Test
    void usesPrefixToMatchNameToPath() {
        SourceNameMappings names = userSuppliedSourceMappings(of("/name", "NamingService"));

        assertThat(names.mapForPath("/name/one")).isEqualTo("NamingService");
    }

    @Test
    void usesDefaultKeyIfNoMatch() {
        SourceNameMappings names = userSuppliedSourceMappings(of("default", "SomeService"));

        assertThat(names.mapForPath("/name/one")).isEqualTo("SomeService");
    }

    @Test
    void sourceNameFallsBackToAppIfNotMatchesFound() {
        assertThat(userSuppliedSourceMappings(of()).mapForPath("/service-name/something")).isEqualTo("App");
    }

    @Test
    void picksMostSpecificMatch() {
        SourceNameMappings mappings = userSuppliedSourceMappings(of(
                "/na", "User",
                "/name/one", "Consumer",
                "/name/one/other", "Client",
                "/name", "Admin"
        ));

        assertThat(mappings.mapForPath("/name/one")).isEqualTo("Consumer");
    }

}