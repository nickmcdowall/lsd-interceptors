package com.nickmcdowall.lsd.http.naming;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

class UserSuppliedSourceMappingsTest {

    @Test
    void usesPrefixToMatchNameToPath() {
        SourceNameMappings names = UserSuppliedSourceMappings.userSuppliedSourceMappings(of("/name", "NamingService"));

        assertThat(names.mapForPath("/name/one")).isEqualTo("NamingService");
    }

    @Test
    void usesDefaultKeyIfNoMatch() {
        SourceNameMappings names = UserSuppliedSourceMappings.userSuppliedSourceMappings(of("default", "SomeService"));

        assertThat(names.mapForPath("/name/one")).isEqualTo("SomeService");
    }

    @Test
    void sourceNameFallsBackToAppIfNotMatchesFound() {
        Assertions.assertThat(UserSuppliedSourceMappings.userSuppliedSourceMappings(of()).mapForPath("/service-name/something")).isEqualTo("App");
    }

    @Test
    void picksMostSpecificMatch() {
        SourceNameMappings mappings = UserSuppliedSourceMappings.userSuppliedSourceMappings(of(
                "/na", "User",
                "/name/one", "Consumer",
                "/name/one/other", "Client",
                "/name", "Admin"
        ));

        assertThat(mappings.mapForPath("/name/one")).isEqualTo("Consumer");
    }

}