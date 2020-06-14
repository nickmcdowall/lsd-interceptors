package com.nickmcdowall.lsd.http.naming;

import org.junit.jupiter.api.Test;

import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

class UserSuppliedDestinationMappingsTest {

    @Test
    void usesPrefixToMatchNameToPath() {
        DestinationNameMappings names = UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of("/name", "NamingService"));

        assertThat(names.mapForPath("/name/one")).isEqualTo("NamingService");
    }

    @Test
    void usesDefaultKeyIfNoMatch() {
        DestinationNameMappings names = UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of("default", "SomeService"));

        assertThat(names.mapForPath("/name/one")).isEqualTo("SomeService");
    }

    @Test
    void fallsBackToUsingPathPrefixForNameIfNoMatchAndNoDefault() {
        DestinationNameMappings names = UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of());

        assertThat(names.mapForPath("/service-name/something")).isEqualTo("service_name");
    }

    @Test
    void picksMostSpecificMatch() {
        DestinationNameMappings mappings = UserSuppliedDestinationMappings.userSuppliedDestinationMappings(of(
                "/na", "FirstNamingService",
                "/name/one", "MostSpecificNamingService",
                "/name/one/other", "DifferentNamingService",
                "/name", "MoreSpecificNamingService"
        ));

        assertThat(mappings.mapForPath("/name/one")).isEqualTo("MostSpecificNamingService");
    }
}