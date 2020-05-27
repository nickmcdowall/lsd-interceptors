package com.googlecode.nickmcdowall.interceptor.common;

import org.junit.jupiter.api.Test;

import static com.googlecode.nickmcdowall.interceptor.common.PathToDestinationNameMapper.destinationMappings;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

class PathToDestinationNameMapperTest {

    @Test
    void usesPrefixToMatchNameToPath() {
        PathToDestinationNameMapper namingService = destinationMappings(of("/name", "NamingService"));

        String destinationName = namingService.mapForPath("/name/one");

        assertThat(destinationName).isEqualTo("NamingService");
    }

    @Test
    void usesDefaultKeyIfNoMatch() {
        PathToDestinationNameMapper namingService = destinationMappings(of("default", "SomeService"));

        String destinationName = namingService.mapForPath("/name/one");

        assertThat(destinationName).isEqualTo("SomeService");
    }

    @Test
    void usesOtherIfNoDefaultSet() {
        PathToDestinationNameMapper namingService = destinationMappings(of());

        String destinationName = namingService.mapForPath("/name/one");

        assertThat(destinationName).isEqualTo("Other");
    }

    @Test
    void picksMostSpecificMatch() {
        PathToDestinationNameMapper namingService = destinationMappings(of(
                "/na", "FirstNamingService",
                "/name/one", "MostSpecificNamingService",
                "/name/one/other", "DifferentNamingService",
                "/name", "MoreSpecificNamingService"
        ));

        String destinationName = namingService.mapForPath("/name/one");

        assertThat(destinationName).isEqualTo("MostSpecificNamingService");
    }
}