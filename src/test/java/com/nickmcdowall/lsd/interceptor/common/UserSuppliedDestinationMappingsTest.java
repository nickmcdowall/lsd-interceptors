package com.nickmcdowall.lsd.interceptor.common;

import com.nickmcdowall.lsd.interceptor.naming.DestinationNameMappings;
import org.junit.jupiter.api.Test;

import static com.nickmcdowall.lsd.interceptor.naming.UserSuppliedDestinationMappings.userSuppliedDestinationMappings;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

class UserSuppliedDestinationMappingsTest {

    @Test
    void usesPrefixToMatchNameToPath() {
        DestinationNameMappings names = userSuppliedDestinationMappings(of("/name", "NamingService"));

        assertThat(names.mapForPath("/name/one")).isEqualTo("NamingService");
    }

    @Test
    void usesDefaultKeyIfNoMatch() {
        DestinationNameMappings names = userSuppliedDestinationMappings(of("default", "SomeService"));

        assertThat(names.mapForPath("/name/one")).isEqualTo("SomeService");
    }

    @Test
    void fallsBackToUsingPathPrefixForNameIfNoMatchAndNoDefault() {
        DestinationNameMappings names = userSuppliedDestinationMappings(of());

        assertThat(names.mapForPath("/service-name/something")).isEqualTo("service_name");
    }

    @Test
    void picksMostSpecificMatch() {
        DestinationNameMappings mappings = userSuppliedDestinationMappings(of(
                "/na", "FirstNamingService",
                "/name/one", "MostSpecificNamingService",
                "/name/one/other", "DifferentNamingService",
                "/name", "MoreSpecificNamingService"
        ));

        assertThat(mappings.mapForPath("/name/one")).isEqualTo("MostSpecificNamingService");
    }
}